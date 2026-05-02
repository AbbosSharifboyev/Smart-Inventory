package uz.pdp.smartinventory.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.pdp.smartinventory.model.domain.base.BaseEntity;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Categories extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL)
    private List<Products> products;
}
