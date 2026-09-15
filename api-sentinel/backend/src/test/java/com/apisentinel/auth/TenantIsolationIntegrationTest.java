package com.apisentinel.auth;

import com.apisentinel.application.Application;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.application.CreateApplicationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TenantIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void testUserCannotAccessAnotherUsersApplication() throws Exception {
        // Register User A
        AuthResponse userA = authService.register(
                new RegisterRequest("usera_" + System.currentTimeMillis() + "@sentinel.dev", "password123", "ROLE_USER")
        );

        // Register User B
        AuthResponse userB = authService.register(
                new RegisterRequest("userb_" + System.currentTimeMillis() + "@sentinel.dev", "password123", "ROLE_USER")
        );

        // User A creates an application
        String appResponseJson = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + userA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicationRequest("User A Private App", "PRODUCTION"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appId = objectMapper.readTree(appResponseJson).path("data").path("id").asText();

        // User B attempts to access User A's application by ID -> Must be 403 Forbidden!
        mockMvc.perform(get("/api/v1/applications/" + appId)
                        .header("Authorization", "Bearer " + userB.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
