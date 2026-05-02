package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemRequestDto {
    private UUID productId;
    private Integer count;
}
