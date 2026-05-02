package uz.pdp.smartinventory.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final CustomSecurityService authService;

    @ModelAttribute
    public void addPermissionsToModel(Model model){
        model.addAttribute("canReadProducts",authService.hasPermission("PRODUCT_READ"));
        model.addAttribute("canReadCategories",authService.hasPermission("CATEGORY_READ"));
    }
}
