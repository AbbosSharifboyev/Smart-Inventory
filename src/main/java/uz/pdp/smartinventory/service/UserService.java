package uz.pdp.smartinventory.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.pdp.smartinventory.config.MyUserDetails;
import uz.pdp.smartinventory.criteria.UserCriteria;
import uz.pdp.smartinventory.mapper.UserMapper;
import uz.pdp.smartinventory.model.domain.Permission;
import uz.pdp.smartinventory.model.domain.Users;
import uz.pdp.smartinventory.model.dto.PasswordChangeDto;
import uz.pdp.smartinventory.model.dto.UserCreateDto;
import uz.pdp.smartinventory.model.dto.UserDto;
import uz.pdp.smartinventory.model.dto.UserUpdateDto;
import uz.pdp.smartinventory.model.enums.Role;
import uz.pdp.smartinventory.repository.PermissionRepository;
import uz.pdp.smartinventory.repository.UserRepository;
import uz.pdp.smartinventory.validator.UserValidator;


import java.util.*;


@Service
public class UserService
        extends AbstractService<UserRepository,
                                UserMapper,
                                UserValidator>
        implements CRUDService<
                   UserCreateDto,
                   UserDto,
                   UserUpdateDto,
                   UUID,
                   UserCriteria>
        , UserDetailsService {

    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    public UserService(UserRepository repository,
                          UserMapper mapper,
                          UserValidator validator,
                          PermissionRepository permissionRepository,
                          PasswordEncoder passwordEncoder,
                          CacheManager cacheManager) {
        super(repository, mapper, validator);
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
    }


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users authUser = repository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("Foydalanuvchi topilmadi: " + username));

        return mapper.toUserDetails(authUser);
    }

    @Override
    public UserDto create(UserCreateDto dto) {

        validator.validateOnCreate(dto);
        Users user = mapper.fromCreateDto(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        user.setEnabled(true);

        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()){
            List<Permission> permissions = permissionRepository.findAllById(dto.getPermissionIds());
            user.setPermissions(new HashSet<>(permissions));
        }
        Users savedUser = repository.save(user);
        return mapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(UserUpdateDto dto, UUID userId) {

        Users user = validator.existAndGet(userId);

        validator.validateOnUpdate(dto, userId);

        mapper.updateFromDto(dto, user);

        user.setEnabled(dto.getEnabled() != null && dto.getEnabled());

        if (dto.getPermissionIds() != null){
            List<Permission> permissions = permissionRepository.findAllById(dto.getPermissionIds());
            user.getPermissions().clear();
            user.getPermissions().addAll(permissions);
        }
        Users saved = repository.save(user);

        clearUserPermissionCache(saved.getUsername());

        return mapper.toDto(saved);
    }

    private void clearUserPermissionCache(String username) {
        Cache cache = cacheManager.getCache("permissions");
        if (cache != null){
            permissionRepository.findAll()
                    .stream()
                    .map(Permission::getName)
                    .forEach(p -> cache.evict(username + "_" + p));
        }
    }

    public UserUpdateDto getForUpdate(UUID id){
        UserDto userDto = get(id);
        return mapper.toUpdateDto(userDto);
    }

    @Transactional
    public void changePassword(UUID userId, PasswordChangeDto dto){

        Users user = validator.existAndGet(userId);
        validator.validateChangePassword(user,dto);

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        repository.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void resetPasswordByAdmin(UUID userId, String newPassword){

        Users user = validator.existAndGet(userId);

        validator.validateChangePasswordByAdmin(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Transactional
    public UserDto updateProfile(UUID userId,UserUpdateDto dto){

        Users user = validator.existAndGet(userId);
        validator.validateOnUpdate(dto,userId);

        mapper.updateProfileFromDto(dto,user);

        return mapper.toDto(repository.save(user));
    }

    @Override
    public UserDto get(UUID userId) {

        Users user = validator.existAndGet(userId);
        return mapper.toDto(user);
    }

    @Override
    public Page<UserDto> getAll(UserCriteria criteria) {

        int page = (criteria.getPage() != null) ? criteria.getPage() : 0;
        int size = (criteria.getSize() != null) ? criteria.getSize() : 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return repository.findAll((root, query, cb) ->{

            List<Predicate> predicates = new ArrayList<>();
            //Umumiy qidiruv
            if (StringUtils.hasText(criteria.getSearch())){
                String searchPattern = "%" + criteria.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")),searchPattern),
                        cb.like(cb.lower(root.get("username")),searchPattern),
                        cb.like(cb.lower(root.get("email")),searchPattern)
                ));
            }
            //Aniq maydonlar buyicha qidiruv
            if (StringUtils.hasText(criteria.getUsername())){
                predicates.add(cb.like(cb.lower(root.get("username")),
                        "%" + criteria.getUsername().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(criteria.getEmail())){
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + criteria.getEmail().toLowerCase() + "%"));
            }
            if (criteria.getRole() != null){
                predicates.add(cb.equal(root.get("role"), criteria.getRole()));
            }
            if (criteria.getEnabled() != null){
                predicates.add(cb.equal(root.get("enabled"),criteria.getEnabled()));
            }
            predicates.add(cb.equal(root.get("deleted"),false));

            return cb.and(predicates.toArray(new Predicate[0]));

        }, pageable).map(mapper::toDto);
    }

    public List<UserDto> getAllUsers(){
        return repository.findAllByDeletedFalse().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public void delete(UUID userId) {
        Users user = validator.existAndGet(userId);
        user.setDeleted(true);
        user.setEnabled(false);

        //Shu username orqali yangi user ochishda muammo bulmasligi uchun
        user.setUsername(user.getUsername() + "_deleted_" + UUID.randomUUID());

        repository.save(user);
    }

    public long countActiveUsers() {
        return repository.countByEnabledTrue();
    }


    public long countBlockedUsers() {
        return repository.countByEnabledFalse();
    }


    public long countUsersByRole(String roleName) {
        return repository.countByRole(Role.valueOf(roleName));
    }
}
