package uz.pdp.smartinventory.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.smartinventory.model.domain.Orders;
import uz.pdp.smartinventory.model.domain.Products;
import uz.pdp.smartinventory.model.dto.OrderItemRequestDto;
import uz.pdp.smartinventory.model.dto.OrderRequestDto;
import uz.pdp.smartinventory.model.dto.OrderUpdateDto;
import uz.pdp.smartinventory.model.enums.OrderStatus;
import uz.pdp.smartinventory.repository.ProductRepository;


@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final ProductRepository productRepository;

    public void validateCreate(OrderRequestDto dto){

        if (dto.getItems() == null || dto.getItems().isEmpty()){
            throw new RuntimeException("Savat bo'sh bo'lishi mumkin emas!");
        }

        for (OrderItemRequestDto item : dto.getItems()) {

            if (item.getCount() == null || item.getCount() <= 0){
                throw new RuntimeException("Mahsulot miqdori 1 tadan kam bo'lishi mumkin emas!");
            }

            Products product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi: " + item.getProductId()));

            if (product.getQuantity() < item.getCount()){
                throw new RuntimeException("Omborda mahsulot yetarli emas: " + product.getName()
                                          + " (Qoldiq: " + product.getQuantity() + ")");
            }
        }
    }

    public void validateUpdate(OrderUpdateDto dto, Orders existingOrder){

        if (existingOrder.getStatus() == OrderStatus.CANCELLED){
            throw new RuntimeException("Bekor qilingan buyurtmani o'zgartirib bo'lmaydi!");
        }

        if (existingOrder.getStatus() == OrderStatus.COMPLETED){
            // Agar status kelgan bo'lsa va u COMPLETED dan farqli bo'lsa
            if (dto.getStatus() != null && dto.getStatus() != OrderStatus.COMPLETED){
                throw new RuntimeException("Yakunlangan buyurtma statusini o'zgartirish taqiqlanadi!");
            }
            // Agar buyurtma tarkibi (mahsulot soni yoki narxi) o'zgartirilmoqchi bo'lsa
            // Bu joyda dto ichidagi boshqa maydonlarni ham tekshirib qo'yish kerak
            throw new RuntimeException("Yakunlangan buyurtma ma'lumotlarini (tarkibi, narxi, mijoz) tahrirlash mumkin emas!");
        }

        //Statuslar o'tishi (O'rinli o'tishlar zanjiri)
        // Masalan: NEW -> PROCESSING -> COMPLETED (To'g'ri)
        // Lekin: COMPLETED -> NEW (Xato)
        if (existingOrder.getStatus() == OrderStatus.PROCESSING && dto.getStatus() == OrderStatus.NEW) {
            throw new RuntimeException("Jarayondagi buyurtmani qaytadan 'Yangi' holatiga o'tkazib bo'lmaydi!");
        }
    }

}
