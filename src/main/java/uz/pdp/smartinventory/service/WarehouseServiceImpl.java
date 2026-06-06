package uz.pdp.smartinventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.pdp.smartinventory.model.domain.Products;
import uz.pdp.smartinventory.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService{

    private static final int LOW_STOCK_THRESHOLD = 6;
    private static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;

    @Override
    public Map<String, Object> getWarehouseDashboardData(int page, String search, String status) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("name").ascending());
        Page<Products> productPage;


        if (search != null && !search.isBlank()) {
            productPage = productRepository.findAllByNameContainingIgnoreCaseAndDeletedFalse(search, pageable);
        } else {
            productPage = productRepository.findAllByDeletedFalse(pageable);
        }


        long totalProducts = productRepository.countByDeletedFalse();
        long availableProducts = productRepository.countAvailableProducts();
        long lowStockCount = productRepository.countByQuantityLessThanAndDeletedFalse(LOW_STOCK_THRESHOLD);
        long outOfStockCount = productRepository.countByQuantityAndDeletedFalse(0);

        BigDecimal totalValue = productRepository.calculateTotalValue();
        if (totalValue == null) totalValue = BigDecimal.ZERO;

        List<Products> lowStockProducts = productRepository.findAllByQuantityLessThanAndDeletedFalse(LOW_STOCK_THRESHOLD);

        // 📦 JSON uchun Map yig'ish
        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("pageSize", PAGE_SIZE);
        response.put("totalPages", productPage.getTotalPages());
        response.put("currentPage", page);
        response.put("totalElements", productPage.getTotalElements());

        response.put("totalProducts", totalProducts);
        response.put("availableProducts", availableProducts);
        response.put("lowStockCount", lowStockCount);
        response.put("outOfStockCount", outOfStockCount);
        response.put("totalValue", totalValue);
        response.put("lowStockProducts", lowStockProducts);
        response.put("lowStockThreshold", LOW_STOCK_THRESHOLD);
        response.put("search", search);
        response.put("status", status);

        return response;
    }
}
