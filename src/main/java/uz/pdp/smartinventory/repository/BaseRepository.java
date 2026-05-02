package uz.pdp.smartinventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import uz.pdp.smartinventory.model.domain.base.IdEntity;

import java.io.Serializable;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<E extends IdEntity, K extends Serializable>
                    extends JpaRepository<E, K>, JpaSpecificationExecutor<E> {

    Optional<E> findByIdAndDeletedFalse(K id);
}
