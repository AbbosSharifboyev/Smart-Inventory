package uz.pdp.smartinventory.repository;

import org.springframework.stereotype.Repository;
import uz.pdp.smartinventory.model.domain.Permission;

import java.util.UUID;

@Repository
public interface PermissionRepository extends BaseRepository<Permission, UUID> {

}
