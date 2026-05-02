package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateDto extends BaseDto{

    private String name;
    private String description;
}
