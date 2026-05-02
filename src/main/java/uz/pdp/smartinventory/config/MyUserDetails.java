package uz.pdp.smartinventory.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.pdp.smartinventory.model.domain.Permission;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class MyUserDetails implements UserDetails {

    private UUID id;
    private String fullName;
    private String password;
    private String username;
    private String role;
    private boolean enabled;
    private Set<Permission> permissions;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        if (permissions != null) {
            for (Permission p : permissions) {
                authorities.add(new SimpleGrantedAuthority(p.getName()));
            }
        }
        return authorities;
    }

    @Override 
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
    return true;
    }

    @Override
    public boolean isEnabled(){
        return enabled;
    }
}
