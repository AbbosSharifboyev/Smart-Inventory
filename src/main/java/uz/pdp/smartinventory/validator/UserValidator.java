package uz.pdp.smartinventory.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.pdp.smartinventory.model.domain.Users;
import uz.pdp.smartinventory.model.dto.PasswordChangeDto;
import uz.pdp.smartinventory.model.dto.UserCreateDto;
import uz.pdp.smartinventory.model.dto.UserUpdateDto;
import uz.pdp.smartinventory.repository.UserRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public Users existAndGet(UUID userId){
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));
    }

    public void validateOnCreate(UserCreateDto dto) {
        // 1. Username band emasligini tekshirish
        if (userRepository.existsByUsernameAndDeletedFalse(dto.getUsername())) {
            throw new RuntimeException("Bu username allaqachon band: " + dto.getUsername());
        }

        // 2. Email band emasligini tekshirish
        if (userRepository.existsByEmailAndDeletedFalse(dto.getEmail())) {
            throw new RuntimeException("Bu email bilan allaqachon ro'yxatdan o'tilgan: " + dto.getEmail());
        }

        // 3. Parol uzunligini tekshirish
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new RuntimeException("Parol kamida 6 ta belgidan iborat bo'lishi kerak!");
        }
    }


    public void validateOnUpdate(UserUpdateDto dto, UUID currentUserId) {

        // O'zidan boshqa birovning username/emailini band qilib qo'ymasligini tekshirish
        userRepository.findByUsernameAndDeletedFalse(dto.getUsername())
                .ifPresent(user -> {
                    if (!user.getId().equals(currentUserId)) {
                        throw new RuntimeException("Bu username boshqa foydalanuvchi tomonidan band qilingan!");
                    }
                });
        userRepository.findByEmailAndDeletedFalse(dto.getEmail())
                .ifPresent(users -> {
                    if (!users.getId().equals(currentUserId)){
                        throw new RuntimeException("Bu email boshqa foydalanuvchi tomonidan band qilingan!");
                    }
                });
    }

    public void validateChangePassword(Users user, PasswordChangeDto dto){
        //Yangi parol bir-biriga mosligini tekshirish
        if (!dto.getNewPassword().equals(dto.getConfirmedPassword())){
            throw new RuntimeException("Parollar bir-birga mos kelmadi!");
        }
        //Eski parol bir-biriga tugriligini tekshirish
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new RuntimeException("Eski parol notugri kiritildi!");
        }
        //Yangi parol eski parol bilan bir xil bo'lmasligi kerak
        if (dto.getNewPassword().equals(dto.getOldPassword())){
            throw new RuntimeException("Yangi parol eski paroldan farq qilishi kerak!");
        }
    }

    public void validateChangePasswordByAdmin(String newPassword){
        if (newPassword == null || newPassword.length() < 6){
            throw new RuntimeException("Yangi parol talablarga javob bermaydi!");
        }
    }
}
