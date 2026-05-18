package uz.pdp.smartinventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.smartinventory.criteria.OrderCriteria;
import uz.pdp.smartinventory.mapper.OrderMapper;
import uz.pdp.smartinventory.model.domain.OrderItems;
import uz.pdp.smartinventory.model.domain.Orders;
import uz.pdp.smartinventory.model.domain.Products;
import uz.pdp.smartinventory.model.domain.Users;
import uz.pdp.smartinventory.model.dto.*;
import uz.pdp.smartinventory.model.enums.MovementType;
import uz.pdp.smartinventory.model.enums.OrderStatus;
import uz.pdp.smartinventory.repository.OrderRepository;
import uz.pdp.smartinventory.repository.ProductRepository;
import uz.pdp.smartinventory.repository.UserRepository;
import uz.pdp.smartinventory.validator.OrderValidator;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class OrderServiceImpl extends AbstractService<OrderRepository, OrderMapper, OrderValidator>
                implements OrderService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ActionLogService actionLogService;
    private final StockMovementService stockMovementService;

    protected OrderServiceImpl(OrderRepository repository,
                               OrderMapper mapper,
                               OrderValidator validator,
                               ProductRepository productRepository,
                               UserRepository userRepository, ActionLogService actionLogService, StockMovementService stockMovementService) {
        super(repository, mapper, validator);
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.actionLogService = actionLogService;
        this.stockMovementService = stockMovementService;
    }

    @Override
    @Transactional
    public OrderDto create(OrderRequestDto dto) {
        validator.validateCreate(dto);

        Users user = userRepository.findByIdAndDeletedFalse(dto.getUserId())
                .orElseThrow(() ->new RuntimeException("Foydalanuvchi topilmadi"));

        // Entity yaratish (Mapper orqali)
        Orders order = mapper.toEntity(dto);
        order.setUser(user);  // Userni biriktiramiz
        order.setItems(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;

        // OrderItems-ni shakllantirish va Omborni yangilash
        for (OrderItemRequestDto itemDto : dto.getItems()) {
            Products product = productRepository.findByIdAndDeletedFalse(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi"));

            // OrderItem ob'ektini yaratish
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setCount(itemDto.getCount());
            orderItem.setPriceAtOrder(product.getPrice());  // Narxni muhrlash (Snapshot)

            // Jami summani hisoblash
            BigDecimal subTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemDto.getCount()));
            totalAmount = totalAmount.add(subTotal);

            order.getItems().add(orderItem);
        }
        order.setTotalAmount(totalAmount);
        Orders savedOrder = repository.save(order);

        for (OrderItems item : savedOrder.getItems()) {
            stockMovementService.createMovement(
                    item.getProduct().getId(),
                    item.getCount(),
                    MovementType.OUT,
                    "Buyurtma yaratildi. Buyurtma ID: " + savedOrder.getId(),
                    savedOrder,
                    item.getPriceAtOrder()
            );
        }

        actionLogService.saveLog("Yangi buyurtma qabul qilindi: "
                + order.getItems().size() + " ta mahsulot", "SUCCESS");
        return mapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto update(OrderUpdateDto dto, UUID id) {
        Orders existingOrder = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Buyurtma topilmadi!"));

        OrderStatus oldStatus = existingOrder.getStatus();
        OrderStatus newStatus = dto.getStatus();

        // Status o'zgarishi mantiqan to'g'riligini tekshirish
        validator.validateUpdate(dto,existingOrder);

        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED){
            restockProducts(existingOrder);
        }
        // Mapper orqali status va boshqa maydonlarni yangilash
        mapper.updateEntity(dto,existingOrder);

        Orders saved = repository.save(existingOrder);
        actionLogService.saveLog("Buyurtma #" + id + " statusi o'zgardi", "INFO");
        return mapper.toDto(saved);
    }

    private void restockProducts(Orders order) {
        for (OrderItems item : order.getItems()) {
            Products product = item.getProduct();
            if (product != null){
                stockMovementService.createMovement(
                        product.getId(),
                        item.getCount(),
                        MovementType.IN,
                        "Buyurtma bekor qilindi. Buyurtma ID: " + order.getId(),
                        order,
                        item.getPriceAtOrder()
                );
            }
        }
    }

    @Override
    public OrderDto get(UUID id) {
        return repository.findByIdAndDeletedFalse(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Buyurtma topilmadi"));
    }

    @Override
    public Page<OrderDto> getAll(OrderCriteria criteria) {

        int page = (criteria.getPage() != null) ? criteria.getPage() : 0;
        int size = (criteria.getSize() != null) ? criteria.getSize() : 100;
        Pageable pageable = PageRequest.of(page, size);

        // findAll(Specification, Pageable) metodini chaqirish
        Page<Orders> orderPage = repository.findAll((root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // 1. Ism bo'yicha
            if (criteria.getUserFullName() != null && !criteria.getUserFullName().isBlank()){
                    predicates.add(cb.like(cb.lower(root.get("user").get("fullName")),
                            "%" + criteria.getUserFullName().toLowerCase() + "%"));
            }
            // 2. Status bo'yicha
            if (criteria.getStatus() != null){
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));

            }
            // 3. Sana oralig'i
            if (criteria.getDateFrom() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        criteria.getDateFrom().atStartOfDay()));
            }
            if (criteria.getDateTo() != null){
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                        criteria.getDateTo().atTime(23, 59, 59)));
            }
            // 4. Minimal summa
            if (criteria.getMinAmount() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"),
                        criteria.getMinAmount()));
            }
            // 5. O'chirilmaganlarini olish
            predicates.add(cb.equal(root.get("deleted"), false));

            // Tartiblash
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        } ,pageable);

        // Page<Orders> ni Page<OrderDto> ga o'girib qaytarish
        return orderPage.map(mapper::toDto);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Orders order = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Buyurtma topilmadi!"));
        order.setDeleted(true);
        repository.save(order);
    }

    @Override
    public long countByStatus(String statusName){
        return repository.countByStatusAndDeletedFalse(OrderStatus.valueOf(statusName));
    }

    @Override
    public long countByStatuses(List<String> statusNames){
        List<OrderStatus> statuses = statusNames.stream().
                map(OrderStatus::valueOf).
                toList();
        return repository.countByStatusInAndDeletedFalse(statuses);
    }

    public long countTodayOrders() {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return repository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    public long countByDeletedFalse() {
        return repository.countByDeletedFalse();
    }

    public BigDecimal getTotalRevenue() {

        BigDecimal revenue = repository.getTotalRevenueByStatus(OrderStatus.COMPLETED);

        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}
