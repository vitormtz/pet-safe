package com.example.petsafeweb.controller;

import com.example.petsafeweb.dto.LoginRequest;
import com.example.petsafeweb.dto.LoginResponse;
import com.example.petsafeweb.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para gerenciar o login de usuários
 */
@Slf4j
@Controller
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            // Validar campos
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "E-mail é obrigatório");
                return "redirect:/login";
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Senha é obrigatória");
                return "redirect:/login";
            }

            // Fazer login via API
            LoginResponse response = authService.loginUser(loginRequest);

            // Armazenar dados na sessão
            session.setAttribute("userId", response.getUserId());
            session.setAttribute("userEmail", response.getEmail());
            session.setAttribute("userFullName", response.getFullName());
            session.setAttribute("accessToken", response.getAccessToken());
            session.setAttribute("refreshToken", response.getRefreshToken());
            session.setAttribute("isAuthenticated", true);

            // Redirecionar para dashboard (ou home)
            redirectAttributes.addFlashAttribute("success", "Login realizado com sucesso! Bem-vindo, " + response.getFullName());
            return "redirect:/";

        } catch (Exception e) {
            log.error("Erro ao fazer login", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("email", loginRequest.getEmail());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Logout realizado com sucesso!");
        return "redirect:/";
    }
}
