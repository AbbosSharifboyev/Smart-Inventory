package uz.pdp.smartinventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.pdp.smartinventory.model.domain.Orders;
import uz.pdp.smartinventory.model.dto.StockMovementDto;
import uz.pdp.smartinventory.model.dto.StockMovementReportDto;
import uz.pdp.smartinventory.model.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface StockMovementService {

    void createMovement(UUID productId, Integer quantity, MovementType type, String reason, Orders order, BigDecimal priceAtMovement);

    Page<StockMovementDto> getFilteredMovements(MovementType type, String product, LocalDate from, LocalDate to, Pageable pageable);

    StockMovementReportDto getMovementReport(LocalDate from, LocalDate to, Pageable pageable);
}
