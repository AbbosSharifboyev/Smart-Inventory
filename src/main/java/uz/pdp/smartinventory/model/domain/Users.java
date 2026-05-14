package uz.pdp.smartinventory.model.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.smartinventory.model.domain.base.BaseEntity;
import uz.pdp.smartinventory.model.enums.Role;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Users extends BaseEntity {

    @Column(unique = true,nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    @Column(unique = true, nullable = false)
    @Email(message = "Email formati noto`g`ri") // Spring Boot Validation
    @NotBlank(message = "Email bo`sh bo`lishi mumkin emas")
    private String email;

    private boolean enabled = true; // akkaunt holati spring security uchun
    private LocalDateTime lastLogin;
}
