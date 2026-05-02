package uz.pdp.smartinventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.smartinventory.model.domain.Permission;
import uz.pdp.smartinventory.model.enums.Role;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto extends BaseDto {

    private String username;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
    private Set<Permission> permissions;
}