package uz.pdp.smartinventory.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.pdp.smartinventory.model.domain.OrderItems;
import uz.pdp.smartinventory.model.domain.Orders;
import uz.pdp.smartinventory.model.dto.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // Service-da userRepository orqali o'rnatamiz
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalAmount", ignore = true) // Service-da hisoblaymiz
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "address", source = "address")
    @Mapping(target = "phone", source = "phone")
    Orders toEntity(OrderRequestDto dto);

    @Mapping(target = "userFullName",source = "user.fullName", defaultValue = "Noma'lum mijoz")  // User entity-dagi fullName-ni oladi
    OrderDto toDto(Orders entity);

    // Order ichidagi har bir mahsulot (Item) Mapping
    @Mapping(target = "productId",source = "product.id")
    @Mapping(target = "productName",source = "product.name")
    // subTotal-ni hisoblash: count * priceAtOrder
    @Mapping(target = "subTotal",expression = "java(calculateSubTotal(entity))")
    OrderItemDto toItemDto(OrderItems entity);

    // SubTotal hisoblash uchun yordamchi metod (default metod)
    default BigDecimal calculateSubTotal(OrderItems entity){
        if (entity == null || entity.getPriceAtOrder() == null || entity.getCount() == null){
            return BigDecimal.ZERO;
        }
        return entity.getPriceAtOrder().multiply(BigDecimal.valueOf(entity.getCount()));
    }

    // Statusni yangilash uchun (UpdateDto dan foydalanganda)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(OrderUpdateDto dto, @MappingTarget Orders entity);


    // CartItem -> OrderItemRequestDto (Savatdan so'rovga o'tkazish)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "count", source = "quantity")
    OrderItemRequestDto toOrderItemRequest(CartItem cartItem);

    // Collection<CartItem> -> OrderRequestDto
    default OrderRequestDto toOrderRequestDto(UUID userId, Collection<CartItem> cartItems){
        OrderRequestDto dto = new OrderRequestDto();
        dto.setUserId(userId);
        dto.setItems(cartItems.stream()
                .map(this::toOrderItemRequest)
                .collect(Collectors.toList()));
        return dto;
    }
}
