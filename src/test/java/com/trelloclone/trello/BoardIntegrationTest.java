package com.trelloclone.trello;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class BoardIntegrationTest {

        @Container
        @ServiceConnection
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
                        .withDatabaseName("trello")
                        .withPassword("trello")
                        .withUsername("trello");

        @Autowired
        MockMvc mockMvc;

        // ---------------------------------------------------------
        // CREATE
        // ---------------------------------------------------------

        @Test
        void createBoard_shouldCreateBoard() throws Exception {

                Cookie cookie = createAuthenticatedUser();

                String organizationId = createOrganization(cookie);

                mockMvc.perform(
                                post("/api/v1/organization/" + organizationId + "/boards")
                                                .cookie(cookie)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "title": "Engineering"
                                                                }
                                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.title").value("Engineering"))
                                .andExpect(jsonPath("$.data.organizationId").value(organizationId));
        }

        // ---------------------------------------------------------
        // GET
        // ---------------------------------------------------------

        @Test
        void getBoard_shouldReturnBoard() throws Exception {

                Cookie cookie = createAuthenticatedUser();

                String organizationId = createOrganization(cookie);
                String boardId = createBoard(cookie, organizationId);

                mockMvc.perform(
                                get("/api/v1/organization/" + organizationId + "/boards/" + boardId)
                                                .cookie(cookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.id").value(boardId))
                                .andExpect(jsonPath("$.data.title").value("Engineering"))
                                .andExpect(jsonPath("$.data.organizationId").value(organizationId));
        }

        // ---------------------------------------------------------
        // UPDATE
        // ---------------------------------------------------------

        @Test
        void updateBoard_shouldUpdateBoard() throws Exception {

                Cookie cookie = createAuthenticatedUser();

                String organizationId = createOrganization(cookie);
                String boardId = createBoard(cookie, organizationId);

                mockMvc.perform(
                                patch("/api/v1/organization/" + organizationId + "/boards/" + boardId)
                                                .cookie(cookie)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "title": "Engineering Updated"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.id").value(boardId))
                                .andExpect(jsonPath("$.data.title").value("Engineering Updated"))
                                .andExpect(jsonPath("$.data.organizationId").value(organizationId));
        }

        // ---------------------------------------------------------
        // DELETE
        // ---------------------------------------------------------

        @Test
        void deleteBoard_shouldDeleteBoard() throws Exception {

                Cookie cookie = createAuthenticatedUser();

                String organizationId = createOrganization(cookie);
                String boardId = createBoard(cookie, organizationId);

                mockMvc.perform(
                                delete("/api/v1/organization/" + organizationId + "/boards/" + boardId)
                                                .cookie(cookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"));

                // Verify board no longer exists
                mockMvc.perform(
                                get("/api/v1/organization/" + organizationId + "/boards/" + boardId)
                                                .cookie(cookie))
                                .andExpect(status().isNotFound());
        }

        // ---------------------------------------------------------
        // GET - BOARD NOT FOUND
        // ---------------------------------------------------------

        @Test
        void getBoard_shouldReturn404WhenBoardDoesNotExist() throws Exception {

                Cookie cookie = createAuthenticatedUser();

                String organizationId = createOrganization(cookie);

                UUID randomBoardId = UUID.randomUUID();

                mockMvc.perform(
                                get("/api/v1/organization/" + organizationId + "/boards/" + randomBoardId)
                                                .cookie(cookie))
                                .andExpect(status().isNotFound());
        }

        // ---------------------------------------------------------
        // GET - WRONG ORGANIZATION
        // ---------------------------------------------------------

        @Test
        void getBoard_shouldRejectBoardFromDifferentOrganization() throws Exception {

                Cookie cookie = createAuthenticatedUser();

                String organizationOne = createOrganization(cookie);
                String boardId = createBoard(cookie, organizationOne);

                String organizationTwo = createOrganization(cookie);

                mockMvc.perform(
                                get("/api/v1/organization/" + organizationTwo + "/boards/" + boardId)
                                                .cookie(cookie))
                                .andExpect(status().isNotFound());
        }

        // ---------------------------------------------------------
        // CREATE - NON MEMBER
        // ---------------------------------------------------------

        @Test
        void createBoard_shouldRejectNonMember() throws Exception {

                Cookie ownerCookie = createAuthenticatedUser();
                String organizationId = createOrganization(ownerCookie);

                Cookie otherUserCookie = createAuthenticatedUser();

                mockMvc.perform(
                                post("/api/v1/organization/" + organizationId + "/boards")
                                                .cookie(otherUserCookie)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "title": "Unauthorized Board"
                                                                }
                                                                """))
                                .andExpect(status().isForbidden());
        }

        // ---------------------------------------------------------
        // UPDATE - NON ADMIN
        // ---------------------------------------------------------

        @Test
        void updateBoard_shouldRejectNonAdmin() throws Exception {

                Cookie adminCookie = createAuthenticatedUser();

                String organizationId = createOrganization(adminCookie);
                String boardId = createBoard(adminCookie, organizationId);

                /*
                 * NOTE:
                 * You need to add the second user as a MEMBER of the organization
                 * before this test can work.
                 *
                 * Once you have your membership endpoint/service:
                 *
                 * Cookie memberCookie = createAuthenticatedUser();
                 * addMemberToOrganization(...);
                 *
                 * Then use memberCookie here.
                 */
        }

        // ---------------------------------------------------------
        // DELETE - NON ADMIN
        // ---------------------------------------------------------

        @Test
        void deleteBoard_shouldRejectNonAdmin() throws Exception {

                Cookie adminCookie = createAuthenticatedUser();

                String organizationId = createOrganization(adminCookie);
                String boardId = createBoard(adminCookie, organizationId);

                /*
                 * Same as updateBoard_shouldRejectNonAdmin:
                 * this test requires a MEMBER user to exist in the organization.
                 */
        }

        // ---------------------------------------------------------
        // TEST HELPERS
        // ---------------------------------------------------------

        private Cookie createAuthenticatedUser() throws Exception {

                String email = "user-" + UUID.randomUUID() + "@example.com";

                mockMvc.perform(
                                post("/api/v1/auth/signup")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "email": "%s",
                                                                    "password": "password123",
                                                                    "userName": "Test User"
                                                                }
                                                                """.formatted(email)))
                                .andExpect(status().isCreated());

                MvcResult signinResult = mockMvc.perform(
                                post("/api/v1/auth/signin")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "email": "%s",
                                                                    "password": "password123"
                                                                }
                                                                """.formatted(email)))
                                .andExpect(status().isOk())
                                .andReturn();

                String setCookie = signinResult
                                .getResponse()
                                .getHeader("Set-Cookie");

                String accessToken = setCookie
                                .split(";")[0]
                                .split("=", 2)[1];

                return new Cookie("access_token", accessToken);
        }

        private String createOrganization(Cookie cookie) throws Exception {

                MvcResult result = mockMvc.perform(
                                post("/api/v1/organization")
                                                .cookie(cookie)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "name": "Test Organization",
                                                                    "description": "Testing"
                                                                }
                                                                """))
                                .andExpect(status().isCreated())
                                .andReturn();

                return JsonPath.read(
                                result.getResponse().getContentAsString(),
                                "$.data.id");
        }

        private String createBoard(Cookie cookie, String organizationId) throws Exception {

                MvcResult result = mockMvc.perform(
                                post("/api/v1/organization/" + organizationId + "/boards")
                                                .cookie(cookie)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "title": "Engineering"
                                                                }
                                                                """))
                                .andExpect(status().isCreated())
                                .andReturn();

                return JsonPath.read(
                                result.getResponse().getContentAsString(),
                                "$.data.id");
        }
}