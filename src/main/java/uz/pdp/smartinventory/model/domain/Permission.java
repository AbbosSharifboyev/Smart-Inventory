package uz.pdp.smartinventory.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.smartinventory.model.domain.base.IdEntity;

import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Permission extends IdEntity {


    @Column(unique = true, nullable = false)
    private String name; // Masalan: "PRODUCT_CREATE"

    private String description; // Huquq haqida qisqacha ma'lumot
}
