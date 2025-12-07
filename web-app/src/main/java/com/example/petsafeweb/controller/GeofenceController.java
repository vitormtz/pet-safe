package com.example.petsafeweb.controller;

import com.example.petsafeweb.dto.GeofenceRequest;
import com.example.petsafeweb.dto.GeofenceResponse;
import com.example.petsafeweb.service.GeofenceService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para gerenciamento de geofence
 */
@Slf4j
@Controller
@RequestMapping("/area-segura")
public class GeofenceController {

    private final GeofenceService geofenceService;

    public GeofenceController(GeofenceService geofenceService) {
        this.geofenceService = geofenceService;
    }

    private String checkAuth(HttpSession session, RedirectAttributes redirectAttributes) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        String accessToken = (String) session.getAttribute("accessToken");

        if (isAuthenticated == null || !isAuthenticated || accessToken == null || accessToken.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Sessão expirada. Faça login novamente.");
            return "redirect:/login";
        }
        return null;
    }

    /**
     * Exibe a página de geofence (criar ou editar)
     */
    @GetMapping
    public String showGeofencePage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String authCheck = checkAuth(session, redirectAttributes);
        if (authCheck != null)
            return authCheck;

        String accessToken = (String) session.getAttribute("accessToken");

        try {
            // Tenta buscar o geofence existente
            GeofenceResponse geofence = geofenceService.getGeofence(accessToken);

            if (geofence != null) {
                model.addAttribute("geofence", geofence);
                model.addAttribute("hasGeofence", true);
            } else {
                model.addAttribute("hasGeofence", false);
            }

            return "geofence";
        } catch (Exception e) {
            log.error("Erro ao carregar página de geofence", e);
            model.addAttribute("error", "Erro ao carregar geofence: " + e.getMessage());
            model.addAttribute("hasGeofence", false);
            return "geofence";
        }
    }

    /**
     * Criar ou atualizar geofence
     */
    @PostMapping("/save")
    public String saveGeofence(
            @RequestParam String name,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Integer radius,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String authCheck = checkAuth(session, redirectAttributes);
        if (authCheck != null)
            return authCheck;

        String accessToken = (String) session.getAttribute("accessToken");

        try {
            GeofenceRequest request = new GeofenceRequest();
            request.setName(name);
            request.setLatitude(latitude);
            request.setLongitude(longitude);
            request.setRadius_m(radius);

            // Verifica se já existe geofence
            GeofenceResponse existing = geofenceService.getGeofence(accessToken);

            if (existing != null) {
                // Atualizar
                geofenceService.updateGeofence(request, accessToken);
                redirectAttributes.addFlashAttribute("success", "Área segura atualizada com sucesso!");
            } else {
                // Criar
                geofenceService.createGeofence(request, accessToken);
                redirectAttributes.addFlashAttribute("success", "Área segura criada com sucesso!");
            }

            return "redirect:/area-segura";
        } catch (Exception e) {
            log.error("Erro ao salvar geofence", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/area-segura";
        }
    }

    /**
     * Deletar geofence
     */
    @PostMapping("/delete")
    public String deleteGeofence(HttpSession session, RedirectAttributes redirectAttributes) {
        String authCheck = checkAuth(session, redirectAttributes);
        if (authCheck != null)
            return authCheck;

        String accessToken = (String) session.getAttribute("accessToken");

        try {
            geofenceService.deleteGeofence(accessToken);
            redirectAttributes.addFlashAttribute("success", "Geofence removido com sucesso!");
            return "redirect:/area-segura";
        } catch (Exception e) {
            log.error("Erro ao deletar geofence", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar geofence: " + e.getMessage());
            return "redirect:/area-segura";
        }
    }
}
