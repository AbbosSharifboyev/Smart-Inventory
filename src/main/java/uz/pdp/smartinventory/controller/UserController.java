package uz.pdp.smartinventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.config.CustomSecurityService;
import uz.pdp.smartinventory.config.MyUserDetails;
import uz.pdp.smartinventory.criteria.UserCriteria;
import uz.pdp.smartinventory.model.dto.PasswordChangeDto;
import uz.pdp.smartinventory.model.dto.UserCreateDto;
import uz.pdp.smartinventory.model.dto.UserDto;
import uz.pdp.smartinventory.model.dto.UserUpdateDto;
import uz.pdp.smartinventory.repository.PermissionRepository;
import uz.pdp.smartinventory.service.UserService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final PermissionRepository permissionRepository;
    private final CustomSecurityService auth;


    //Admin yangi user yaratish sahifasi
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model){
        model.addAttribute("userDto",new UserCreateDto());
        model.addAttribute("allPermissions",permissionRepository.findAll());
        return "user/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("userDto") UserCreateDto dto,
                         BindingResult bindingResult,
                         Model model){
        if (bindingResult.hasErrors()){
            model.addAttribute("allPermissions",permissionRepository.findAll());
            return "user/create";
        }
        service.create(dto);
        return "redirect:/users";
    }

    //Foydalanuvchilar ro'yxati (Admin uchun)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listPage(@ModelAttribute("criteria")UserCriteria criteria, Model model){

        Page<UserDto> users = service.getAll(criteria);

        model.addAttribute("users",users.getContent());
        model.addAttribute("page",users);
        model.addAttribute("criteria",criteria);

        model.addAttribute("totalCount", users.getTotalElements());
        model.addAttribute("activeCount", service.countActiveUsers());
        model.addAttribute("adminCount", service.countUsersByRole("ADMIN"));
        model.addAttribute("blockedCount", service.countBlockedUsers());
        model.addAttribute("canUserManage",auth.hasPermission("USER_MANAGE"));
        return "user/list";
    }

    //Foydalanuvchini tahrirlash (Admin uchun)
    @GetMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateForm(@PathVariable UUID id, Model model){
        model.addAttribute("userDto",service.get(id));
        model.addAttribute("allPermissions", permissionRepository.findAll());
        return "user/update";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("userDto") UserUpdateDto dto,
                         BindingResult bindingResult,
                         Model model){

        System.out.println("Kelgan status: " + dto.isEnabled());
        if (bindingResult.hasErrors()){
            model.addAttribute("allPermissions",permissionRepository.findAll());
            return "user/update";
        }
        service.update(dto,id);
        return "redirect:/users";
    }


    //Shaxsiy profillarni tahrirlash(har bir user uzi uchun)
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(@AuthenticationPrincipal MyUserDetails userDetails, Model model){

        UUID currentUserId = userDetails.getId();
        UserDto userDto = service.get(currentUserId);
        model.addAttribute("user",userDto);
        return "user/profile";
    }

    @PostMapping("/profile/update")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@AuthenticationPrincipal MyUserDetails userDetails,
                                @Valid @ModelAttribute("user") UserUpdateDto dto,
                                BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "user/profile";
        }
        service.updateProfile(userDetails.getId(),dto);
        return "redirect:/users/profile?success";
    }

    //Parolni uzgartirish
    @GetMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public String changePasswordPage(Model model){
        model.addAttribute("passwordDto",new PasswordChangeDto());
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal MyUserDetails userDetails,
                                 @Valid @ModelAttribute("passwordDto") PasswordChangeDto dto,
                                 BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "user/change-password";
        }
        service.changePassword(userDetails.getId(),dto);
        return "redirect:/users/profile?success";
    }

    //Admin tomonidan parolni reset qilish
    @PostMapping("/reset-password/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String resetPassword(@PathVariable UUID id,@RequestParam("newPassword") String newPassword){
        service.resetPasswordByAdmin(id,newPassword);
        return "redirect:/users/update/" + id + "?resetSuccess";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable UUID id){
        service.delete(id);
        return "redirect:/users";
    }
}
