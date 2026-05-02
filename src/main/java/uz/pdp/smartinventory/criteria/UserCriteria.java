package uz.pdp.smartinventory.criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.smartinventory.model.enums.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCriteria extends BaseCriteria {

    private String username;
    private String email;
    private Role role;
    private Boolean enabled;
}