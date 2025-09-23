package ru.practicum.ewm.service.user;

import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto saveUser(NewUserRequest request);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void removeUser(Long id);
}
