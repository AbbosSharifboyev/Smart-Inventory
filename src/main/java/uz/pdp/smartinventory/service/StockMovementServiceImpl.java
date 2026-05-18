package uz.pdp.smartinventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.smartinventory.model.domain.Orders;
import uz.pdp.smartinventory.model.domain.Products;
import uz.pdp.smartinventory.model.domain.StockMovement;
import uz.pdp.smartinventory.model.dto.StockMovementDto;
import uz.pdp.smartinventory.model.enums.MovementType;
import uz.pdp.smartinventory.repository.ProductRepository;
import uz.pdp.smartinventory.repository.StockMovementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockMovementServiceImpl implements StockMovementService{

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;


    @Override
    @Transactional
    public void createMovement(
            UUID productId,
            Integer quantity,
            MovementType type,
            String reason,
            Orders order,
            BigDecimal priceAtMovement) {

        log.info("Omborda harakat boshlandi: ProductId={}, Type={}, Qty={}", productId, type, quantity);

        Products product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi! ID: " + productId));

        if (type == MovementType.IN) {
            product.setQuantity(product.getQuantity() + quantity);
        } else if (type == MovementType.OUT) {
            if (product.getQuantity() < quantity) {
                throw new RuntimeException("Omborda yetarli mahsulot yo'q! So'raldi: "
                        + quantity + ", Bor: " + product.getQuantity());
            }
            product.setQuantity(product.getQuantity() - quantity);
        }
        productRepository.save(product);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setOrder(order);
        movement.setPriceAtMovement(
                priceAtMovement != null ? priceAtMovement : product.getPrice()
        );

        stockMovementRepository.save(movement);
        log.info("Ombor harakati muvaffaqiyatli saqlandi. Harakat ID: {}", movement.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementDto> getFilteredMovements(
            MovementType type, String productName, LocalDate from, LocalDate to, Pageable pageable) {

        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime toDateTime = (to != null) ? to.atTime(23, 59, 59) : LocalDateTime.of(2099, 12, 31, 23, 59);

        return stockMovementRepository.findFiltered(
                type,
                productName != null && !productName.isBlank() ? productName : null,
                fromDateTime,
                toDateTime,
                pageable
        ).map(this::convertToDto);
    }


    private StockMovementDto convertToDto(StockMovement entity){

        StockMovementDto dto = new StockMovementDto();
        dto.setId(entity.getId());
        dto.setProductId(entity.getProduct().getId());
        dto.setProductName(entity.getProduct().getName());
        dto.setType(entity.getType().name());
        dto.setQuantity(entity.getQuantity());
        dto.setPriceAtMovement(entity.getPriceAtMovement());
        dto.setReason(entity.getReason());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        if (entity.getOrder() != null){
            dto.setOrderId(entity.getOrder().getId());
        }
        return dto;
    }
}
