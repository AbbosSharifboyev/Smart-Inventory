package uz.pdp.smartinventory.criteria;

import lombok.Getter;
import lombok.Setter;
import uz.pdp.smartinventory.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class OrderCriteria extends BaseCriteria{

    private String userFullName;
    private UUID userId;        // Ma'lum bir foydalanuvchi buyurtmalari
    private OrderStatus status; // Status bo'yicha filtr (masalan, faqat NEW)
    private LocalDate dateFrom; // Shu sanadan...
    private LocalDate dateTo;   // Shu sanagacha...
    private BigDecimal minAmount; // Minimal summa bo'yicha
}
