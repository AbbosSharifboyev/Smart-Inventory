package uz.pdp.smartinventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.model.dto.StockMovementCreateDto;
import uz.pdp.smartinventory.model.dto.StockMovementDto;
import uz.pdp.smartinventory.model.enums.MovementType;
import uz.pdp.smartinventory.service.ProductService;
import uz.pdp.smartinventory.service.StockMovementService;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final ProductService productService;

    @GetMapping()
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Page<StockMovementDto>> listMovements(
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        Page<StockMovementDto> movementPage =
                stockMovementService.getFilteredMovements(type, product, from, to ,pageable);

        return ResponseEntity.ok(movementPage);
    }

    //  Omborga qo`lda mahsulot kiritish
    @PostMapping("/in")
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> manualStockIn(@Valid @RequestBody StockMovementCreateDto dto){

            stockMovementService.createMovement(
                    dto.getProductId(),
                    dto.getQuantity(),
                    MovementType.IN,
                    "Qo`lda kiritildi: " + dto.getReason(),
                    null,
                    null
            );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
