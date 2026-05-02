package uz.pdp.smartinventory.model.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW("Yangi"),
    PROCESSING("Jarayonda"),
    COMPLETED("Bajarilgan"),
    CANCELLED("Bekor qilingan");

    private final String displayName;
    OrderStatus(String displayName) { this.displayName = displayName; }
}