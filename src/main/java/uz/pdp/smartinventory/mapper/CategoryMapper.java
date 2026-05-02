package uz.pdp.smartinventory.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uz.pdp.smartinventory.model.domain.Categories;
import uz.pdp.smartinventory.model.dto.CategoryCreateDto;
import uz.pdp.smartinventory.model.dto.CategoryDto;
import uz.pdp.smartinventory.model.dto.CategoryUpdateDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Categories toEntity(CategoryCreateDto dto);
    CategoryDto toDto(Categories entity);
    void updateEntity(CategoryUpdateDto dto, @MappingTarget Categories entity);
}
