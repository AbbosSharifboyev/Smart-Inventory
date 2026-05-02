package uz.pdp.smartinventory.criteria;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Getter
@Setter
public class ProductCriteria extends BaseCriteria{
    private String name;
    private UUID categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort;
}
