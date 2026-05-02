package uz.pdp.smartinventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import lombok.*;
import java.util.UUID;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private UUID productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imagePath;

    public BigDecimal getTotalPrice() {
        if (price == null || quantity == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}