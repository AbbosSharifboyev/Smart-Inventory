package uz.pdp.smartinventory.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.smartinventory.model.domain.Orders;
import uz.pdp.smartinventory.model.dto.ProductCreateDto;
import uz.pdp.smartinventory.model.dto.ProductUpdateDto;
import uz.pdp.smartinventory.model.enums.OrderStatus;
import uz.pdp.smartinventory.repository.CategoryRepository;
import uz.pdp.smartinventory.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Create uchun
    public void validateCreate(ProductCreateDto dto) {
        checkPrice(dto.getPrice());
        checkQuantity(dto.getQuantity());

        if (productRepository.existsByNameAndDeletedFalse(dto.getName())) {
            throw new RuntimeException("Bunday nomli mahsulot allaqachon mavjud!");
        }

        checkCategory(dto.getCategoryId());
    }

    // Update uchun
    public void validateUpdate(ProductUpdateDto dto, UUID id) {
        if (dto.getPrice() != null) checkPrice(dto.getPrice());
        if (dto.getQuantity() != null) checkQuantity(dto.getQuantity()); // Miqdor kelgan bo'lsa tekshiramiz


        if (dto.getName() != null) {
            // O'zidan boshqa shu nomli mahsulot borligini tekshirish
            boolean exists = productRepository.existsByNameAndIdNotAndDeletedFalse(dto.getName(), id);
            if (exists) {
                throw new RuntimeException("Bu nom boshqa mahsulotga band qilingan!");
            }
        }

        if (dto.getCategoryId() != null) checkCategory(dto.getCategoryId());
    }




    // --- Yordamchi metodlar (Private helpers) ---

    private void checkPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Mahsulot narxi manfiy bo'lishi mumkin emas!");
        }
    }

    private void checkQuantity(Integer quantity) {
        // Miqdor null bo'lmasligi va 0 dan kichik bo'lmasligi kerak
        if (quantity == null || quantity < 0) {
            throw new RuntimeException("Mahsulot miqdori xato kiritildi (manfiy bo'lishi mumkin emas)!");
        }
    }

    private void checkCategory(UUID categoryId) {
        if (categoryId == null || !categoryRepository.existsByIdAndDeletedFalse(categoryId)) {
            throw new RuntimeException("Tanlangan kategoriya bazada topilmadi!");
        }
    }
}