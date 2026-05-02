package uz.pdp.smartinventory.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeDto {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 6, message = "Yangi parol kamida 6 ta belgi bo'lishi kerak")
    private String newPassword;

    @NotBlank
    private String confirmedPassword;
}
