package uz.pdp.smartinventory.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {

    @NotBlank(message = "Refresh token bo'sh bo'lishi mumkin emas!")
    private String refreshToken;
}