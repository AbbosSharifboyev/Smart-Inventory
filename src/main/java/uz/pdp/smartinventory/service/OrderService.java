package uz.pdp.smartinventory.service;

import uz.pdp.smartinventory.criteria.OrderCriteria;
import uz.pdp.smartinventory.model.dto.OrderDto;
import uz.pdp.smartinventory.model.dto.OrderRequestDto;
import uz.pdp.smartinventory.model.dto.OrderUpdateDto;

import java.util.List;
import java.util.UUID;

public interface OrderService extends CRUDService<
        OrderRequestDto,   //Create Dto
        OrderDto,          //Response Dto
        OrderUpdateDto,    //Update Dto
        UUID,              //Id type
        OrderCriteria      //Search criteria
        > {
    // Bu yerga Orderga xos qo'shimcha metodlar qo'shish mumkin
    // Masalan: void changeStatus(UUID id, OrderStatus status);

    long countByStatus(String statusName);

    long countByStatuses(List<String> statusNames);
}

