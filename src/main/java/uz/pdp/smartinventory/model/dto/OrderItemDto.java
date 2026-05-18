package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class OrderItemDto {
    private UUID productId;
    private String productName;
    private Integer count;
    private BigDecimal priceAtOrder;
    private BigDecimal subTotal;   //count * priceAtOrder
}
