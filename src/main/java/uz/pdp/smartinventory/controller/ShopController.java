package uz.pdp.smartinventory.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.smartinventory.config.MyUserDetails;
import uz.pdp.smartinventory.criteria.ProductCriteria;
import uz.pdp.smartinventory.model.dto.CartItem;
import uz.pdp.smartinventory.model.dto.OrderRequestDto;

import uz.pdp.smartinventory.model.dto.ProductDto;
import uz.pdp.smartinventory.service.CartService;
import uz.pdp.smartinventory.service.OrderService;
import uz.pdp.smartinventory.service.ProductService;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping
    public String shopPage(@ModelAttribute ProductCriteria criteria, Model model) {

        Page<ProductDto> productPage = productService.getFilteredProducts(criteria);

        model.addAttribute("products",productPage.getContent());
        model.addAttribute("currentPage",productPage.getNumber());
        model.addAttribute("totalPages",productPage.getTotalPages());
        model.addAttribute("criteria",criteria);
        return "shop/list";
    }

    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable UUID id,
                            HttpSession session,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        try {
            cartService.addToCart(id, session);
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error",e.getMessage());
        }
        return "redirect:" + getRefer(request);
    }


    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {

        model.addAttribute("cartItems",cartService.getCartItems(session));
        model.addAttribute("subtotal", cartService.calculateTotal(session) != null ? cartService.calculateTotal(session) : 0.0);
        return "shop/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable UUID id, HttpSession session,
                                 HttpServletRequest request) {
        cartService.removeFromCart(id, session);
        return "redirect:" + getRefer(request);
    }

    @PostMapping("/cart/delete/{id}")
    public String deleteFromCart(@PathVariable UUID id, HttpSession session,
                                 HttpServletRequest request) {
        cartService.deleteFromCart(id, session);
        return "redirect:" + getRefer(request);
    }


    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {

        Collection<CartItem> cartItems = cartService.getCartItems(session);
        if (cartItems.isEmpty()){
            return "redirect:/shop";
        }
        model.addAttribute("totalPrice", cartService.calculateTotal(session));
        model.addAttribute("cartItems", cartItems);
        return "shop/checkout";
    }

    @PostMapping("/checkout/complete")
    public String completeOrder(HttpSession session,
                                @AuthenticationPrincipal MyUserDetails userDetails,
                                @RequestParam String address,
                                @RequestParam String phone) {

        UUID userId = userDetails.getId();

        OrderRequestDto dto = cartService.prepareOrderRequest(userId, session);
        dto.setAddress(address);
        dto.setPhone(phone);
        orderService.create(dto);
        cartService.clearCart(session);
        return "redirect:/shop/success";
    }


    @GetMapping("/success")
    public String successPage() {
        return "shop/success";
    }

    private String getRefer(HttpServletRequest request){
        String referer = request.getHeader("Referer");
        return referer != null ? referer : "/shop";
    }

}