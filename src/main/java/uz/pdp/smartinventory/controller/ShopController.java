package uz.pdp.smartinventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uz.pdp.smartinventory.config.MyUserDetails;
import uz.pdp.smartinventory.criteria.ProductCriteria;
import uz.pdp.smartinventory.model.dto.OrderDto;
import uz.pdp.smartinventory.model.dto.OrderRequestDto;

import uz.pdp.smartinventory.model.dto.ProductDto;
import uz.pdp.smartinventory.service.CartService;
import uz.pdp.smartinventory.service.OrderService;
import uz.pdp.smartinventory.service.ProductService;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping("/products")
    public ResponseEntity<Page<ProductDto>> getShopProducts(ProductCriteria criteria) {

        Page<ProductDto> productPage = productService.getFilteredProducts(criteria);
        return ResponseEntity.ok(productPage);
    }


    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> completeOrder(
            @Valid @RequestBody OrderRequestDto dto,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        // Agar dto ichida userId frontenddan kelmasa, tizimga kirgan userni ID sini beramiz
        if (dto.getUserId() == null && userDetails != null){
            dto.setUserId(userDetails.getId());
        }

        OrderDto savedOrder = orderService.create(dto);
        return ResponseEntity.ok(savedOrder);
    }
}