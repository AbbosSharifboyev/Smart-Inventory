package uz.pdp.smartinventory.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.smartinventory.model.dto.ProductDto;
import uz.pdp.smartinventory.service.ProductService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartValidator {

    private final ProductService productService;

    public void validateStock(UUID productId, int requestedQty){
        ProductDto product = productService.get(productId);
        if (product.getQuantity() <= 0){
            throw new RuntimeException("Kechirasiz mahsulot tugagan!");
        }
        if (requestedQty > product.getQuantity()){
            throw new RuntimeException("Omborda yetarli mahsulot yo`q! " +
                    "(Mavjud: " + product.getQuantity() + ")" );
        }
    }

}
