package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductDto extends BaseDto{

    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imagePath;
    private String categoryName;
    private UUID categoryId;
}
