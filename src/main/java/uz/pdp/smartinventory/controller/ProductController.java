package uz.pdp.smartinventory.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.config.CustomSecurityService;
import uz.pdp.smartinventory.criteria.ProductCriteria;
import uz.pdp.smartinventory.model.dto.ProductCreateDto;
import uz.pdp.smartinventory.model.dto.ProductDto;
import uz.pdp.smartinventory.model.dto.ProductUpdateDto;
import uz.pdp.smartinventory.service.CategoryService;
import uz.pdp.smartinventory.service.ProductService;


import java.util.UUID;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final ProductService service;
    private final CategoryService categoryService;
    private final CustomSecurityService authService;

    @GetMapping
    @PreAuthorize("@auth.hasPermission('PRODUCT_READ')")
    public String getAllPage(Model model, @ModelAttribute("criteria") ProductCriteria criteria){

        Page<ProductDto> productPage = service.getAll(criteria);

        model.addAttribute("products",productPage.getContent());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalProducts",productPage.getTotalElements());
        long availableCount = service.countAvailableProducts();
        model.addAttribute("availableProducts",availableCount);
        model.addAttribute("criteria",criteria);
        model.addAttribute("canCreate", authService.hasPermission("PRODUCT_CREATE"));
        model.addAttribute("canUpdate",authService.hasPermission("PRODUCT_UPDATE"));
        model.addAttribute("canDelete",authService.hasPermission("PRODUCT_DELETE"));

        // Select-box to'lishi uchun hamma kategoriyalarni olib kelamiz
        model.addAttribute("categories",categoryService.getAllForSelect());

        return "product/list";
    }

    @GetMapping("/create")
    @PreAuthorize("@auth.hasPermission('PRODUCT_CREATE')")
    public String createPage(Model model) {
        // 1. Yangi mahsulot uchun bo'sh DTO
        model.addAttribute("productDto", new ProductCreateDto());

        // 2. Barcha mavjud kategoriyalar ro'yxati (Dropdown uchun)
        // CategoryCriteria bo'sh bo'lsa, servis hamma kategoriyalarni qaytaradi
        model.addAttribute("categories", categoryService.getAllForSelect());

        return "product/create";
    }

    @PostMapping("/create")
    @PreAuthorize("@auth.hasPermission('PRODUCT_CREATE')")
    public String create(@Valid @ModelAttribute("productDto") ProductCreateDto dto,
                         BindingResult bindingResult,
                         Model model){
        if (bindingResult.hasErrors()) {
            // Xatolik bo'lsa, kategoriyalarni qayta yuklab formaga qaytaramiz
            model.addAttribute("categories", categoryService.getAllForSelect());
            return "product/create";
        }
        service.create(dto);
        return "redirect:/products";
    }

    @GetMapping("/update/{id}")
    @PreAuthorize("@auth.hasPermission('PRODUCT_UPDATE')")
    public String updatePage(@PathVariable UUID id,Model model){
        ProductDto productDto = service.get(id);
        model.addAttribute("productDto",productDto);
        model.addAttribute("categories",categoryService.getAllForSelect());
        return "product/update";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("@auth.hasPermission('PRODUCT_UPDATE')")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("productDto") ProductUpdateDto dto,
                         BindingResult bindingResult,
                         Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllForSelect());
            return "product/update";
        }
        service.update(dto, id);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@auth.hasPermission('PRODUCT_DELETE')")
    public String delete(@PathVariable UUID id){
        service.delete(id);
        return "redirect:/products";
    }
}
