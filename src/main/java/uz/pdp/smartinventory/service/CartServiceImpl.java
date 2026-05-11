package uz.pdp.smartinventory.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.smartinventory.mapper.OrderMapper;
import uz.pdp.smartinventory.model.dto.CartItem;
import uz.pdp.smartinventory.model.dto.OrderRequestDto;
import uz.pdp.smartinventory.model.dto.ProductDto;
import uz.pdp.smartinventory.validator.CartValidator;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService{

    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final CartValidator cartValidator;


    @Override
    public void addToCart(UUID productId, HttpSession session) {
        Map<UUID, CartItem> cart = getOrCreateCart(session);
        int currentQty = cart.containsKey(productId) ? cart.get(productId).getQuantity() : 0;

        cartValidator.validateStock(productId, currentQty + 1);
        if (cart.containsKey(productId)){
            cart.get(productId).setQuantity(currentQty + 1);
        }else {
            ProductDto product = productService.get(productId);
            cart.put(productId, new CartItem(product.getId(),product.getName(),
                    product.getPrice(),1,product.getImagePath()));
        }

        session.setAttribute("cart",cart);
    }

    @Override
    public void removeFromCart(UUID productId, HttpSession session) {
        Map<UUID, CartItem> cart = getOrCreateCart(session);
        if (cart.containsKey(productId)){
            CartItem item = cart.get(productId);
            if (item.getQuantity() > 1) item.setQuantity(item.getQuantity() - 1);
            else cart.remove(productId);
            session.setAttribute("cart",cart);
        }
    }

    @Override
    public void deleteFromCart(UUID productId, HttpSession session) {
        Map<UUID, CartItem> cart = getOrCreateCart(session);
        cart.remove(productId);
        session.setAttribute("cart",cart);
    }

    @Override
    public BigDecimal calculateTotal(HttpSession httpSession) {
        return getCartItems(httpSession).stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    @Override
    public OrderRequestDto prepareOrderRequest(UUID userId, HttpSession httpSession) {
        Collection<CartItem> items = getCartItems(httpSession);
        if (items.isEmpty()){
            throw new RuntimeException("Savat bo`sh");
        }
        return orderMapper.toOrderRequestDto(userId,items);
    }

    @Override
    public void clearCart(HttpSession httpSession) {
        httpSession.removeAttribute("cart");
    }

    @Override
    public Collection<CartItem> getCartItems(HttpSession httpSession) {
        return getOrCreateCart(httpSession).values();
    }

    private Map<UUID, CartItem> getOrCreateCart(HttpSession session){
        Map<UUID, CartItem> cart = (Map<UUID, CartItem>) session.getAttribute("cart");
        return cart != null? cart : new HashMap<>();
    }
}
