package uz.pdp.smartinventory.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.catalina.LifecycleState;
import uz.pdp.smartinventory.model.domain.Permission;
import uz.pdp.smartinventory.model.enums.Role;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "Username bo'sh bo'lishi mumkin emas")
    private String username;

    @NotBlank(message = "Parol bo'sh bo'lishi mumkin emas")
    @Size(min = 6, message = "Parol kamida 6 ta belgidan iborat bo'lishi kerak")
    private String password;

    private String fullName;

    @Email(message = "Email formati noto'g'ri")
    @NotBlank(message = "Email bo'sh bo'lishi mumkin emas")
    private String email;

    private Role role;
    private List<UUID> permissionIds;
}