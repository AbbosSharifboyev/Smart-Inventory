package uz.pdp.smartinventory.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.smartinventory.model.domain.Users;
import uz.pdp.smartinventory.model.enums.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends BaseRepository<Users, UUID> {

    // Login uchun o'chirilmagan foydalanuvchini topish
    Optional<Users> findByUsernameAndDeletedFalse(String username);

    boolean existsByEmailAndDeletedFalse(String email);

    Optional<Users> findByFullNameAndDeletedFalse(String userFullName);

    // Login paytida permissionlarni birdan yuklash uchun
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.permissions WHERE u.username = :username")
    Optional<Users> findByUsernameByPermissions(@Param("username") String username);



    boolean existsByUsernameAndDeletedFalse(String username);

    Optional<Users> findByEmailAndDeletedFalse(String email);

    @Query("""
            select count(u) >0
            from Users u
            join u.permissions p
            where u.username = :username and p.name = :permission
            """)
    boolean hasPermission(@Param("username") String username, @Param("permission") String permission);


    long countByEnabledTrue();
    long countByEnabledFalse();
    long countByRole(Role role);

    List<Users> findAllByDeletedFalse();
}
