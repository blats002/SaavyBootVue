package org.saavy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.saavy.config.security.JwtUtils;
import org.saavy.config.security.UserDetailsImpl;
import org.saavy.config.security.UserDetailsServiceImpl;
import org.saavy.entity.User;
import org.saavy.entity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /api/auth/login - should return JWT token on valid credentials")
    void testLogin_Success() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "admin",
                "admin@saavy.org",
                "Admin User",
                "encoded-password",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("mock-jwt-token-12345");

        AuthController.LoginRequest request = new AuthController.LoginRequest("admin", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-12345"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 401 on bad credentials")
    void testLogin_InvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AuthController.LoginRequest request = new AuthController.LoginRequest("invalidUser", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/metadata/users/fields - should be publicly accessible without auth")
    void testPublicMetadataEndpoint() throws Exception {
        mockMvc.perform(get("/api/metadata/users/fields"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Load seeded 'user' from database and verify credentials and ROLE_USER")
    void testLoadUserCredentials() {
        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService.loadUserByUsername("user");
        org.junit.jupiter.api.Assertions.assertNotNull(userDetails);
        org.junit.jupiter.api.Assertions.assertEquals("user", userDetails.getUsername());
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("user123", userDetails.getPassword()));
        org.junit.jupiter.api.Assertions.assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("POST /api/auth/change-password - should successfully change password when current password matches")
    void testChangePassword_Success() throws Exception {
        User user = userRepository.findByUsername("user").orElseThrow();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        AuthController.ChangePasswordRequest request = new AuthController.ChangePasswordRequest(
                "user123",
                "newSecret456",
                "newSecret456"
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        // Verify updated password in DB
        User updated = userRepository.findByUsername("user").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("newSecret456", updated.getPassword()));

        // Restore password for other tests
        updated.setPassword(passwordEncoder.encode("user123"));
        userRepository.save(updated);
    }

    @Test
    @DisplayName("POST /api/auth/change-password - should return 400 when current password is wrong")
    void testChangePassword_WrongCurrentPassword() throws Exception {
        User user = userRepository.findByUsername("user").orElseThrow();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        AuthController.ChangePasswordRequest request = new AuthController.ChangePasswordRequest(
                "wrongPassword",
                "newSecret456",
                "newSecret456"
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    @DisplayName("POST /api/auth/change-password - should return 400 when confirm password does not match")
    void testChangePassword_MismatchedConfirm() throws Exception {
        User user = userRepository.findByUsername("user").orElseThrow();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        AuthController.ChangePasswordRequest request = new AuthController.ChangePasswordRequest(
                "user123",
                "newSecret456",
                "different456"
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("New password and confirm password do not match"));
    }
}

