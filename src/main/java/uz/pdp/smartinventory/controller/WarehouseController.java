package uz.pdp.smartinventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.service.WarehouseService;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Map<String, Object>> getWarehouseDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false)   String search,
            @RequestParam(required = false)   String status){

        Map<String, Object> data = warehouseService.getWarehouseDashboardData(page, search, status);

        return ResponseEntity.ok(data);
    }
}
