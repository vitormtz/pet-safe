package com.example.petsafeweb.controller;

import com.example.petsafeweb.dto.PetRequest;
import com.example.petsafeweb.dto.PetResponse;
import com.example.petsafeweb.service.PetService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller para gerenciamento de pets
 */
@Slf4j
@Controller
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    /**
     * Exibe a página de listagem de pets
     */
    @GetMapping
    public String showPetsPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        String accessToken = (String) session.getAttribute("accessToken");

        // Verificar se o usuário está autenticado
        if (isAuthenticated == null || !isAuthenticated) {
            redirectAttributes.addFlashAttribute("error", "Você precisa estar logado para acessar esta página.");
            return "redirect:/login";
        }

        try {
            // Verificar se tem token
            if (accessToken == null || accessToken.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Sessão expirada. Faça login novamente.");
                return "redirect:/login";
            }

            List<PetResponse> pets = petService.listPets(accessToken);

            model.addAttribute("pets", pets != null ? pets : List.of());
            model.addAttribute("petRequest", new PetRequest());

            return "pets";

        } catch (Exception e) {
            log.error("Erro ao carregar página de pets: {}", e.getMessage(), e);

            // Se for erro de autenticação, redirecionar para login
            if (e.getMessage().contains("Sessão expirada") || e.getMessage().contains("Unauthorized")) {
                redirectAttributes.addFlashAttribute("error", "Sessão expirada. Faça login novamente.");
                return "redirect:/login";
            }

            model.addAttribute("error", e.getMessage());
            model.addAttribute("pets", List.of());
            model.addAttribute("petRequest", new PetRequest());
            return "pets";
        }
    }

    /**
     * Cria um novo pet
     */
    @PostMapping
    public String createPet(@ModelAttribute PetRequest petRequest,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            // Converter strings vazias em null para campos opcionais
            if (petRequest.getBreed() != null && petRequest.getBreed().trim().isEmpty()) {
                petRequest.setBreed(null);
            }
            if (petRequest.getMicrochipId() != null && petRequest.getMicrochipId().trim().isEmpty()) {
                petRequest.setMicrochipId(null);
            }

            // Processar data de nascimento
            if (petRequest.getDob() != null && !petRequest.getDob().trim().isEmpty()) {
                // Converter formato yyyy-MM-dd para ISO 8601 (yyyy-MM-ddT00:00:00Z)
                String dob = petRequest.getDob().trim();
                if (!dob.contains("T")) {
                    dob = dob + "T00:00:00Z";
                    petRequest.setDob(dob);
                }
            } else {
                petRequest.setDob(null);
            }

            String accessToken = (String) session.getAttribute("accessToken");
            petService.createPet(petRequest, accessToken);

            redirectAttributes.addFlashAttribute("success", "Pet cadastrado com sucesso!");
            return "redirect:/pets";

        } catch (Exception e) {
            log.error("Erro ao criar pet", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pets";
        }
    }

    /**
     * Atualiza um pet existente
     */
    @PostMapping("/{id}/update")
    public String updatePet(@PathVariable Long id,
                           @ModelAttribute PetRequest petRequest,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            // Converter strings vazias em null para campos opcionais
            if (petRequest.getBreed() != null && petRequest.getBreed().trim().isEmpty()) {
                petRequest.setBreed(null);
            }
            if (petRequest.getMicrochipId() != null && petRequest.getMicrochipId().trim().isEmpty()) {
                petRequest.setMicrochipId(null);
            }

            // Processar data de nascimento
            if (petRequest.getDob() != null && !petRequest.getDob().trim().isEmpty()) {
                // Converter formato yyyy-MM-dd para ISO 8601 (yyyy-MM-ddT00:00:00Z)
                String dob = petRequest.getDob().trim();
                if (!dob.contains("T")) {
                    dob = dob + "T00:00:00Z";
                    petRequest.setDob(dob);
                }
            } else {
                petRequest.setDob(null);
            }

            String accessToken = (String) session.getAttribute("accessToken");
            petService.updatePet(id, petRequest, accessToken);

            redirectAttributes.addFlashAttribute("success", "Pet atualizado com sucesso!");
            return "redirect:/pets";

        } catch (Exception e) {
            log.error("Erro ao atualizar pet", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pets";
        }
    }

    /**
     * Exclui um pet
     */
    @PostMapping("/{id}/delete")
    public String deletePet(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            String accessToken = (String) session.getAttribute("accessToken");
            petService.deletePet(id, accessToken);

            redirectAttributes.addFlashAttribute("success", "Pet excluído com sucesso!");
            return "redirect:/pets";

        } catch (Exception e) {
            log.error("Erro ao excluir pet", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pets";
        }
    }
}
