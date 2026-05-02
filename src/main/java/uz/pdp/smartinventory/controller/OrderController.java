package uz.pdp.smartinventory.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.criteria.OrderCriteria;
import uz.pdp.smartinventory.model.dto.OrderDto;
import uz.pdp.smartinventory.model.dto.OrderRequestDto;
import uz.pdp.smartinventory.model.dto.OrderUpdateDto;
import uz.pdp.smartinventory.model.enums.OrderStatus;
import uz.pdp.smartinventory.service.OrderService;
import uz.pdp.smartinventory.service.ProductService;
import uz.pdp.smartinventory.service.UserService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public String getAll(Model model, @ModelAttribute("criteria") OrderCriteria criteria){

        Page<OrderDto> orderPage = orderService.getAll(criteria);
        System.out.println("Orders API-ga so'rov keldi!"); // Konsolni tekshirish uchun

        model.addAttribute("orders", orderPage);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("currentPages", criteria.getPage());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("criteria",criteria);
        model.addAttribute("totalOrders", orderPage.getTotalElements());
        model.addAttribute("activeCount", orderService.countByStatuses(List.of("NEW", "PROCESSING")));
        model.addAttribute("completedCount", orderService.countByStatus("COMPLETED"));
        model.addAttribute("cancelledCount", orderService.countByStatus("CANCELLED"));
        return "order/list";
    }

    @GetMapping("/create")
    public String createPage(Model model){
        model.addAttribute("orderRequest",new OrderRequestDto());
        model.addAttribute("products",productService.getAllActive());
        model.addAttribute("users",userService.getAllUsers());
        return "order/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("orderRequest") OrderRequestDto dto,
                         BindingResult bindingResult,
                         Model model){
        if (bindingResult.hasErrors()){
            System.out.println("Validatsiya xatosi: " + bindingResult.getAllErrors());
            model.addAttribute("products",productService.getAllActive());
            model.addAttribute("users",userService.getAllUsers());
            return "order/create";
        }
        orderService.create(dto);
        return "redirect:/orders";
    }

    @GetMapping("/update/{id}")
    public String updatePage(@PathVariable UUID id,Model model){
        // Mavjud buyurtmani olamiz
        OrderDto order = orderService.get(id);

        // Update uchun DTO tayyorlaymiz (mavjud ma'lumotlar bilan)
        OrderUpdateDto updateDto = new OrderUpdateDto();

        updateDto.setStatus(order.getStatus()); // Hozirgi statusini o'rnatamiz
        model.addAttribute("orderUpdate",order);
        model.addAttribute("orderId",id);
        model.addAttribute("statuses", OrderStatus.values());
        return "order/update";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable UUID id,
                         @ModelAttribute("orderUpdate") OrderUpdateDto dto){
        orderService.update(dto,id);
        return "redirect:/orders";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable UUID id){
        orderService.delete(id);
        return "redirect:/orders";
    }

}
