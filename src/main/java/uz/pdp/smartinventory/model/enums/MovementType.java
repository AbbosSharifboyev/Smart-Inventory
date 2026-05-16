package uz.pdp.smartinventory.model.enums;


public enum MovementType {
    IN("Kirim"),
    OUT("Chiqim");

    private final String displayName;
    MovementType(String displayName) { this.displayName = displayName; }
}

