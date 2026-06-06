package uz.pdp.smartinventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.config.JwtProvider;
import uz.pdp.smartinventory.model.dto.LoginRequest;
import uz.pdp.smartinventory.model.dto.RefreshRequest;
import uz.pdp.smartinventory.model.dto.UserCreateDto;
import uz.pdp.smartinventory.model.dto.UserDto;
import uz.pdp.smartinventory.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager; // Parolni tekshiruvchi ob'ekt


    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserCreateDto dto){

        UserDto registeredUser = userService.create(dto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest){
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // Spring Security parolni BCrypt qilingan varianti bilan solishtiradi
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        // Muvaffaqiyatli login bo'lsa, lastLogin vaqtini yangilaymiz
        userService.updateLastLogin(username);

        // Tokenlarni yaratamiz
        String accessToken = jwtProvider.generateAccessToken(username);
        String refreshToken = jwtProvider.generateRefreshToken(username);

        userService.saveRefreshToken(username, refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ResponseEntity.ok(tokens);
    }

    //Refresh Token orqali yangi Access Token olish (Foydalanuvchi login oynasiga tushib qolmasligi uchun)
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody RefreshRequest request){

        String incomingRefreshToken = request.getRefreshToken();
        String username = jwtProvider.extractUsername(incomingRefreshToken);

        if (username != null && jwtProvider.isTokenValid(incomingRefreshToken, username)){

            // BAZADAGI TOKEN BILAN FRONTENDDAN KELGAN TOKENNI SOLISHTIRAMIZ
            boolean isTokenValidInDb = userService.validateDatabaseRefreshToken(username, incomingRefreshToken);
            if (isTokenValidInDb){
                // Hamma narsa to'g'ri bo'lsa, faqat yangi access token beramiz

                String newAccessToken = jwtProvider.generateAccessToken(username);
                return ResponseEntity.ok(Map.of(
                        "accessToken", newAccessToken,
                        "refreshToken", incomingRefreshToken
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Refresh token yaroqsiz!"));
    }

}
