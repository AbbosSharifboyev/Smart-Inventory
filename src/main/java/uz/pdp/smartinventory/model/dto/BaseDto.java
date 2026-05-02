package uz.pdp.smartinventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseDto implements Serializable {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
