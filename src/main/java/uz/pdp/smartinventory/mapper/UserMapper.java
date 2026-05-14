package uz.pdp.smartinventory.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.pdp.smartinventory.config.MyUserDetails;
import uz.pdp.smartinventory.model.domain.Users;
import uz.pdp.smartinventory.model.dto.UserCreateDto;
import uz.pdp.smartinventory.model.dto.UserDto;
import uz.pdp.smartinventory.model.dto.UserUpdateDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity -> MyUserDetails (Security uchun)
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "enabled", source = "enabled")
    MyUserDetails toUserDetails(Users user);

    @Mapping(target = "enabled",source = "enabled")
    UserDto toDto(Users user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Users fromCreateDto(UserCreateDto dto);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "password",ignore = true)
    @Mapping(target = "permissions",ignore = true)
    @Mapping(target = "username",source = "username")
    @Mapping(target = "enabled",source = "enabled")
    void updateFromDto(UserUpdateDto dto, @MappingTarget Users user);

    @Mapping(target = "permissionIds", ignore = true)
    UserUpdateDto toUpdateDto(UserDto dto);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "password",ignore = true)
    @Mapping(target = "permissions",ignore = true)
    @Mapping(target = "role",ignore = true)
    @Mapping(target = "enabled",ignore = true)
    void updateProfileFromDto(UserUpdateDto dto, @MappingTarget Users user);
}