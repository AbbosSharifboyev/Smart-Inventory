package uz.pdp.smartinventory.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.pdp.smartinventory.criteria.ProductCriteria;
import uz.pdp.smartinventory.mapper.ProductMapper;
import uz.pdp.smartinventory.model.domain.Products;
import uz.pdp.smartinventory.model.dto.ProductCreateDto;
import uz.pdp.smartinventory.model.dto.ProductDto;
import uz.pdp.smartinventory.model.dto.ProductUpdateDto;
import uz.pdp.smartinventory.repository.ProductRepository;
import uz.pdp.smartinventory.validator.ProductValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService extends AbstractService<
        ProductRepository,
        ProductMapper,
        ProductValidator>
                implements CRUDService<
        ProductCreateDto,
        ProductDto,
        ProductUpdateDto,
        UUID,
        ProductCriteria> {


    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository repository, ProductMapper mapper, ProductValidator validator, FileStorageService fileStorageService) {
        super(repository, mapper, validator);
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public ProductDto create(ProductCreateDto dto) {
        validator.validateCreate(dto);
        Products entity = mapper.toEntity(dto);
        // Agar rasm yuborilgan bo'lsa
        if (dto.getImage() != null && !dto.getImage().isEmpty()){
            String imageName = fileStorageService.saveImage(dto.getImage());
            entity.setImagePath(imageName);
        }
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ProductDto update(ProductUpdateDto dto, UUID id) {

        validator.validateUpdate(dto,id);
        Products existingProduct = findByIdOrThrow(id);
        mapper.updateEntity(dto,existingProduct);

        //Rasm bilan ishlash
        if (dto.getImage() != null && !dto.getImage().isEmpty()){
            // Agar eski rasm bo'lsa, uni o'chirib tashlaymiz
            if (existingProduct.getImagePath() != null){
                fileStorageService.deleteImage(existingProduct.getImagePath());
            }
            // Yangi rasmni saqlaymiz
            String newImageName = fileStorageService.saveImage(dto.getImage());
            existingProduct.setImagePath(newImageName);
        }
        return mapper.toDto(repository.save(existingProduct));
    }

    @Override
    public ProductDto get(UUID id) {
        return mapper.toDto(findByIdOrThrow(id));
    }

    @Override
    public Page<ProductDto> getAll(ProductCriteria criteria) {

        int page = (criteria.getPage() != null) ? criteria.getPage() : 0;
        int size = (criteria.getSize() != null) ? criteria.getSize() : 100;

        Pageable pageable = PageRequest.of(page, size);

        return repository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"));
            }
            if (criteria.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"),
                        criteria.getCategoryId()));
            }
            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"),
                        criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"),
                        criteria.getMaxPrice()));
            }
            predicates.add(cb.equal(root.get("deleted"), false));
            // tartiblash
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(mapper::toDto);
    }

    public List<ProductDto> getAllActive(){
        return repository.findAllByDeletedFalse().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public long countActiveProducts(){
        return repository.countByDeletedFalse();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Products product = findByIdOrThrow(id);
        product.setDeleted(true);
        repository.save(product);
    }


    public List<Products> getAllProducts() {
        return repository.findAllWithCategories();
    }



    private Products findByIdOrThrow(UUID id){
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi "+id));
    }

    public long countAvailableProducts() {
        return repository.countAvailableProducts();
    }

    public Page<ProductDto> getFilteredProducts(ProductCriteria criteria) {


        if (criteria.getSize() == null || criteria.getSize() != 12){
            criteria.setSize(12);
        }
        Sort sort = Sort.by("id").descending();
        if (criteria.getSort() != null && !criteria.getSort().isBlank()){
            sort = switch (criteria.getSort()){
                case "price_asc" -> Sort.by("price").ascending();
                case "price_desc" -> Sort.by("price").descending();
                case "name_asc" -> Sort.by("name").ascending();
                default -> sort;
            };
        }

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        Page<Products> products;
        if (criteria.getName() != null && !criteria.getName().isBlank()){
            products = repository.findAllByNameContainingIgnoreCaseAndDeletedFalse(criteria.getName(),pageable);
        }else {
            products = repository.findAllByDeletedFalse(pageable);
        }
        return products.map(mapper::toDto);
    }
}
