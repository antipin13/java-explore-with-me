package ru.practicum.ewm.service.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.user.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceImplTest {
    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    NewUserRequest newUserRequest;
    User newUser;
    UserDto userDto;

    @BeforeEach
    public void setUp() {
        newUserRequest = new NewUserRequest("Test User", "test@mail.ru");
        newUser = new User();
        userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@mail.ru")
                .build();
    }

    @Test
    public void testSaveUser() {
        when(userMapper.toUser(newUserRequest)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(newUser);
        when(userMapper.toUserDto(newUser)).thenReturn(userDto);

        UserDto result = userService.saveUser(newUserRequest);

        assertEquals(userDto, result);

        verify(userMapper).toUser(newUserRequest);
        verify(userRepository).save(newUser);
        verify(userMapper).toUserDto(newUser);
    }

    @Test
    public void testGetUsers() {
        List<User> users = Arrays.asList(
                newUser,
                User.builder().build()
        );

        List<UserDto> userDtos = Arrays.asList(
                userDto,
                UserDto.builder().build()
        );

        List<Long> ids = Arrays.asList(1L, 2L);

        when(userRepository.findByIdIn(ids, PageRequest.of(0, 10))).thenReturn(users);

        when(userMapper.toUserDto(users.get(0))).thenReturn(userDtos.get(0));
        when(userMapper.toUserDto(users.get(1))).thenReturn(userDtos.get(1));

        List<UserDto> result = userService.getUsers(ids, 0, 10);

        assertEquals(userDtos, result);
        assertEquals(2, userDtos.size());
        verify(userRepository).findByIdIn(ids, PageRequest.of(0, 10));
    }

    @Test
    public void testRemoveUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(newUser));

        userService.removeUser(1L);

        verify(userRepository).delete(newUser);
    }

    @Test
    public void testRemoveUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.removeUser(1L));

        assertEquals("Пользователь с ID - 1 не найден", exception.getMessage());
    }
}