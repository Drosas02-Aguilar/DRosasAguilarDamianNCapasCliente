package com.digis01.DRosasAguilarDamianNCapasProject.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/usuario")
public class LoginController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpSession session;
    private final String apiBase;

    public LoginController(HttpSession session,
                           @Value("${api.base:http://localhost:8080}") String apiBase) {
        this.session = session;
        this.apiBase = apiBase;
    }

    /* ========================= LOGIN VIEW ========================= */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        // Si ya hay token válido, redirige según rol
        String redirect = redirectIfAlreadyAuthenticated();
        if (redirect != null) return redirect;

        if (StringUtils.hasText(error))  model.addAttribute("error", error);
        if (StringUtils.hasText(logout)) model.addAttribute("logout", "Has cerrado sesión correctamente.");
        return "login";
    }

    /* ========================= DO LOGIN ========================= */
    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> body = Map.of("username", username, "password", password);
            HttpEntity<Map<String, String>> req = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    apiBase + "/auth/login",
                    HttpMethod.POST,
                    req,
                    new ParameterizedTypeReference<String>() {}
            );

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null || resp.getBody().isBlank()) {
                model.addAttribute("error", "Credenciales inválidas.");
                return "login";
            }

            String jwt = resp.getBody().replace("\"","").trim(); // por si el server lo mandó entre comillas

            if (!isJwtNotExpired(jwt)) {
                model.addAttribute("error", "Token inválido o expirado.");
                return "login";
            }

            // === Decodificar claims (role, userId) ===
            Map<String, Object> payload = decodePayload(jwt);
            String role = String.valueOf(payload.getOrDefault("role", "USER"));
            Integer userId = extractUserId(payload);

            // === Guardar en sesión ===
            session.setAttribute("JWT_TOKEN", jwt);
            session.setAttribute("AUTH_USER", username);
            session.setAttribute("ROLE", role);
            if (userId != null) session.setAttribute("AUTH_USER_ID", userId);

            // === Redirigir según rol ===
            if ("ADMIN".equalsIgnoreCase(role)) {
                return "redirect:/usuario"; // listado
            } else {
                if (userId != null) return "redirect:/usuario/action/" + userId; // detalle propio
                return "redirect:/usuario/403"; // si faltara userId por alguna razón
            }

        } catch (Exception ex) {
            model.addAttribute("error", "No se pudo iniciar sesión: " + ex.getMessage());
            return "login";
        }
    }

    /* ========================= LOGOUT ========================= */
    @PostMapping("/logout")
    public String doLogoutPost() {
        clearSession();
        return "redirect:/usuario/login";
    }

    @GetMapping("/logout")
    public String doLogoutGet() {
        clearSession();
        return "redirect:/usuario/login";
    }

    /* ========================= TOKEN CHECK (opcional) ========================= */
    @ResponseBody
    @GetMapping("/token/validate")
    public ResponseEntity<?> validateToken() {
        return hasValidToken()
                ? ResponseEntity.ok(Map.of("valid", true))
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
    }

    /* ========================= HELPERS ========================= */
    private String redirectIfAlreadyAuthenticated() {
        Object tk = session.getAttribute("JWT_TOKEN");
        if (tk == null) return null;

        String jwt = tk.toString();
        if (!isJwtNotExpired(jwt)) {
            clearSession();
            return null;
        }

        // Lee de sesión si ya tenemos role/userId; si no, decódelos del token
        String role = (String) session.getAttribute("ROLE");
        Integer userId = (Integer) session.getAttribute("AUTH_USER_ID");
        if (role == null || userId == null) {
            Map<String, Object> payload = decodePayload(jwt);
            if (role == null) role = String.valueOf(payload.getOrDefault("role", "USER"));
            if (userId == null) userId = extractUserId(payload);
            if (role != null) session.setAttribute("ROLE", role);
            if (userId != null) session.setAttribute("AUTH_USER_ID", userId);
        }

        if ("ADMIN".equalsIgnoreCase(role)) return "redirect:/usuario";
        if (userId != null) return "redirect:/usuario/action/" + userId;
        return null; // deja entrar a la vista login si faltaran datos
    }

    private void clearSession() {
        session.removeAttribute("JWT_TOKEN");
        session.removeAttribute("AUTH_USER");
        session.removeAttribute("ROLE");
        session.removeAttribute("AUTH_USER_ID");
        try { session.invalidate(); } catch (IllegalStateException ignore) {}
    }

    private boolean hasValidToken() {
        Object tk = session.getAttribute("JWT_TOKEN");
        if (tk == null) return false;
        String jwt = tk.toString();
        if (!isJwtNotExpired(jwt)) {
            clearSession();
            return false;
        }
        return true;
    }

    private boolean isJwtNotExpired(String jwt) {
        try {
            Map<String, Object> payload = decodePayload(jwt);
            Object expObj = payload.get("exp");
            if (expObj == null) return true;
            long expSeconds = (expObj instanceof Number) ? ((Number) expObj).longValue()
                    : Long.parseLong(expObj.toString());
            long now = Instant.now().getEpochSecond();
            return now + 5 < expSeconds; // margen de 5 seg
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> decodePayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) throw new IllegalArgumentException("JWT inválido");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return mapper.readValue(payloadJson, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo decodificar el JWT", e);
        }
    }

    private Integer extractUserId(Map<String, Object> payload) {
        Object uid = payload.get("userId");
        if (uid == null) return null;
        if (uid instanceof Number) return ((Number) uid).intValue();
        try { return Integer.parseInt(uid.toString()); } catch (NumberFormatException e) { return null; }
    }
}
