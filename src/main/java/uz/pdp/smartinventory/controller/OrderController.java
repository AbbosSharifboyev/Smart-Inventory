package uz.pdp.smartinventory.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.criteria.OrderCriteria;
import uz.pdp.smartinventory.model.dto.*;
import uz.pdp.smartinventory.model.enums.OrderStatus;
import uz.pdp.smartinventory.service.OrderService;
import uz.pdp.smartinventory.service.ProductService;
import uz.pdp.smartinventory.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAll(OrderCriteria criteria){

        Page<OrderDto> orderPage = orderService.getAll(criteria);
        return ResponseEntity.ok(orderPage);
    }

    // Buyurtmalar sahifasi tepasidagi statistika ko'rsatkichlari uchun
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(){

        Map<String, Long> stats = new HashMap<>();
        stats.put("activeCount", orderService.countByStatuses(List.of("NEW", "PROCESSING")));
        stats.put("completedCount", orderService.countByStatus("COMPLETED"));
        stats.put("cancelledCount", orderService.countByStatus("CANCELLED"));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable UUID id){
        return ResponseEntity.ok(orderService.get(id));
    }


    @PostMapping()
    public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderRequestDto dto){

        OrderDto savedDto = orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @Valid @RequestBody OrderUpdateDto dto){
        orderService.update(dto,id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
