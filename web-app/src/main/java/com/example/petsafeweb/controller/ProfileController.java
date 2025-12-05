package com.example.petsafeweb.controller;

import com.example.petsafeweb.dto.UpdatePasswordRequest;
import com.example.petsafeweb.dto.UpdateProfileRequest;
import com.example.petsafeweb.dto.UserProfileResponse;
import com.example.petsafeweb.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para gerenciar o perfil do usuário
 */
@Slf4j
@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Verificar se o usuário está autenticado
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            redirectAttributes.addFlashAttribute("error", "Você precisa fazer login para acessar esta página.");
            return "redirect:/login";
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            String accessToken = (String) session.getAttribute("accessToken");

            // Buscar dados do perfil na API
            UserProfileResponse profile = userService.getUserProfile(userId, accessToken);

            // Adicionar dados ao model
            model.addAttribute("profile", profile);
            model.addAttribute("updateRequest", new UpdateProfileRequest());

            return "profile";

        } catch (Exception e) {
            log.error("Erro ao carregar perfil", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("updateRequest", new UpdateProfileRequest());
            return "profile";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                               @RequestParam("phone") String phone,
                               @RequestParam(value = "currentPassword", required = false) String currentPassword,
                               @RequestParam(value = "newPassword", required = false) String newPassword,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        // Verificar se o usuário está autenticado
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            redirectAttributes.addFlashAttribute("error", "Você precisa fazer login para acessar esta página.");
            return "redirect:/login";
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            String accessToken = (String) session.getAttribute("accessToken");

            // Validar campos obrigatórios
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new Exception("Nome completo é obrigatório");
            }

            if (phone == null || phone.trim().isEmpty()) {
                throw new Exception("Telefone é obrigatório");
            }

            // Validar senha ANTES de salvar qualquer coisa
            boolean isChangingPassword = newPassword != null && !newPassword.trim().isEmpty();
            if (isChangingPassword) {
                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                    throw new Exception("Para alterar a senha, informe a senha atual");
                }
                if (newPassword.length() < 8) {
                    throw new Exception("A nova senha deve ter no mínimo 8 caracteres");
                }
            }

            // Se está alterando senha, atualizar senha PRIMEIRO
            if (isChangingPassword) {
                UpdatePasswordRequest passwordRequest = UpdatePasswordRequest.builder()
                        .currentPassword(currentPassword)
                        .newPassword(newPassword)
                        .build();

                userService.updateUserPassword(passwordRequest, accessToken);
            }

            // Só depois de validar/atualizar senha, atualizar dados do perfil
            UpdateProfileRequest profileRequest = UpdateProfileRequest.builder()
                    .fullName(fullName.trim())
                    .phone(phone.trim())
                    .build();

            UserProfileResponse updatedProfile = userService.updateUserProfile(profileRequest, accessToken);

            // Atualizar dados na sessão
            session.setAttribute("userFullName", updatedProfile.getFullName());

            // Redirecionar com mensagem de sucesso
            String successMessage = isChangingPassword
                    ? "Perfil e senha atualizados com sucesso!"
                    : "Perfil atualizado com sucesso!";
            redirectAttributes.addFlashAttribute("success", successMessage);
            return "redirect:/profile";

        } catch (Exception e) {
            log.error("Erro ao atualizar perfil", e);

            // Recarregar dados do perfil para exibir na página
            try {
                Long userId = (Long) session.getAttribute("userId");
                String accessToken = (String) session.getAttribute("accessToken");
                UserProfileResponse profile = userService.getUserProfile(userId, accessToken);
                model.addAttribute("profile", profile);
            } catch (Exception ex) {
                log.error("Erro ao recarregar perfil", ex);
            }

            model.addAttribute("error", e.getMessage());
            return "profile";
        }
    }
}
