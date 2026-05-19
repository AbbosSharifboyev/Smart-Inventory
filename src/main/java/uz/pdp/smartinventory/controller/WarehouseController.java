package uz.pdp.smartinventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uz.pdp.smartinventory.model.domain.Products;
import uz.pdp.smartinventory.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WarehouseController {

    private static final int LOW_STOCK_THRESHOLD = 6;
    private static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false)   String search,
            @RequestParam(required = false)   String status,
            Model model){

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("name").ascending());

        Page<Products> productPage;
        if (search != null && !search.isBlank()){
            productPage = productRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedFalse(search,pageable);
        }else {
            productPage = productRepository.findAllByDeletedFalse(pageable);
        }

        long totalProducts = productRepository.countByDeletedFalse();
        long availableProducts = productRepository.countAvailableProducts();
        long lowStockCount = productRepository.countByQuantityLessThanAndDeletedFalse(LOW_STOCK_THRESHOLD);
        long outOfStockCount = productRepository.countByQuantityAndDeletedFalse(0);

        BigDecimal totalValue = productRepository.calculateTotalValue();

        List<Products> lowStockProducts = productRepository
                .findAllByQuantityLessThanAndDeletedFalse(LOW_STOCK_THRESHOLD);

        model.addAttribute("products",        productPage.getContent());
        model.addAttribute("pageSize",        PAGE_SIZE);
        model.addAttribute("totalPages",      productPage.getTotalPages());
        model.addAttribute("currentPage",     page);
        model.addAttribute("totalElements",   productPage.getTotalElements());

        model.addAttribute("totalProducts",   totalProducts);
        model.addAttribute("availableProducts", availableProducts);
        model.addAttribute("lowStockCount",   lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("totalValue",      totalValue);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("lowStockThreshold", LOW_STOCK_THRESHOLD);

        model.addAttribute("search", search);
        model.addAttribute("status", status);

        return "warehouse";
    }
}
