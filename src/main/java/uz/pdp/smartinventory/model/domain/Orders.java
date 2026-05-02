package uz.pdp.smartinventory.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.pdp.smartinventory.model.domain.base.BaseEntity;
import uz.pdp.smartinventory.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Orders extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private Users user;

    @Column(name = "delivery_address", length = 500, nullable = false)
    private String address = "No Address";

    @Column(name = "contact_phone", length = 20, nullable = false)
    private String phone = "No Phone";

    @Column(precision = 19,scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItems> items;

}
