package uz.pdp.smartinventory.model.dto;

import lombok.Getter;
import lombok.Setter;
import uz.pdp.smartinventory.model.enums.OrderStatus;

@Getter
@Setter
public class OrderUpdateDto {
    private OrderStatus status;

    /*// 2. Izoh (Comment/Note)
    // Nega status o'zgardi? (Masalan: "Mijoz telefonni ko'tarmadi" yoki "Bekor qilindi")
    private String note;

    // 3. Yetkazib berish manzili (Agar mijoz oxirgi soniyada manzilni o'zgartirsa)
    private String shippingAddress;

    // 4. To'lov holati (Agar buyurtma to'langan bo'lsa)
    private Boolean isPaid;

    // 5. Yetkazib beruvchi (Courier ID)
    private UUID deliveryPersonId;*/
}
