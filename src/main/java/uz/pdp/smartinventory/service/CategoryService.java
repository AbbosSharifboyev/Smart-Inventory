package uz.pdp.smartinventory.service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.smartinventory.criteria.CategoryCriteria;
import uz.pdp.smartinventory.mapper.CategoryMapper;
import uz.pdp.smartinventory.model.domain.Categories;
import uz.pdp.smartinventory.model.dto.CategoryCreateDto;
import uz.pdp.smartinventory.model.dto.CategoryDto;
import uz.pdp.smartinventory.model.dto.CategoryUpdateDto;
import uz.pdp.smartinventory.repository.CategoryRepository;
import uz.pdp.smartinventory.validator.CategoryValidator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService extends AbstractService<
        CategoryRepository,
        CategoryMapper,
        CategoryValidator>
        implements CRUDService<
        CategoryCreateDto,
        CategoryDto,
        CategoryUpdateDto,
        UUID,
        CategoryCriteria> {

    private final ActionLogService actionLogService;

    protected CategoryService(CategoryRepository repository, CategoryMapper mapper, CategoryValidator validator, ActionLogService actionLogService) {
        super(repository, mapper, validator);
        this.actionLogService = actionLogService;
    }



    @Override
    @Transactional
    public CategoryDto create(CategoryCreateDto dto) {
        validator.validateCreate(dto);
        Categories entity = mapper.toEntity(dto);
        CategoryDto savedDto = mapper.toDto(repository.save(entity));
        actionLogService.saveLog("Yangi kategoriya qo`shildi: " + entity.getName(), "INFO");
        return savedDto;
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryUpdateDto dto, UUID id) {
        validator.validateUpdate(dto,id);
        Categories existingCategory = findByIdOrThrow(id);
        mapper.updateEntity(dto,existingCategory);
        Categories savedCategory = repository.save(existingCategory);
        actionLogService.saveLog("Kategoriya ma'lumotlari yangilandi: " + savedCategory.getName(), "INFO");
        return mapper.toDto(savedCategory);
    }

    @Override
    public CategoryDto get(UUID id) {
        return mapper.toDto(findByIdOrThrow(id));
    }

    @Override
    public Page<CategoryDto> getAll(CategoryCriteria criteria) {

        // default list uchun
        int page = (criteria.getPage() != null) ? criteria.getPage() : 0;
        int size = (criteria.getSize() != null) ? criteria.getSize() : 100;
        Pageable pageable = PageRequest.of(page, size);

        Page<Categories> categoryPage;
        // 2. Qidiruv mantiqi (Bitta metod ichida)
        if (criteria.getName() != null && !criteria.getName().isBlank()){
            categoryPage = repository.findAllByNameContainingIgnoreCaseAndDeletedFalse(
                    criteria.getName(), pageable);
        }else {
            categoryPage = repository.findAllByDeletedFalse(pageable);
        }
        return categoryPage.map(mapper::toDto);
    }




    public long countActiveCategories(){
        return repository.countByDeletedFalse();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Categories category = findByIdOrThrow(id);
        category.setDeleted(true);
        repository.save(category);
    }

    private Categories findByIdOrThrow(UUID id){
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Kategoriya topilmadi " + id));
    }

    public List<CategoryDto> getAllForSelect() {
        return repository.findAllByDeletedFalse().stream().
                map(mapper::toDto).
                collect(Collectors.toList());
    }
}
