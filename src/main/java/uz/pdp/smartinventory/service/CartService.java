package uz.pdp.smartinventory.service;

import jakarta.servlet.http.HttpSession;
import uz.pdp.smartinventory.model.dto.CartItem;
import uz.pdp.smartinventory.model.dto.OrderRequestDto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

public interface CartService {

    void addrToCart(UUID productId, HttpSession httpSession);
    void removeFromCart(UUID productId, HttpSession httpSession);
    void deleteFromCart(UUID productId, HttpSession httpSession);

    Collection<CartItem> getCartItems(HttpSession httpSession);
    BigDecimal calculateTotal(HttpSession httpSession);

    void clearCart(HttpSession httpSession);

    OrderRequestDto prepareOrderRequest(UUID userId, HttpSession httpSession);
}
