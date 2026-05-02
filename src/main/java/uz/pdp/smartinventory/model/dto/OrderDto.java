package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;
import uz.pdp.smartinventory.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderDto extends BaseDto{
    private String userFullName;  // User entity'dan olinadi
    private BigDecimal totalAmount;   // Umumiy summa
    private OrderStatus status; // CANCELED,NEW
    private List<OrderItemDto> items; //Buyurtma tarkibi
}
