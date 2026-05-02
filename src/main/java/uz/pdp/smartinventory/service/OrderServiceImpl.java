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
import uz.pdp.smartinventory.model.enums.OrderStatus;
import uz.pdp.smartinventory.repository.OrderRepository;
import uz.pdp.smartinventory.repository.ProductRepository;
import uz.pdp.smartinventory.repository.UserRepository;
import uz.pdp.smartinventory.validator.OrderValidator;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl extends AbstractService<OrderRepository, OrderMapper, OrderValidator>
                implements OrderService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    protected OrderServiceImpl(OrderRepository repository,
                               OrderMapper mapper,
                               OrderValidator validator,
                               ProductRepository productRepository,
                               UserRepository userRepository) {
        super(repository, mapper, validator);
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OrderDto create(OrderRequestDto dto) {
        // 1. Validatsiya (Format va Ombor qoldig'ini tekshirish)
        validator.validateCreate(dto);

        // 2. Foydalanuvchini olish
        Users user = userRepository.findByIdAndDeletedFalse(dto.getUserId())
                .orElseThrow(() ->new RuntimeException("Foydalanuvchi topilmadi"));

        // 3. Entity yaratyapmiz (Mapper orqali)
        Orders order = mapper.toEntity(dto);
        order.setUser(user);  // Userni biriktiramiz
        order.setItems(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 4. OrderItems-ni shakllantirish va Omborni yangilash
        for (OrderItemRequestDto itemDto : dto.getItems()) {
            Products product = productRepository.findByIdAndDeletedFalse(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi"));

            // OrderItem ob'ektini yaratish
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setCount(itemDto.getCount());
            orderItem.setPriceAtOrder(product.getPrice());  // Narxni muhrlash (Snapshot)

            // Mahsulot miqdorini kamaytirish (Stock update)
            product.setQuantity(product.getQuantity() - itemDto.getCount());
            productRepository.save(product);

            // Jami summani hisoblash
            BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getCount()));
            totalAmount = totalAmount.add(subTotal);

            order.getItems().add(orderItem);
        }
        order.setTotalAmount(totalAmount);

        // 5. Saqlash va DTO-ga o'girib qaytarish
        Orders savedOrder = repository.save(order);
        return mapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto update(OrderUpdateDto dto, UUID id) {
        Orders existingOrder = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Buyurtma topilmadi!"));

        // Status o'zgarishi mantiqan to'g'riligini tekshirish
        validator.validateUpdate(dto,existingOrder);

        // Mapper orqali status va boshqa maydonlarni yangilash
        mapper.updateEntity(dto,existingOrder);

        Orders saved = repository.save(existingOrder);
        return mapper.toDto(saved);
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
}
