package uz.pdp.smartinventory.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.config.CustomSecurityService;
import uz.pdp.smartinventory.criteria.ProductCriteria;
import uz.pdp.smartinventory.model.dto.IdNameDto;
import uz.pdp.smartinventory.model.dto.ProductCreateDto;
import uz.pdp.smartinventory.model.dto.ProductDto;
import uz.pdp.smartinventory.model.dto.ProductUpdateDto;
import uz.pdp.smartinventory.service.ProductService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    //@PreAuthorize("@auth.hasPermission('PRODUCT_READ')")
    public ResponseEntity<Page<ProductDto>> getAllPage(ProductCriteria criteria){

        Page<ProductDto> productPage = productService.getAll(criteria);
        return ResponseEntity.ok(productPage);
    }

    @GetMapping("/stats")
    //@PreAuthorize("@auth.hasPermission('PRODUCT_CREATE')")
    public ResponseEntity<Map<String, Long>> getStats(){

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalProducts",     productService.countTotalProducts());
        stats.put("availableProducts", productService.countAvailableProducts());
        stats.put("lessProducts",      productService.countAlertProducts());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    // @PreAuthorize("@auth.hasPermission('PRODUCT_READ')")
    public ResponseEntity<ProductDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.get(id));
    }

    @GetMapping("/select-list")
    public ResponseEntity<List<IdNameDto>> getProductSelectList(){
        return ResponseEntity.ok(productService.getAllActiveForSelect());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    //@PreAuthorize("@auth.hasPermission('PRODUCT_CREATE')")
    public ResponseEntity<ProductDto> create(@Valid @ModelAttribute ProductCreateDto dto){

        ProductDto savedDto = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }


    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    //@PreAuthorize("@auth.hasPermission('PRODUCT_UPDATE')")
    public ResponseEntity<Void> update(@PathVariable UUID id,
                         @Valid @ModelAttribute ProductUpdateDto dto){

        productService.update(dto, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("@auth.hasPermission('PRODUCT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
