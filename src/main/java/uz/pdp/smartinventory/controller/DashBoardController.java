package uz.pdp.smartinventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uz.pdp.smartinventory.repository.OrderRepository;
import uz.pdp.smartinventory.repository.ProductRepository;

import java.math.BigDecimal;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashBoardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @GetMapping()
    public String index(Model model){

        model.addAttribute("totalOrders",orderRepository.countByDeletedFalse());
        model.addAttribute("totalRevenue",orderRepository.getTotalRevenue());
        model.addAttribute("lowStock",productRepository.countByQuantityLessThanAndDeletedFalse(6));

        return "dashboard";

    }
}
