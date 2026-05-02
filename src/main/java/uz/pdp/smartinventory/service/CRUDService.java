package uz.pdp.smartinventory.service;

import org.springframework.data.domain.Page;
import uz.pdp.smartinventory.criteria.BaseCriteria;
import uz.pdp.smartinventory.model.dto.BaseDto;

import java.io.Serializable;
import java.util.List;

/**
 * @param <CD> create qilishda keladigan (Create Dto)
 * @param <D> dto
 * @param <UD> update qilishda keladigan (Update Dto)
 * @param <K> key (id)
 * @param <C> kriteria
 */
public interface CRUDService<
        CD ,
        D extends BaseDto,
        UD ,
        K extends Serializable,
        C extends BaseCriteria> {

    /**
     * @param dto
     * @return
     */

    D create(CD dto);

    D update(UD dto, K id);

    D get(K id);

    Page<D> getAll(C criteria);

    void delete(K id);
}
