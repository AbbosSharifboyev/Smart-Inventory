package uz.pdp.smartinventory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import uz.pdp.smartinventory.model.domain.Categories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends BaseRepository<Categories, UUID> {

    // Kategoriya nomi bazada borligini tekshirish (Unique check)
    boolean existsByNameAndDeletedFalse(String name);

    boolean existsByIdAndDeletedFalse(UUID id);

    boolean existsByNameAndIdNotAndDeletedFalse(String name, UUID id);

    // Nomiga ko'ra qidirish (Faqat o'chirilmaganlarini)
    Optional<Categories> findByNameAndDeletedFalse(String name);

    // Qidiruv (Masalan, nomi bo'yicha qisman qidirish)
    List<Categories> findAllByDeletedFalse();

    // findById o'rniga ishlatish uchun
    Optional<Categories> findByIdAndDeletedFalse(UUID id);

    long countByDeletedFalse();

    // 1. Hammasini sahifalab olish (Hech qanday filtrsiz)
    Page<Categories> findAllByDeletedFalse(Pageable pageable);

    // 2. Nomi bo'yicha qidirib, natijani sahifalab olish
    Page<Categories> findAllByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
}
