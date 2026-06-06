package uz.pdp.smartinventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.config.CustomSecurityService;
import uz.pdp.smartinventory.config.MyUserDetails;
import uz.pdp.smartinventory.criteria.UserCriteria;
import uz.pdp.smartinventory.model.domain.Permission;
import uz.pdp.smartinventory.model.dto.*;
import uz.pdp.smartinventory.model.enums.PermissionEnum;
import uz.pdp.smartinventory.repository.PermissionRepository;
import uz.pdp.smartinventory.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService service;
    private final CustomSecurityService auth;
    private final PermissionRepository permissionRepository;


    @GetMapping("/permissions")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IdNameDto>> getAllPermissions(){

        List<Permission> permissionsFromDb = permissionRepository.findAll();

        List<IdNameDto> permissionList = permissionsFromDb.stream()
                .map(p -> new IdNameDto(
                        p.getId(),
                        p.getName()
                )).toList();

        return ResponseEntity.ok(permissionList);
    }

    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllPage(UserCriteria criteria){

        Page<UserDto> usersPage = service.getAll(criteria);
        return ResponseEntity.ok(usersPage);
    }

    @GetMapping("/stats")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats(){
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCount", service.countTotalUsers());
        stats.put("activeCount", service.countActiveUsers());
        stats.put("adminCount", service.countUsersByRole("ADMIN"));
        stats.put("blockedCount", service.countBlockedUsers());
        stats.put("canUserManage", auth.hasPermission("USER_MANAGE"));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id){
        UserDto userDto = service.get(id);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping()
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateDto dto){

        UserDto createdUser = service.create(dto);
        return new ResponseEntity<>(createdUser,HttpStatus.CREATED);
    }



    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> update(@PathVariable UUID id,
                                          @Valid @RequestBody UserUpdateDto dto) {
        UserDto updatedUser = service.update(dto, id);
        return ResponseEntity.ok(updatedUser);
    }


    //Shaxsiy profillarni tahrirlash(har bir user uzi uchun)
    @GetMapping("/profile")
    //@PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> profilePage(@AuthenticationPrincipal MyUserDetails userDetails){

        UUID currentUserId = userDetails.getId();
        UserDto userDto = service.get(currentUserId);
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/profile")
    //@PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateProfile(@AuthenticationPrincipal MyUserDetails userDetails,
                                                 @Valid @RequestBody UserUpdateDto dto){

        UserDto updatedProfile = service.updateProfile(userDetails.getId(), dto);
        return ResponseEntity.ok(updatedProfile);
    }



    @PatchMapping("/change-password")
    //@PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @Valid @RequestBody PasswordChangeDto dto){

        service.changePassword(userDetails.getId(),dto);
        return ResponseEntity.ok(Map.of("message","Parol muvaffaqiyatli o`zgartirildi"));
    }

    //Admin tomonidan parolni reset qilish
    @PatchMapping("/reset-password/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable UUID id,
            @RequestParam("newPassword") String newPassword){

        service.resetPasswordByAdmin(id,newPassword);
        return ResponseEntity.ok(Map.of("message", "Foydalanuvchi paroli admin tomonidan yangilandi"));
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
