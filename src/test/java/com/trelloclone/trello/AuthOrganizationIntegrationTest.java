package com.trelloclone.trello;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.Cookie;

import org.testcontainers.containers.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class AuthOrganizationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("trello")
            .withUsername("trello")
            .withPassword("trello");

    @Autowired
    MockMvc mockMvc;

    @Test
    void signup_shouldCreateUser() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test2@example.com",
                                    "password": "password123",
                                    "userName": "Test User"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void signup_shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "test2-emal",
                            "password": "password123",
                            "userName": "Test User"
                        }
                        """)).andExpect(status().isBadRequest());
    }

    @Test
    void signin_shouldSignInUser() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "signin@example.com",
                                    "password": "password123",
                                    "userName": "Test User"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/signin").contentType(MediaType.APPLICATION_JSON).content("""
                    {
                        "email": "signin@example.com",
                        "password": "password123"
                    }
                """)).andExpect(status().isOk()).andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void me_shouldAuthenticateAndReturnUser() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "me@example.com",
                                    "password": "password123",
                                    "userName": "Test User"
                                }
                                """))
                .andExpect(status().isCreated());

        MvcResult signInResult = mockMvc
                .perform(post("/api/v1/auth/signin").contentType(MediaType.APPLICATION_JSON).content("""
                            {
                                "email": "me@example.com",
                                "password": "password123"
                            }
                        """)).andExpect(status().isOk()).andExpect(header().exists("Set-Cookie")).andReturn();

        String setCookie = signInResult.getResponse().getHeader("Set-Cookie");

        String accessToken = setCookie.split(";")[0].split("=", 2)[1];

        mockMvc.perform(
                get("/api/v1/auth/me").cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@example.com"))
                .andExpect(jsonPath("$.data.userName").value("Test User"));
    }

    @Test
    void signup_shouldRejectDuplicateEmail() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "duplicate@example.com",
                                    "password": "password123",
                                    "userName": "Test User"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "duplicate@example.com",
                                    "password": "password123",
                                    "userName": "Another User"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void signin_shouldRejectWrongPassword() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "wrong-password@example.com",
                                    "password": "password123",
                                    "userName": "Test User"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "wrong-password@example.com",
                                    "password": "wrongpassword"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(
                get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
