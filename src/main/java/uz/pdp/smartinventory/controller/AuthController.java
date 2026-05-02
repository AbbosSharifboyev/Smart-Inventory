package uz.pdp.smartinventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uz.pdp.smartinventory.model.dto.UserCreateDto;
import uz.pdp.smartinventory.service.UserService;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(){
        return "auth/login";
    }

    @GetMapping("/logout")
    public String  logoutPage(){
        return "auth/logout";
    }

    @GetMapping("register")
    public String registerPage(Model model){
        model.addAttribute("userDto",new UserCreateDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userDto") UserCreateDto dto,
                           BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "auth/register";
        }
        userService.create(dto);
        return "redirect:/auth/login";
    }
}
