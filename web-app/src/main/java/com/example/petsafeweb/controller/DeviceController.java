package com.example.petsafeweb.controller;

import com.example.petsafeweb.dto.DeviceRequest;
import com.example.petsafeweb.dto.DeviceResponse;
import com.example.petsafeweb.dto.GeofenceResponse;
import com.example.petsafeweb.dto.LocationResponse;
import com.example.petsafeweb.service.DeviceService;
import com.example.petsafeweb.service.GeofenceService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller para gerenciamento de dispositivos
 */
@Slf4j
@Controller
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final GeofenceService geofenceService;

    public DeviceController(DeviceService deviceService, GeofenceService geofenceService) {
        this.deviceService = deviceService;
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
     * Exibe a página de listagem de dispositivos
     */
    @GetMapping
    public String showDevicesPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String authCheck = checkAuth(session, redirectAttributes);
        if (authCheck != null)
            return authCheck;

        String accessToken = (String) session.getAttribute("accessToken");

        try {
            // VERIFICA SE O USUÁRIO JÁ CADASTROU UM GEOFENCE
            GeofenceResponse geofence = geofenceService.getGeofence(accessToken);
            if (geofence == null) {
                redirectAttributes.addFlashAttribute("error",
                    "Você precisa cadastrar uma Área Segura (Geofence) antes de gerenciar dispositivos.");
                return "redirect:/geofence";
            }

            List<DeviceResponse> devices = deviceService.listDevices(accessToken);

            model.addAttribute("devices", devices != null ? devices : List.of());
            model.addAttribute("deviceRequest", new DeviceRequest());

            return "devices"; // Nome do seu template Thymeleaf/JSP

        } catch (Exception e) {
            log.error("Erro ao carregar página de dispositivos: {}", e.getMessage(), e);

            if (e.getMessage().contains("Sessão expirada")) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/login";
            }

            model.addAttribute("error", e.getMessage());
            model.addAttribute("devices", List.of());
            model.addAttribute("deviceRequest", new DeviceRequest());
            return "devices";
        }
    }

    /**
     * Cria um novo dispositivo
     */
    @PostMapping
    public String createDevice(@ModelAttribute DeviceRequest deviceRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String authCheck = checkAuth(session, redirectAttributes);
        if (authCheck != null)
            return authCheck;

        try {
            // Conversão para null de strings vazias para PATCH (se o DTO for usado para
            // criar E atualizar)
            // Para criação, o serial_number é binding:"required", os outros são opcionais.

            String accessToken = (String) session.getAttribute("accessToken");
            deviceService.createDevice(deviceRequest, accessToken);

            redirectAttributes.addFlashAttribute("success", "Dispositivo cadastrado com sucesso!");
            return "redirect:/devices";

        } catch (Exception e) {
            log.error("Erro ao criar dispositivo", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/devices";
        }
    }

    /**
     * Atualiza um dispositivo existente
     */
    @PostMapping("/{id}/update")
    public String updateDevice(@PathVariable("id") Long id,
            @ModelAttribute DeviceRequest deviceRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String authCheck = checkAuth(session, redirectAttributes);
        if (authCheck != null)
            return authCheck;

        try {
            // Conversão de strings vazias para null (se aplicável aos campos do formulário)
            // Isso garante que o JSON enviado pelo DTO com
            // @JsonInclude(JsonInclude.Include.NON_EMPTY) seja limpo.
            if (deviceRequest.getImei() != null && deviceRequest.getImei().trim().isEmpty()) {
                deviceRequest.setImei(null);
            }
            // Repita para outros campos opcionais de atualização...

            String accessToken = (String) session.getAttribute("accessToken");
            deviceService.updateDevice(id, deviceRequest, accessToken);

            redirectAttributes.addFlashAttribute("success", "Dispositivo atualizado com sucesso!");
            return "redirect:/devices";

        } catch (Exception e) {
            log.error("Erro ao atualizar dispositivo", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/devices";
        }
    }

    /**
     * Exclui um dispositivo
     */
    @PostMapping("/{id}/delete")
    public String deleteDevice(@PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String authCheck = checkAuth(session, redirectAttributes);
        if (authCheck != null)
            return authCheck;

        try {
            String accessToken = (String) session.getAttribute("accessToken");
            deviceService.deleteDevice(id, accessToken);

            redirectAttributes.addFlashAttribute("success", "Dispositivo excluído com sucesso!");
            return "redirect:/devices";

        } catch (Exception e) {
            log.error("Erro ao excluir dispositivo", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/devices";
        }
    }

    /**
     * Exibe a página de detalhes de um dispositivo, incluindo as localizações no
     * mapa
     */
    @GetMapping("/{id}")
    public String showDeviceDetails(@PathVariable("id") Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        String authCheck = checkAuth(session, redirectAttributes);
        if (authCheck != null)
            return authCheck;

        String accessToken = (String) session.getAttribute("accessToken");
        final int API_LIMIT = 10; // Limite de 50 pontos, conforme solicitado
        final int DISPLAY_LIMIT = 3;
        try {
            // 1. Obter Detalhes do Dispositivo (usando /devices/{id}/status)
            DeviceResponse device = deviceService.getDeviceDetails(id, accessToken);

            // 2. Obter Localizações Recentes (usando /devices/{id}/locations/{limit})
            List<LocationResponse> locations = deviceService.listDeviceLocations(id, API_LIMIT, accessToken);

            // Adiciona dados ao modelo para o Thymeleaf
            model.addAttribute("device", device);
            model.addAttribute("locations", locations);
            model.addAttribute("locationLimit", API_LIMIT);
            model.addAttribute("mapDisplayLimit", DISPLAY_LIMIT);

            return "device_details"; // Novo template

        } catch (Exception e) {
            log.error("Erro ao carregar detalhes do dispositivo {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao carregar detalhes: " + e.getMessage());
            return "redirect:/devices";
        }
    }

    /**
     * Retorna a lista de localizações recentes em formato JSON para atualização do
     * mapa.
     */
    @GetMapping("/{id}/locations/json")
    @ResponseBody // Indica que o retorno é o corpo da resposta HTTP (JSON) e não o nome de um
                  // template
    public List<LocationResponse> getDeviceLocationsJson(
            @PathVariable("id") Long id,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            HttpSession session) throws Exception {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null || accessToken.isEmpty()) {
            throw new Exception("Sessão expirada ou não autenticada.");
        }

        // Reusa o método do service. O limite é 50 por padrão ou o que for passado como
        // parâmetro.
        return deviceService.listDeviceLocations(id, limit, accessToken);
    }
}