package uz.pdp.smartinventory.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.authenticator.SavedRequest;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderRequestDto {

    @NotNull(message = "Mijozni tanlash majburiy")
    private UUID userId;

    @NotEmpty(message = "Buyurtma tarkibi bo'sh bo'lishi mumkin emas")
    @Valid
    private List<OrderItemRequestDto> items;

    @NotBlank(message = "Manzilni kiriting")
    private String address;

    @NotBlank(message = "Telefon raqamini kiriting")
    private String phone;
}
