package org.example.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.dto.UserUpdateRequest;
import org.example.userservice.exception.GlobalExceptionHandler;
import org.example.userservice.exception.ResourceNotFoundException;
import org.example.userservice.exception.UserAlreadyExistsException;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private UserController userController;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler()) // ← Добавь это!
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== CREATE USER ==========
    @Test
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setAge(25);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("John Doe");
        response.setEmail("john@example.com");
        response.setAge(25);

        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.age", is(25)));

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    void createUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("");
        request.setEmail("invalid-email");
        request.setAge(-5);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.age").exists());

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_DuplicateEmail_ReturnsConflict() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setEmail("existing@example.com");
        request.setAge(25);

        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService).createUser(any(UserRequest.class));
    }

    // ========== GET USER BY ID ==========
    @Test
    void getUserById_ExistingId_ReturnsUser() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("John Doe");
        response.setEmail("john@example.com");
        response.setAge(25);

        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_NonExistingId_ReturnsNotFound() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    // ========== GET ALL USERS ==========
    @Test
    void getAllUsers_ReturnsUserList() throws Exception {
        UserResponse user1 = new UserResponse();
        user1.setId(1L);
        user1.setName("John");
        user1.setEmail("john@example.com");

        UserResponse user2 = new UserResponse();
        user2.setId(2L);
        user2.setName("Jane");
        user2.setEmail("jane@example.com");

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Jane")));

        verify(userService).getAllUsers();
    }

    // ========== UPDATE USER ==========
    @Test
    void updateUser_ValidRequest_ReturnsUpdatedUser() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("John Updated");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setAge(30);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("John Updated");
        response.setEmail("updated@example.com");
        response.setAge(30);

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.age", is(30)));

        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    // ========== DELETE USER ==========
    @Test
    void deleteUser_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}