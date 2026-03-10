package com.bookstore.admin.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@DisplayName("Login Controller Tests")
class LoginControllerTest {

    private static final String LOGIN_PATH = "/login";
    private static final String LOGIN_VIEW = "login";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should display login page")
    void shouldDisplayLoginPage() throws Exception {
        mockMvc.perform(get(LOGIN_PATH))
            .andExpect(status().isOk())
            .andExpect(view().name(LOGIN_VIEW));
    }

    @Test
    @DisplayName("Should display error message when error parameter is present")
    void shouldDisplayErrorMessage() throws Exception {
        mockMvc.perform(get(LOGIN_PATH).param("error", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name(LOGIN_VIEW))
            .andExpect(model().attribute("error", "login.error.unauthorized"));
    }

    @Test
    @DisplayName("Should display logout message when logout parameter is present")
    void shouldDisplayLogoutMessage() throws Exception {
        mockMvc.perform(get(LOGIN_PATH).param("logout", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name(LOGIN_VIEW))
            .andExpect(model().attribute("message", "You have been logged out successfully"));
    }
}
