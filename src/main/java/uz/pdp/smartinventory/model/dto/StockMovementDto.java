package uz.pdp.smartinventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDto extends BaseDto{

    private UUID productId;
    private String productName;

    private String type;
    private Integer quantity;
    private BigDecimal priceAtMovement;
    private String reason;

    private UUID orderId;

    private String orderStatus;
}
