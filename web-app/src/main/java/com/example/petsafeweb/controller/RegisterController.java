package com.example.petsafeweb.controller;

import com.example.petsafeweb.dto.RegisterRequest;
import com.example.petsafeweb.dto.RegisterResponse;
import com.example.petsafeweb.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para gerenciar o cadastro de novos usuários
 */
@Slf4j
@Controller
public class RegisterController {

    private final AuthService authService;

    @Autowired
    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        try {
            // Validação básica
            if (fullName == null || fullName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Nome completo é obrigatório");
                return "redirect:/register";
            }

            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "E-mail é obrigatório");
                return "redirect:/register";
            }

            if (password == null || password.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "A senha deve ter no mínimo 8 caracteres");
                return "redirect:/register";
            }

            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "As senhas não coincidem");
                return "redirect:/register";
            }

            // Remover formatação do telefone antes de enviar para a API
            String cleanPhone = phone.replaceAll("[^0-9]", "");

            // Criar o objeto de requisição
            RegisterRequest registerRequest = new RegisterRequest(
                email.trim(),
                password,
                fullName.trim(),
                cleanPhone
            );

            // Chamar a API para registrar o usuário
            log.info("Registrando novo usuário: {}", email);
            RegisterResponse response = authService.registerUser(registerRequest);

            log.info("Usuário registrado com sucesso. ID: {}", response.getId());
            redirectAttributes.addFlashAttribute("success",
                "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Erro ao registrar usuário", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
