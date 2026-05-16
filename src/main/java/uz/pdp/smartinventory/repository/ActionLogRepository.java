package uz.pdp.smartinventory.repository;

import org.springframework.stereotype.Repository;
import uz.pdp.smartinventory.model.domain.ActionLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionLogRepository extends BaseRepository<ActionLog, UUID> {

    List<ActionLog> findTop5ByOrderByCreatedAtDesc();
}
