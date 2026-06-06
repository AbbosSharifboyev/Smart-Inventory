package uz.pdp.smartinventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.config.CustomSecurityService;
import uz.pdp.smartinventory.criteria.CategoryCriteria;
import uz.pdp.smartinventory.model.dto.CategoryCreateDto;
import uz.pdp.smartinventory.model.dto.CategoryDto;
import uz.pdp.smartinventory.model.dto.CategoryUpdateDto;
import uz.pdp.smartinventory.model.dto.IdNameDto;
import uz.pdp.smartinventory.service.CategoryService;
import uz.pdp.smartinventory.service.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;


    @GetMapping()
    //@PreAuthorize("@auth.hasPermission('CATEGORY_READ')")
    public ResponseEntity<Page<CategoryDto>> getAll(CategoryCriteria criteria){

        Page<CategoryDto> categoryPage = categoryService.getAll(criteria);
        return ResponseEntity.ok(categoryPage);
    }

    // Alohida statistika endpointi (Ekranning tepasidagi kichik panellar uchun)
    @GetMapping("/stats")
    //@PreAuthorize("@auth.hasPermission('CATEGORY_READ')")
    public ResponseEntity<Map<String, Long>> getStats(){

        Map<String, Long> stats = new HashMap<>();
        stats.put("activeCatCount", categoryService.countActiveCategories());
        stats.put("totalProductCount", productService.countActiveProducts());
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/{id}")
    //@PreAuthorize("@auth.hasPermission('CATEGORY_READ')")
    public ResponseEntity<CategoryDto> getById(@PathVariable UUID id){

        return ResponseEntity.ok(categoryService.get(id));
    }


    @GetMapping("/select-list")
    //@PreAuthorize("@auth.hasPermission('CATEGORY_READ')")
    public ResponseEntity<List<IdNameDto>> getForSelect(){

        return ResponseEntity.ok(categoryService.getAllForSelect());
    }

    @PostMapping()
    //@PreAuthorize("@auth.hasPermission('CATEGORY_CREATE')")
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryCreateDto dto){

        CategoryDto savedDto = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }


    @PutMapping("/{id}")
    //@PreAuthorize("@auth.hasPermission('CATEGORY_UPDATE')")
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @Valid @RequestBody CategoryUpdateDto dto){
        categoryService.update(dto, id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content (Muzaffaqiyatli yangilandi, lekin qaytishga tana yo'q)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@auth.hasPermission('CATEGORY_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        categoryService.delete(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
