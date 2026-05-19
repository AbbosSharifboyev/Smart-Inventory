package uz.pdp.smartinventory.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.smartinventory.model.domain.Orders;
import uz.pdp.smartinventory.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends BaseRepository<Orders, UUID> {

    // Foydalanuvchining o'chirilmagan buyurtmalarini olish
    List<Orders> findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);

    // N+1 muammosini oldini olish uchun (Foydalanuvchi va mahsulotlarni birga yuklash)
    @EntityGraph(attributePaths = {"user","items","items.product"})
    Optional<Orders> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT o FROM Orders o LEFT JOIN FETCH o.user WHERE o.deleted = false")
    List<Orders> findAllByDeletedFalse();

    long countByDeletedFalse();

    @Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.deleted = false and o.status = :status")
    BigDecimal getTotalRevenueByStatus(@Param("status") OrderStatus status);

    @Query("select sum(o.totalAmount) from Orders o where o.status = :status and o.createdAt >= :from and o.createdAt <= :to and o.deleted = false")
    BigDecimal sumRevenueByPeriod(
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    long countByStatusAndDeletedFalse(OrderStatus orderStatus);

    long countByStatusInAndDeletedFalse(List<OrderStatus> statuses);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
