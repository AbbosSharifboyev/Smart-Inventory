package uz.pdp.smartinventory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.smartinventory.model.domain.StockMovement;
import uz.pdp.smartinventory.model.enums.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends BaseRepository<StockMovement, UUID> {


    @EntityGraph(attributePaths = {"product"})
    @Query(value = """
            select s from StockMovement s
            join s.product p
            where (cast(:type as string )  is null or s.type = :type)
              and (:product is null or lower(p.name) like lower(concat('%', cast(:product as string), '%')))
              and (cast(:from as timestamp ) is null or s.createdAt >= :from)
              and (cast(:to as timestamp ) is null or s.createdAt <= :to)
            order by s.createdAt desc
            """,
            countQuery = """
            select count(s) from StockMovement s
            join s.product p
            where (cast(:type as string ) is null or s.type = :type)
              and (:product is null or lower(p.name) like lower(concat('%', cast(:product as string), '%')))
              and (cast(:from as timestamp ) is null or s.createdAt >= :from)
              and (cast(:to as timestamp ) is null or s.createdAt <= :to)
            """
    )
    Page<StockMovement> findFiltered(
             @Param("type")     MovementType type,
             @Param("product")  String product,
             @Param("from")     LocalDateTime from,
             @Param("to")       LocalDateTime to,
             Pageable pageable
    );
}
