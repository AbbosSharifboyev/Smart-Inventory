package uz.pdp.smartinventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.smartinventory.model.domain.ActionLog;
import uz.pdp.smartinventory.service.ActionLogService;
import uz.pdp.smartinventory.service.OrderServiceImpl;
import uz.pdp.smartinventory.service.ProductService;

import java.util.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class DashBoardController {

    private final OrderServiceImpl orderService;
    private final ProductService productService;
    private final ActionLogService actionLogService;

    @GetMapping()
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Map<String, Object>> getDashboardData(){

        Map<String, Object> dashboardData = new HashMap<>();

        List<ActionLog> activities = actionLogService.getRecentActivities();
        dashboardData.put("activities", activities != null ? activities : List.of());

        dashboardData.put("totalOrders",orderService.countByDeletedFalse());
        dashboardData.put("totalRevenue",orderService.getTotalRevenue());
        dashboardData.put("lowStock",productService.getLowStockProducts());

        return ResponseEntity.ok(dashboardData);

    }
}
