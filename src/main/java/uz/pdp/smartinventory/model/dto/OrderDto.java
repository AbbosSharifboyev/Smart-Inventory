package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;
import uz.pdp.smartinventory.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderDto extends BaseDto{
    private String userFullName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemDto> items;
}
