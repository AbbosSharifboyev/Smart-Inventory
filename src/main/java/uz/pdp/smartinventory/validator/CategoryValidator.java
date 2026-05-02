package uz.pdp.smartinventory.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.smartinventory.model.dto.CategoryCreateDto;
import uz.pdp.smartinventory.model.dto.CategoryUpdateDto;
import uz.pdp.smartinventory.repository.CategoryRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryValidator {

    private final CategoryRepository repository;

    public void validateCreate(CategoryCreateDto dto){
        if (dto.getName() == null || dto.getName().isBlank()){
            throw new RuntimeException("Kategoriya nomi bo`sh bo`lishi mumkin emas!");
        }
        if (repository.existsByNameAndDeletedFalse(dto.getName())){
            throw new RuntimeException("Bunday nomli kategoriya allaqachon mavjud!");
        }
    }

    public void validateUpdate(CategoryUpdateDto dto, UUID id){
        if (dto.getName() != null && !dto.getName().isBlank()){
            if (repository.existsByNameAndIdNotAndDeletedFalse(dto.getName(), id)){
                throw new RuntimeException("Bu nom boshqa kategoriya tomonidan band qilingan!");
            }
        }
    }
}
