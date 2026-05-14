package uz.pdp.smartinventory.model.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.smartinventory.model.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {

    private String fullName;

    private String username;

    @Email(message = "Email formati noto'g'ri")
    private String email;

    private Role role;
    private Boolean enabled;

    // Admin foydalanuvchining huquqlarini o'zgartirishi uchun IDlar ro'yxati
    private List<UUID> permissionIds;
}