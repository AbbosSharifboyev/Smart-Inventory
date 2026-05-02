package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto extends BaseDto {
    private String name;
    private String description;
}
