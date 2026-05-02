package uz.pdp.smartinventory.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateDto {

    @NotBlank(message = "Kategory nomi bo`sh bo`lishi mumkin emas")
    private String name;

    private String description;
}
