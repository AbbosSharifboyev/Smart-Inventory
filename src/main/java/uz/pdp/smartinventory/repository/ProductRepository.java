package uz.pdp.smartinventory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.pdp.smartinventory.model.domain.Products;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends BaseRepository<Products, UUID> {

    boolean existsByNameAndDeletedFalse(String name);


    Optional<Products> findByIdAndDeletedFalse(UUID id);

    boolean existsByNameAndIdNotAndDeletedFalse(String name, UUID id);

    List<Products> findAllByDeletedFalse();

    long countByQuantityLessThanAndDeletedFalse(Integer limit);

    @Query("SELECT p FROM Products p JOIN FETCH p.category WHERE p.deleted = false")
    List<Products> findAllWithCategories();

    long countByDeletedFalse();

    @Query("SELECT COUNT(p) FROM Products p WHERE p.quantity > 0 AND p.deleted = false")
    long countAvailableProducts();

    Page<Products> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Products> findAllByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Page<Products> findAllByDeletedFalse(Pageable pageable);

    List<Products> findAllByQuantityLessThanAndDeletedFalse(int threshold);
}
