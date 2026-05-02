package uz.pdp.smartinventory.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.smartinventory.repository.UserRepository;

@Service("auth")
@RequiredArgsConstructor
public class CustomSecurityService {

    private final UserRepository userRepository;

    public boolean hasPermission(String permission){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()){
            return false;
        }
        String username = authentication.getName();
        return userRepository.hasPermission(username,permission);
    }
}
