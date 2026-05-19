package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Getter
@Setter
public class StockMovementReportDto {

    private Page<StockMovementDto> movementPage;
    private long kirimCount;
    private long chiqimCount;
    private BigDecimal totalKirimSum;
}
