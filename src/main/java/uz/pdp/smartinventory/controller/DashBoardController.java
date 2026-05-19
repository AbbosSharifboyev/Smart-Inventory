package uz.pdp.smartinventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uz.pdp.smartinventory.model.domain.ActionLog;
import uz.pdp.smartinventory.repository.OrderRepository;
import uz.pdp.smartinventory.repository.ProductRepository;
import uz.pdp.smartinventory.service.ActionLogService;
import uz.pdp.smartinventory.service.OrderServiceImpl;
import uz.pdp.smartinventory.service.ProductService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashBoardController {

    private final OrderServiceImpl orderService;
    private final ProductService productService;
    private final ActionLogService actionLogService;

    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String index(Model model){

        List<ActionLog> activities = actionLogService.getRecentActivities();

        model.addAttribute("activities",activities !=null ? activities : new ArrayList<>());
        model.addAttribute("totalOrders",orderService.countByDeletedFalse());
        model.addAttribute("totalRevenue",orderService.getTotalRevenue());
        model.addAttribute("lowStock",productService.getLowStockProducts());

        return "dashboard";

    }
}
