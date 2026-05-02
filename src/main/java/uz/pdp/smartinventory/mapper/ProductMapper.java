package uz.pdp.smartinventory.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.pdp.smartinventory.model.domain.Products;
import uz.pdp.smartinventory.model.dto.ProductCreateDto;
import uz.pdp.smartinventory.model.dto.ProductDto;
import uz.pdp.smartinventory.model.dto.ProductUpdateDto;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // 1. DTO dan Entity ga (Create paytida)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    // BU YERDA category.name KERAK EMAS, chunki create paytida faqat ID yetarli
    Products toEntity(ProductCreateDto dto);

    // 2. Entity dan DTO ga (List yoki Get paytida)
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryId", source = "category.id")
    ProductDto toDto(Products entity);

    // 3. Update paytida
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    void updateEntity(ProductUpdateDto dto, @MappingTarget Products entity);
}