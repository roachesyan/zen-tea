package com.zentea.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zentea.dto.request.CategoryCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andReturn();
        if (result.getResponse().getStatus() != 200) {
            System.out.println("Login failed: " + result.getResponse().getContentAsString());
            throw new RuntimeException("Login failed in setUp");
        }
        String body = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    @Test
    void listCategories_publicAccess() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createCategory_requiresAuth() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("测试分类");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCategory_withAuth_success() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("测试分类");
        request.setIcon("🧪");
        request.setSort(99);

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("测试分类"));
    }
}
