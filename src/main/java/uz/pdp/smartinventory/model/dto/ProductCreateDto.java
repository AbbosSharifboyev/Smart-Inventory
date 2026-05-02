package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductCreateDto{
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private MultipartFile image;
    private UUID categoryId;
}
