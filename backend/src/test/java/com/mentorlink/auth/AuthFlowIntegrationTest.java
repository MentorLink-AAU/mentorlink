package com.mentorlink.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorlink.modules.auth.dto.LoginRequest;
import com.mentorlink.modules.auth.dto.RegisterStudentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void registerStudent_thenLoginStudent_returnsJwt() throws Exception {
        RegisterStudentRequest reg = new RegisterStudentRequest();
        reg.setEmail("student1@gmail.com");
        reg.setFullName("Student One");
        reg.setPassword("Pass@12345");
        reg.setRollNumber("A001");
        reg.setDepartment("CSE");
        reg.setYearOfStudy(3);

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("student1@gmail.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));

        LoginRequest login = new LoginRequest();
        login.setEmail("student1@gmail.com");
        login.setPassword("Pass@12345");

        mockMvc.perform(post("/api/auth/login/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.requiresPasswordChange").value(false));
    }
}

