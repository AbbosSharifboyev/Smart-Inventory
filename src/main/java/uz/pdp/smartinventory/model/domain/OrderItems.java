package uz.pdp.smartinventory.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.pdp.smartinventory.model.domain.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItems extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Products product;

    private BigDecimal subtotal;

    private Integer count;

    private BigDecimal priceAtOrder;    //   Tarix uchun
}
