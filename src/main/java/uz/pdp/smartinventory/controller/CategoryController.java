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
import uz.pdp.smartinventory.criteria.CategoryCriteria;
import uz.pdp.smartinventory.model.dto.CategoryCreateDto;
import uz.pdp.smartinventory.model.dto.CategoryDto;
import uz.pdp.smartinventory.model.dto.CategoryUpdateDto;
import uz.pdp.smartinventory.service.CategoryService;
import uz.pdp.smartinventory.service.ProductService;

import java.util.UUID;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CategoryService service;
    private final ProductService productService;
    private final CustomSecurityService authService;

    @GetMapping()
    @PreAuthorize("@auth.hasPermission('CATEGORY_READ')")
    public String getAll(Model model, @ModelAttribute("criteria") CategoryCriteria criteria){

        Page<CategoryDto> categoryPage = service.getAll(criteria);

        model.addAttribute("categories", categoryPage.getContent());

        //Jami sahifalar sonini uzatamiz (HTMLdagi pagination tugmalari uchun kerak)
        model.addAttribute("totalPages",categoryPage.getTotalPages());

        model.addAttribute("criteria",criteria);
        model.addAttribute("activeCatCount",service.countActiveCategories());
        model.addAttribute("totalProductCount",productService.countActiveProducts());

        model.addAttribute("canCreate", authService.hasPermission("CATEGORY_CREATE"));
        model.addAttribute("canUpdate",authService.hasPermission("CATEGORY_UPDATE"));
        model.addAttribute("canDelete",authService.hasPermission("CATEGORY_DELETE"));

        return "category/list";
    }

    @GetMapping("/create")
    @PreAuthorize("@auth.hasPermission('CATEGORY_CREATE')")
    public String createPage(Model model){
        model.addAttribute("categoryDto",new CategoryCreateDto());
        return "category/create";
    }

    @PostMapping("/create")
    @PreAuthorize("@auth.hasPermission('CATEGORY_CREATE')")
    public String create(@Valid @ModelAttribute("categoryDto")CategoryCreateDto dto,
                         BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "category/create";
        }
        service.create(dto);
        return "redirect:/categories";
    }

    @GetMapping("/update/{id}")
    @PreAuthorize("@auth.hasPermission('CATEGORY_UPDATE')")
    public String updatePage(@PathVariable UUID id,Model model){

        model.addAttribute("categoryDto",service.get(id));
        model.addAttribute("id",id);
        model.addAttribute("productCount",productService.countActiveProducts());
        return "category/update";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("@auth.hasPermission('CATEGORY_UPDATE')")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("categoryDto") CategoryUpdateDto dto,
                         BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()){
            model.addAttribute("id",id);
            return "category/update";
        }
        service.update(dto,id);
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("@auth.hasPermission('CATEGORY_DELETE')")
    public String delete(@PathVariable UUID id){
        service.delete(id);
        return "redirect:/categories";
    }
}
