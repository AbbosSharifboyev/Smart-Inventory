package uz.pdp.smartinventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.smartinventory.model.dto.StockMovementCreateDto;
import uz.pdp.smartinventory.model.dto.StockMovementDto;
import uz.pdp.smartinventory.model.enums.MovementType;
import uz.pdp.smartinventory.service.ProductService;
import uz.pdp.smartinventory.service.StockMovementService;

import java.time.LocalDate;


@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final ProductService productService;

    @GetMapping()
    public String listMovements(
            Model model,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate to,
            @PageableDefault(sort = "createdAt",
                                        direction = Sort.Direction.DESC) Pageable pageable){
        Page<StockMovementDto> movementPage =
                stockMovementService.getFilteredMovements(type, product, from, to ,pageable);

        model.addAttribute("movements",movementPage.getContent());
        model.addAttribute("movementPage",movementPage);

        // Qo'lda kirim qilish formasi uchun mahsulotlar ro'yxati
        model.addAttribute("products",productService.getAllActive());
        model.addAttribute("activePage","transactions"); // Menu aktivligi uchun

        return "transaction/list";
    }

    //  Omborga qo`lda mahsulot kiritish
    @PostMapping("/in")
    public String manualStockIn(@ModelAttribute @Valid StockMovementCreateDto dto,
                                RedirectAttributes redirectAttributes){
        try {
            stockMovementService.createMovement(
                    dto.getProductId(),
                    dto.getQuantity(),
                    MovementType.IN,
                    "Qo`lda kiritildi: " + dto.getReason(),
                    null,
                    null
            );
            redirectAttributes.addFlashAttribute("successMessage", "Mahsulot muvaffaqiyatli kirim qilindi!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xatolik yuz berdi: " + e.getMessage());
        }
        return "redirect:/transactions";
    }
}
