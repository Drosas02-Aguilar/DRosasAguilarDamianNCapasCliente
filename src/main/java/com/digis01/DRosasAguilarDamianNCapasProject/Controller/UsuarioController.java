package com.digis01.DRosasAguilarDamianNCapasProject.Controller;

import com.digis01.DRosasAguilarDamianNCapasProject.ML.Colonia;
import com.digis01.DRosasAguilarDamianNCapasProject.ML.Direccion;
import com.digis01.DRosasAguilarDamianNCapasProject.ML.ErrorCM;
import com.digis01.DRosasAguilarDamianNCapasProject.ML.Estado;
import com.digis01.DRosasAguilarDamianNCapasProject.ML.Municipio;
import com.digis01.DRosasAguilarDamianNCapasProject.ML.Pais;
import com.digis01.DRosasAguilarDamianNCapasProject.ML.Result;
import com.digis01.DRosasAguilarDamianNCapasProject.ML.Rol;
import com.digis01.DRosasAguilarDamianNCapasProject.ML.Usuario;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.ParameterizedTypeReference;

import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.InputStreamReader;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.function.EntityResponse;

import jakarta.validation.Valid;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("usuario")
public class UsuarioController {

    @Autowired
    private RestTemplate restTemplate;
    // ========================= LISTADO + SEARCH (GET) =========================

@GetMapping
    public String Index(
            Model model,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false, name = "apellidoPaterno") String apellidoPaterno,
            @RequestParam(required = false, name = "apellidoMaterno") String apellidoMaterno,
            @RequestParam(required = false) Integer idRol
    ) {
        // 1) Traer TODO del API de usuarios
        ResponseEntity<Result<List<Usuario>>> responseEntity = restTemplate.exchange(
                "http://localhost:8080/usuarioapi",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Usuario>>>() {}
        );

        List<Usuario> all = Collections.emptyList();
        if (responseEntity.getStatusCode().is2xxSuccessful()
                && responseEntity.getBody() != null
                && responseEntity.getBody().correct
                && responseEntity.getBody().object != null) {
            all = responseEntity.getBody().object;
        }

        // 2) Filtrar en memoria con Stream (nombre, apellidos, rol)
        final int rolFiltro = (idRol == null) ? 0 : idRol;

        List<Usuario> filtrados = all.stream()
                .filter(u -> contains(u.getNombre(), nombre))
                .filter(u -> contains(u.getApellidopaterno(), apellidoPaterno))
                .filter(u -> contains(u.getApellidomaterno(), apellidoMaterno))
                .filter(u -> rolFiltro == 0 || (u.getRol() != null && u.getRol().getIdRol() == rolFiltro))
                .toList();

        model.addAttribute("usuarios", filtrados);

        // 3) Modelo de filtros para recordar valores en el formulario
        Usuario filtro = new Usuario(
                nullToEmpty(nombre),
                nullToEmpty(apellidoPaterno),
                nullToEmpty(apellidoMaterno),
                new Rol()
        );
        filtro.getRol().setIdRol(rolFiltro);
        model.addAttribute("usuariobusqueda", filtro);

        // 4) Roles para el combo
        ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                "http://localhost:8080/rolapi/getall",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Rol>>>() {}
        );
        Result<List<Rol>> rolesRs = rolesResponse.getBody();
        model.addAttribute("roles",
                rolesRs != null && rolesRs.correct && rolesRs.object != null
                        ? rolesRs.object
                        : Collections.emptyList());

        return "UsuarioIndex";
    }

    /* ===== Helpers de búsqueda ===== */
    private static String nullToEmpty(String s) { return (s == null) ? "" : s; }
    private static boolean contains(String campo, String criterio) {
        if (criterio == null || criterio.isBlank()) return true;
        return normalize(campo).contains(normalize(criterio));
    }
    private static String normalize(String s) {
        if (s == null) return "";
        String base = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return base.toLowerCase(Locale.ROOT).trim();
    }
    

    

    // ========================= ACTION (0 = ALTA | >0 = DETAIL) =========================
    @GetMapping("/action/{idUsuario}")
    public String action(Model model, @PathVariable("idUsuario") int idUsuario) {

        if (idUsuario == 0) {
            // Alta (Usuario + primera Dirección)
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(0);

            // Seed direcciones[0] con idDireccion = 0
            Direccion d = new Direccion();
            d.setIdDireccion(0);
            Colonia col = new Colonia();
            Municipio mun = new Municipio();
            Estado est = new Estado();
            Pais pai = new Pais();
            est.setPais(pai);
            mun.setEstado(est);
            col.setMunicipio(mun);
            d.setColonia(col);

            List<Direccion> list = new ArrayList<>();
            list.add(d);
            usuario.setDirecciones(list);

            // Roles y Países
            ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                    "http://localhost:8080/rolapi/getall",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Rol>>>() {
            }
            );
            ResponseEntity<Result<List<Pais>>> paisesResponse = restTemplate.exchange(
                    "http://localhost:8080/catalogoapi/paises",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Pais>>>() {
            }
            );

            model.addAttribute("Usuario", usuario);
            model.addAttribute("roles",
                    (rolesResponse.getStatusCode().is2xxSuccessful()
                    && rolesResponse.getBody() != null
                    && rolesResponse.getBody().correct)
                    ? rolesResponse.getBody().object
                    : Collections.emptyList()
            );
            model.addAttribute("paises",
                    (paisesResponse.getStatusCode().is2xxSuccessful()
                    && paisesResponse.getBody() != null
                    && paisesResponse.getBody().correct)
                    ? paisesResponse.getBody().object
                    : Collections.emptyList()
            );

            return "UsuarioForm";
        } else {
            // Detail (EditarUsuario)
            ResponseEntity<Result<Usuario>> usuarioResp = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/direcciones/{id}",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            },
                    idUsuario
            );

            if (usuarioResp.getStatusCode().is2xxSuccessful()
                    && usuarioResp.getBody() != null
                    && usuarioResp.getBody().correct) {
                model.addAttribute("usuario", usuarioResp.getBody().object);
                return "EditarUsuario";
            }

            return "redirect:/usuario";
        }
    }

    // ========================= FORM EDITABLE  (did) =========================
    @GetMapping("formEditable")
    public String formEditable(@RequestParam int IdUsuario,
            @RequestParam("did") int did,
            Model model) {

        if (did == -1) {
            // Editar información de usuario (sin dirección)
            ResponseEntity<Result<Usuario>> usuarioResp = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/get/{id}",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            },
                    IdUsuario
            );
            if (!usuarioResp.getStatusCode().is2xxSuccessful()
                    || usuarioResp.getBody() == null
                    || !usuarioResp.getBody().correct) {
                return "redirect:/usuario";
            }

            Usuario u = usuarioResp.getBody().object;
            // Semilla direcciones[0] con idDireccion = -1 para el POST
            Direccion d = new Direccion();
            d.setIdDireccion(-1);
            List<Direccion> list = new ArrayList<>();
            list.add(d);
            u.setDirecciones(list);

            ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                    "http://localhost:8080/rolapi/getall",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Rol>>>() {
            }
            );

            model.addAttribute("Usuario", u);
            model.addAttribute("roles",
                    (rolesResponse.getStatusCode().is2xxSuccessful()
                    && rolesResponse.getBody() != null
                    && rolesResponse.getBody().correct)
                    ? rolesResponse.getBody().object
                    : Collections.emptyList()
            );
            return "UsuarioForm";

        } else if (did == 0) {
            // Agregar nueva dirección a un usuario existente
            Usuario u = new Usuario();
            u.setIdUsuario(IdUsuario);

            Direccion d = new Direccion();
            d.setIdDireccion(0);
            Colonia col = new Colonia();
            Municipio mun = new Municipio();
            Estado est = new Estado();
            Pais pai = new Pais();
            est.setPais(pai);
            mun.setEstado(est);
            col.setMunicipio(mun);
            d.setColonia(col);
            List<Direccion> list = new ArrayList<>();
            list.add(d);
            u.setDirecciones(list);

            ResponseEntity<Result<List<Pais>>> paisesResponse = restTemplate.exchange(
                    "http://localhost:8080/catalogoapi/paises",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Pais>>>() {
            }
            );

            model.addAttribute("Usuario", u);
            model.addAttribute("paises",
                    (paisesResponse.getStatusCode().is2xxSuccessful()
                    && paisesResponse.getBody() != null
                    && paisesResponse.getBody().correct)
                    ? paisesResponse.getBody().object
                    : Collections.emptyList()
            );
            return "UsuarioForm";

        } else {
            // Editar dirección existente (did > 0)
            ResponseEntity<Result<Direccion>> dirResp = restTemplate.exchange(
                    "http://localhost:8080/direccionapi/get/{idDireccion}",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Direccion>>() {
            },
                    did
            );
            if (!dirResp.getStatusCode().is2xxSuccessful()
                    || dirResp.getBody() == null
                    || !dirResp.getBody().correct
                    || dirResp.getBody().object == null) {
                return "redirect:/usuario/action/" + IdUsuario;
            }

            Usuario u = new Usuario();
            u.setIdUsuario(IdUsuario);
            List<Direccion> list = new ArrayList<>();
            list.add(dirResp.getBody().object);
            u.setDirecciones(list);

            ResponseEntity<Result<List<Pais>>> paisesResponse = restTemplate.exchange(
                    "http://localhost:8080/catalogoapi/paises",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Pais>>>() {
            }
            );

            model.addAttribute("Usuario", u);
            model.addAttribute("paises",
                    (paisesResponse.getStatusCode().is2xxSuccessful()
                    && paisesResponse.getBody() != null
                    && paisesResponse.getBody().correct)
                    ? paisesResponse.getBody().object
                    : Collections.emptyList()
            );
            return "UsuarioForm";
        }
    }

    // ========================= POST UNIFICADO () =========================
    @PostMapping("add")
    public String Add(@Valid @ModelAttribute("Usuario") Usuario usuario,
            BindingResult bindingResult,
            Model model,
            @RequestParam(name = "userFotoInput", required = false) MultipartFile imagen) {

        // Sentinel seguro (NO comparar con null: getIdDireccion() es int)
        int idUsuario = usuario.getIdUsuario();
        int idDireccion = 0;
        if (usuario.getDirecciones() != null
                && !usuario.getDirecciones().isEmpty()
                && usuario.getDirecciones().get(0) != null) {
            idDireccion = usuario.getDirecciones().get(0).getIdDireccion();
        }

        // ====== 1) ALTA USUARIO (idUsuario == 0 && idDireccion == 0)
        if (idUsuario == 0 && idDireccion == 0) {

            if (bindingResult.hasErrors()) {
                // recargar roles y paises
                ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                        "http://localhost:8080/rolapi/getall",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Result<List<Rol>>>() {
                }
                );
                ResponseEntity<Result<List<Pais>>> paisesResponse = restTemplate.exchange(
                        "http://localhost:8080/catalogoapi/paises",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Result<List<Pais>>>() {
                }
                );
                model.addAttribute("Usuario", usuario);
                model.addAttribute("roles",
                        (rolesResponse.getStatusCode().is2xxSuccessful()
                        && rolesResponse.getBody() != null
                        && rolesResponse.getBody().correct)
                        ? rolesResponse.getBody().object
                        : Collections.emptyList()
                );
                model.addAttribute("paises",
                        (paisesResponse.getStatusCode().is2xxSuccessful()
                        && paisesResponse.getBody() != null
                        && paisesResponse.getBody().correct)
                        ? paisesResponse.getBody().object
                        : Collections.emptyList()
                );
                return "UsuarioForm";
            }

            // imagen (sólo .jpg como tu código original)
            if (imagen != null && !imagen.isEmpty() && imagen.getOriginalFilename() != null) {
                String nombre = imagen.getOriginalFilename();
                String extension = nombre.contains(".") ? nombre.substring(nombre.lastIndexOf('.') + 1) : "";
                if ("jpg".equalsIgnoreCase(extension)) {
                    try {
                        byte[] bytes = imagen.getBytes();
                        String base64Image = java.util.Base64.getEncoder().encodeToString(bytes);
                        usuario.setImagen(base64Image);
                    } catch (Exception ex) {
                        /* swallow */ }
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Usuario> entity = new HttpEntity<>(usuario, headers);

            ResponseEntity<Result<Usuario>> addResp = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/agregar",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            }
            );

            return "redirect:/usuario";

        }

        // ====== 2) EDITAR INFO USUARIO (idUsuario > 0 && idDireccion == -1)
        if (idUsuario > 0 && idDireccion == -1) {

            if (bindingResult.hasErrors()) {
                ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                        "http://localhost:8080/rolapi/getall",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Result<List<Rol>>>() {
                }
                );
                model.addAttribute("Usuario", usuario);
                model.addAttribute("roles",
                        (rolesResponse.getStatusCode().is2xxSuccessful()
                        && rolesResponse.getBody() != null
                        && rolesResponse.getBody().correct)
                        ? rolesResponse.getBody().object
                        : Collections.emptyList()
                );
                return "UsuarioForm";
            }

            if (imagen != null && !imagen.isEmpty() && imagen.getOriginalFilename() != null) {
                String nombre = imagen.getOriginalFilename();
                String extension = nombre.contains(".") ? nombre.substring(nombre.lastIndexOf('.') + 1) : "";
                if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension)) {
                    try {
                        byte[] bytes = imagen.getBytes();
                        String base64Image = java.util.Base64.getEncoder().encodeToString(bytes);
                        usuario.setImagen(base64Image);
                    } catch (Exception ex) {
                        /* swallow */ }
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Usuario> entity = new HttpEntity<>(usuario, headers);

            ResponseEntity<Result<Usuario>> updResp = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/update/{id}",
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            },
                    idUsuario
            );

            return "redirect:/usuario/action/" + idUsuario;
        }

        // ====== 3) AGREGAR DIRECCIÓN (idUsuario > 0 && idDireccion == 0)
        if (idUsuario > 0 && idDireccion == 0) {

            // NO usamos bindingResult (es @Valid Usuario, estamos agregando solo una dirección)
            Direccion d = (usuario.getDirecciones() != null && !usuario.getDirecciones().isEmpty())
                    ? usuario.getDirecciones().get(0)
                    : null;

            if (d == null) {
                return "redirect:/usuario/action/" + idUsuario;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Direccion> entity = new HttpEntity<>(d, headers);

            ResponseEntity<Result<Direccion>> addDirResp = restTemplate.exchange(
                    "http://localhost:8080/direccionapi/usuario/{idUsuario}/agregar",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Result<Direccion>>() {
            },
                    idUsuario
            );

            return "redirect:/usuario/action/" + idUsuario;
        }

        // ====== 4) EDITAR DIRECCIÓN (idUsuario > 0 && idDireccion > 0)
        if (idUsuario > 0 && idDireccion > 0) {

            Direccion d = (usuario.getDirecciones() != null && !usuario.getDirecciones().isEmpty())
                    ? usuario.getDirecciones().get(0)
                    : null;

            if (d == null) {
                return "redirect:/usuario/action/" + idUsuario;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Direccion> entity = new HttpEntity<>(d, headers);

            ResponseEntity<Result<Direccion>> updDirResp = restTemplate.exchange(
                    "http://localhost:8080/direccionapi/update/{idDireccion}",
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<Result<Direccion>>() {
            },
                    idDireccion
            );

            return "redirect:/usuario/action/" + idUsuario;
        }

        return "redirect:/usuario";
    }

    // ========================= ELIMINAR DIRECCIÓN (se mantiene igual) =========================
    @GetMapping("direccion/delete")
    public String DireccionDelete(@RequestParam int idDireccion, @RequestParam int idUsuario) {
        ResponseEntity<Result<Direccion>> deleteResponse = restTemplate.exchange(
                "http://localhost:8080/direccionapi/delete/{idDireccion}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Direccion>>() {
        },
                idDireccion
        );
        // (log opcional)
        return "redirect:/usuario/action/" + idUsuario;
    }
   
    //=================== CARGA MASIVA ===============================


   @GetMapping("cargamasiva")
    public String verCargaMasiva(Model model) {
        if (!model.containsAttribute("uploadOk")) {
            model.addAttribute("uploadOk", false);
        }
        if (!model.containsAttribute("listaErrores")) {
            model.addAttribute("listaErrores", java.util.Collections.emptyList());
        }
        return "CargaMasiva"; // templates/CargaMasiva.html
    }

    // ===== SUBIR ARCHIVO (registra el job en el API) =====
    @PostMapping("cargamasiva")
    public String subirCargaMasiva(@RequestParam("archivo") MultipartFile archivo,
                                   @RequestParam(value = "sobrescribir", defaultValue = "false") boolean sobrescribir,
                                   Model model,
                                   RedirectAttributes ra) {
        try {
            if (archivo == null || archivo.isEmpty()) {
                ra.addFlashAttribute("error", "Selecciona un archivo .xlsx o .txt");
                return "redirect:/usuario/cargamasiva";
            }

            HttpHeaders reqHeaders = new HttpHeaders();
            reqHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            reqHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

            ByteArrayResource filePart = new ByteArrayResource(archivo.getBytes()) {
                @Override public String getFilename() { return archivo.getOriginalFilename(); }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", filePart);
            body.add("sobrescribir", String.valueOf(sobrescribir));

            HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(body, reqHeaders);

            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/cargamasiva",
                    HttpMethod.POST,
                    req,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> job = resp.getBody();
            if (job == null) {
                ra.addFlashAttribute("error", "El API no devolvió respuesta.");
                return "redirect:/usuario/cargamasiva";
            }

            String status = String.valueOf(job.getOrDefault("status", ""));
            if ("ERROR".equalsIgnoreCase(status)) {
                // Mostrar tabla en la misma vista (sin redirigir)
                prepararTablaDesdeUploadError(job, model);
                return "CargaMasiva";
            }

            model.addAttribute("uploadOk", true);
            model.addAttribute("job", job); // debe contener 'id'
            return "CargaMasiva";

        } catch (HttpStatusCodeException ex) {
            // Parsear JSON del API (409, 400, etc.) y renderizar tabla
            Map<String, Object> job = parseJsonSafely(ex.getResponseBodyAsString());
            if (job != null) {
                prepararTablaDesdeUploadError(job, model);
                return "CargaMasiva";
            }
            ra.addFlashAttribute("error", "No se pudo subir el archivo (HTTP " + ex.getRawStatusCode() + ").");
            return "redirect:/usuario/cargamasiva";
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "No se pudo subir el archivo: " + ex.getMessage());
            return "redirect:/usuario/cargamasiva";
        }
    }

    // ===== PROCESAR JOB (ejecuta inserciones en el API) =====
    @PostMapping("cargamasiva/procesar/{id}")
    public String procesarCargaMasiva(@PathVariable String id,
                                      Model model,
                                      RedirectAttributes ra) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> req = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/cargamasiva/procesar/{id}",
                    HttpMethod.POST,
                    req,
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    id
            );

            Map<String, Object> job = resp.getBody();
            if (job == null) {
                ra.addFlashAttribute("error", "Sin respuesta del API al procesar.");
                return "redirect:/usuario/cargamasiva";
            }

            String status = String.valueOf(job.getOrDefault("status", ""));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> errores =
                    (List<Map<String, Object>>) job.getOrDefault("errores", List.of());

            boolean ok = "PROCESADO".equalsIgnoreCase(status) && (errores == null || errores.isEmpty());

            model.addAttribute("archivoCorrecto", ok);
            model.addAttribute("listaErrores", errores != null ? errores : List.of());
            model.addAttribute("resultado", job); // ej: resultado['insertados'], ['ignorados']
            return "CargaMasiva";

        } catch (HttpStatusCodeException ex) {
            Map<String, Object> job = parseJsonSafely(ex.getResponseBodyAsString());
            if (job != null) {
                // También renderiza como tabla si el procesado devolvió error
                String status = String.valueOf(job.getOrDefault("status", ""));
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> errores =
                        (List<Map<String, Object>>) job.getOrDefault("errores", List.of());
                boolean ok = "PROCESADO".equalsIgnoreCase(status) && (errores == null || errores.isEmpty());

                model.addAttribute("archivoCorrecto", ok);
                model.addAttribute("listaErrores", errores != null ? errores : List.of());
                model.addAttribute("resultado", job);
                return "CargaMasiva";
            }
            ra.addFlashAttribute("error", "Error al procesar (HTTP " + ex.getRawStatusCode() + ").");
            return "redirect:/usuario/cargamasiva";
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Error al procesar: " + ex.getMessage());
            return "redirect:/usuario/cargamasiva";
        }
    }

    // ===== Helpers =====

    private void prepararTablaDesdeUploadError(Map<String, Object> job, Model model) {
        String obs = String.valueOf(job.getOrDefault("observacion", "Error al registrar la carga."));
        String filename = String.valueOf(job.getOrDefault("filename", ""));
        String sha1 = String.valueOf(job.getOrDefault("sha1", ""));

        // Resumen para el panel "Resultado"
        Map<String, Object> resumen = Map.of(
                "status", "ERROR",
                "observacion", obs,
                "insertados", 0,
                "actualizados", 0,
                "ignorados", 0,
                "filename", filename,
                "sha1", sha1
        );

        // Tabla: un renglón genérico (porque en UPLOAD el API no manda errores por fila)
        Map<String, Object> renglon = Map.of(
                "fila", "-",
                "campo", "ARCHIVO",
                "mensaje", obs
        );

        model.addAttribute("uploadOk", false);
        model.addAttribute("job", null);
        model.addAttribute("resultado", resumen);
        model.addAttribute("listaErrores", List.of(renglon));
        // Opcional: también puedes pasar un tip para la vista
        model.addAttribute("error", null);
    }

    private Map<String, Object> parseJsonSafely(String json) {
        try {
            if (json == null || json.isBlank()) return null;
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }



//    //=========== SEARCH BUSCADO INNDEX =====================================
//    @PostMapping
//    public String Index(Model model, @ModelAttribute("usuariobusqueda") Usuario usuariobusqueda) {
//
//        if (usuariobusqueda.getRol() == null) {
//            usuariobusqueda.setRol(new Rol());
//        }
//
//        if (usuariobusqueda.getRol().getIdRol() == 0) {
//            usuariobusqueda.getRol().setIdRol(0);
//        }
//        Result result = usuarioDAOImplementation.GetAll(usuariobusqueda);
//
//        Result rolesRs = rolDAOImplementation.GetAllRol();
//
//        // --- Modelo para la vista ---
//        model.addAttribute("usuariobusqueda", usuariobusqueda);
//        model.addAttribute("usuarios", result.correct ? result.objects : null);
//        model.addAttribute("roles", rolesRs.correct ? rolesRs.objects : Collections.emptyList());
//
//        return "UsuarioIndex";
//    }
//
//
//    // ========================= ELIMINAR USUARIO =========================
//    
//    @GetMapping("eliminar")
//    public String Eliminar(@RequestParam("id") int idUsuario){
//        
//        RestTemplate restTemplate =  new RestTemplate();
//        
//        ResponseEntity<Result<Usuario>> usuarioResponse = restTemplate.exchange(
//        
//        "http://localhost:8080/usuarioapi/delete/{idUsuario}",
//                HttpMethod.DELETE,
//                HttpEntity.EMPTY,
//                new ParameterizedTypeReference<Result<Usuario>>(){},
//                idUsuario);
//        if (usuarioResponse.getStatusCode().is2xxSuccessful() &&
//        usuarioResponse.getBody() != null &&
//        usuarioResponse.getBody().correct) {
//        System.out.println("Dirección eliminada con éxito");
//    } else {
//        System.out.println("Error al eliminar la dirección");
//    }
//        
//                return "redirect:/usuario";           
//         
//    
//    }
//    // ========================= CASCADAS (CATÁLOGOS) =========================
//    @GetMapping("getPaises")
//    @ResponseBody
//    public Map<String, Object> getPaises() {
//        RestTemplate rt = new RestTemplate();
//
//        ResponseEntity<Result<List<Pais>>> resp = rt.exchange(
//                "http://localhost:8080/catalogoapi/paises",
//                HttpMethod.GET,
//                HttpEntity.EMPTY,
//                new ParameterizedTypeReference<Result<List<Pais>>>() {
//        }
//        );
//
//        Map<String, Object> out = new LinkedHashMap<>();
//        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
//            List<Pais> lista = resp.getBody().object;
//            out.put("correct", lista != null);
//            out.put("objects", lista != null ? lista : Collections.emptyList());
//            return out;
//        }
//        out.put("correct", false);
//        out.put("objects", Collections.emptyList());
//        return out;
//    }
//
//    @GetMapping("getEstadosByPais")
//    @ResponseBody
//    public Map<String, Object> getEstadosByPais(@RequestParam("IdPais") int IdPais) {
//        RestTemplate rt = new RestTemplate();
//
//        ResponseEntity<Result<List<Estado>>> resp = rt.exchange(
//                "http://localhost:8080/catalogoapi/estados/{idPais}",
//                HttpMethod.GET,
//                HttpEntity.EMPTY,
//                new ParameterizedTypeReference<Result<List<Estado>>>() {
//        },
//                IdPais
//        );
//
//        Map<String, Object> out = new LinkedHashMap<>();
//        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
//            List<Estado> lista = resp.getBody().object;
//            out.put("correct", lista != null);
//            out.put("objects", lista != null ? lista : Collections.emptyList());
//            return out;
//        }
//        out.put("correct", false);
//        out.put("objects", Collections.emptyList());
//        return out;
//    }
//
//    @GetMapping("MunicipiosGetByIdEstado")
//    @ResponseBody
//    public Map<String, Object> municipioByIdEstado(@RequestParam("IdEstado") int IdEstado) {
//        RestTemplate rt = new RestTemplate();
//
//        ResponseEntity<Result<List<Municipio>>> resp = rt.exchange(
//                "http://localhost:8080/catalogoapi/municipios/{idEstado}",
//                HttpMethod.GET,
//                HttpEntity.EMPTY,
//                new ParameterizedTypeReference<Result<List<Municipio>>>() {
//        },
//                IdEstado
//        );
//
//        Map<String, Object> out = new LinkedHashMap<>();
//        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
//            List<Municipio> lista = resp.getBody().object;
//            out.put("correct", lista != null);
//            out.put("objects", lista != null ? lista : Collections.emptyList());
//            return out;
//        }
//        out.put("correct", false);
//        out.put("objects", Collections.emptyList());
//        return out;
//    }
//
//    @GetMapping("ColoniasGetByIdMunicipio")
//    @ResponseBody
//    public Map<String, Object> ColoniaGetByIdMunicipio(@RequestParam("IdMunicipio") int IdMunicipio) {
//        RestTemplate rt = new RestTemplate();
//
//        ResponseEntity<Result<List<Colonia>>> resp = rt.exchange(
//                "http://localhost:8080/catalogoapi/colonias/{idMunicipio}",
//                HttpMethod.GET,
//                HttpEntity.EMPTY,
//                new ParameterizedTypeReference<Result<List<Colonia>>>() {
//        },
//                IdMunicipio
//        );
//
//        Map<String, Object> out = new LinkedHashMap<>();
//        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
//            List<Colonia> lista = resp.getBody().object;
//            out.put("correct", lista != null);
//            out.put("objects", lista != null ? lista : Collections.emptyList());
//            return out;
//        }
//        out.put("correct", false);
//        out.put("objects", Collections.emptyList());
//        return out;
//    }
    // ========================= ELIMINAR DIRECCIÓN =========================
//    @GetMapping("direccion/delete")
//public String DireccionDelete(@RequestParam int idDireccion, @RequestParam int idUsuario) {
//    RestTemplate restTemplate = new RestTemplate();                            
//
//    ResponseEntity<Result<Direccion>> deleteResponse = restTemplate.exchange(
//            "http://localhost:8080/direccionapi/delete/{idDireccion}",
//            HttpMethod.DELETE,
//            HttpEntity.EMPTY,
//            new ParameterizedTypeReference<Result<Direccion>>() {},
//            idDireccion
//    );
//
//    // Podrías validar si realmente se eliminó
//    if (deleteResponse.getStatusCode().is2xxSuccessful() &&
//        deleteResponse.getBody() != null &&
//        deleteResponse.getBody().correct) {
//        System.out.println("Dirección eliminada con éxito");
//    } else {
//        System.out.println("Error al eliminar la dirección");
//    }
//
//        return "redirect:/usuario/editarUsuario?idUsuario=" + idUsuario;
//}
    //============== STATUS =========================
    //    @PatchMapping("{id}/status")
//    @ResponseBody
//    public java.util.Map<String, Object> toggleStatus(@PathVariable int id,
//            @RequestParam boolean activo,
//            java.security.Principal principal) {
//        String ub = (principal != null) ? principal.getName() : "system";
//        Result r = usuarioJPADAO.SetActivo(id, activo, ub);
//
//        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
//        resp.put("ok", r.correct);
//        resp.put("activo", activo);
//        resp.put("msg", r.errorMessage); // puede ser null y no truena
//        return resp;
//    }
//
//    // ============= CARGA MASIVA DE DATOS ======================
//    @GetMapping("cargamasiva")
//    public String CargaMasiva() {
//        return "CargaMasiva";
//    }
//
//    @PostMapping("cargamasiva")
//    public String CargaMasiva(@RequestParam("archivo") MultipartFile file, Model model, HttpSession session) {
//
//        model.addAttribute("archivoCorrecto", false);
//
//        String root = System.getProperty("user.dir");
//        String rutaArchivo = "/src/main/resources/archivos/";
//
//        // usa segundos correctos 'ss'
//        String fechaSubida = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//        String rutaFinal = root + rutaArchivo + fechaSubida + "_" + file.getOriginalFilename();
//
//        try {
//            // asegúrate de que exista el directorio
//            File dir = new File(root + rutaArchivo);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            file.transferTo(new File(rutaFinal));
//        } catch (Exception ex) {
//            System.out.println("Error al guardar archivo: " + ex.getMessage());
//            model.addAttribute("listaErrores", List.of(new ErrorCM(0, "", "No se pudo guardar el archivo.")));
//            return "CargaMasiva";
//        }
//
//        List<Usuario> usuarios;
//        List<ErrorCM> errores;
//
//        // Detecta extensión de forma segura
//        String[] parts = file.getOriginalFilename().split("\\.");
//        String ext = parts.length > 1 ? parts[parts.length - 1].toLowerCase() : "";
//
//        if ("txt".equals(ext)) {
//            usuarios = ProcesarTXT(new File(rutaFinal));
//        } else if ("xlsx".equals(ext)) {
//            usuarios = ProcesarExcel(new File(rutaFinal));
//        } else {
//            model.addAttribute("listaErrores", List.of(new ErrorCM(0, ext, "Extensión no soportada")));
//            return "CargaMasiva";
//        }
//
//        errores = ValidarDatos(usuarios);
//
//        if (errores.isEmpty()) {
//            model.addAttribute("listaErrores", errores);
//            model.addAttribute("archivoCorrecto", true);
//
//            session.setAttribute("path", rutaFinal);
//
//        } else {
//            model.addAttribute("listaErrores", errores);
//            model.addAttribute("archivoCorrecto", false);
//            // por seguridad, limpia el path si hay errores
//            session.removeAttribute("path");
//        }
//
//        return "CargaMasiva";
//    }
//
//    @GetMapping("cargamasiva/procesar")
//    public String CargaMasivaProcesar(HttpSession session) {
//        try {
//            Object pathObj = session.getAttribute("path");
//            if (pathObj == null) {
//                // No hay archivo listo para procesar (probablemente no pasaste validación o no se guardó el path)
//                return "redirect:/usuario/cargamasiva";
//            }
//
//            String ruta = pathObj.toString();
//            List<Usuario> usuarios;
//
//            String ext = ruta.contains(".") ? ruta.substring(ruta.lastIndexOf('.') + 1).toLowerCase() : "";
//
//            if ("txt".equals(ext)) {
//                usuarios = ProcesarTXT(new File(ruta));
//            } else if ("xlsx".equals(ext)) {
//                usuarios = ProcesarExcel(new File(ruta));
//            } else {
//                // extensión inesperada
//                return "redirect:/usuario/cargamasiva";
//            }
//
//            if (usuarios != null) {
//                for (Usuario usuario : usuarios) {
//                    // usuarioDAOImplementation.Add(usuario);
//
//                    //      Result result = usuarioJPADAOImplementation.Add(usuario);
//                    Result result = usuarioJPADAOImplementation.Add(usuario);
//                }
//            }
//
//            // Limpia la sesión al terminar
//            session.removeAttribute("path");
//
//        } catch (Exception ex) {
//            System.out.println("Error en procesar: " + ex.getLocalizedMessage());
//        }
//
//        return "redirect:/usuario";
//    }
//
//    private List<Usuario> ProcesarTXT(File file) {
//        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
//
//            String linea;
//            List<Usuario> usuarios = new ArrayList<>();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            sdf.setLenient(false);
//
//            while ((linea = bufferedReader.readLine()) != null) {
//                String[] campos = linea.split("\\|");
//
//                Usuario usuario = new Usuario();
//                usuario.setNombre(campos[0]);
//                usuario.setApellidopaterno(campos[1]);
//                usuario.setApellidomaterno(campos[2]);
//                usuario.setUsername(campos[3]);
//                usuario.setEmail(campos[4]);
//                usuario.setPassword(campos[5]);
//
//                // Fecha Nacimiento (yyyy-MM-dd)
//                try {
//                    if (campos[6] != null && !campos[6].trim().isEmpty()) {
//                        usuario.setFechaNacimiento(sdf.parse(campos[6].trim()));
//                    }
//                } catch (Exception ex) {
//                    usuario.setFechaNacimiento(null);
//                }
//
//                usuario.setSexo(campos[7]);
//                usuario.setTelefono(campos[8]);
//                usuario.setCelular(campos[9]);
//                usuario.setCurp(campos[10]);
//                usuario.setTiposangre(campos[11]);
//
//                // Rol
//                Rol rol = new Rol();
//                try {
//                    rol.setIdRol(Integer.parseInt(campos[12]));
//                } catch (Exception ex) {
//                    rol.setIdRol(0);
//                }
//                usuario.setRol(rol);
//
//                // Dirección
//                Direccion direccion = new Direccion();
//                direccion.setCalle(campos[13]);
//                direccion.setNumeroExterior(campos[14]);
//                direccion.setNumeroInterior(campos[15]);
//
//                Colonia colonia = new Colonia();
//                try {
//                    colonia.setIdColonia(Integer.parseInt(campos[16]));
//                } catch (Exception ex) {
//                    colonia.setIdColonia(0);
//                }
//                direccion.setColonia(colonia);
//
//                List<Direccion> direcciones = new ArrayList<>();
//                direcciones.add(direccion);
//                usuario.setDirecciones(direcciones);
//
//                usuarios.add(usuario);
//            }
//
//            return usuarios;
//        } catch (Exception ex) {
//            System.out.println("Error al procesar archivo: " + ex.getMessage());
//            return null;
//        }
//    }
//
//    private List<Usuario> ProcesarExcel(File file) {
//        List<Usuario> usuarios = new ArrayList<>();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        sdf.setLenient(false);
//
//        try (FileInputStream fis = new FileInputStream(file); XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
//
//            Sheet sheet = workbook.getSheetAt(0);
//            DataFormatter fmt = new DataFormatter();
//
//            boolean primeraFila = true;
//            for (Row row : sheet) {
//                if (row == null) {
//                    continue;
//                }
//                if (primeraFila) {
//                    primeraFila = false;
//                    continue;
//                } // salta encabezados
//
//                Usuario usuario = new Usuario();
//                usuario.setNombre(fmt.formatCellValue(row.getCell(0)));
//                usuario.setApellidopaterno(fmt.formatCellValue(row.getCell(1)));
//                usuario.setApellidomaterno(fmt.formatCellValue(row.getCell(2)));
//                usuario.setUsername(fmt.formatCellValue(row.getCell(3)));
//                usuario.setEmail(fmt.formatCellValue(row.getCell(4)));
//                usuario.setPassword(fmt.formatCellValue(row.getCell(5)));
//
//                // Fecha de nacimiento (col 6)
//                Cell cFecha = row.getCell(6);
//                if (cFecha != null) {
//                    try {
//                        if (cFecha.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cFecha)) {
//                            usuario.setFechaNacimiento(cFecha.getDateCellValue());
//                        } else {
//                            String v = fmt.formatCellValue(cFecha).trim();
//                            if (!v.isEmpty()) {
//                                usuario.setFechaNacimiento(sdf.parse(v));
//                            }
//                        }
//                    } catch (Exception ignore) {
//                        usuario.setFechaNacimiento(null);
//                    }
//                }
//
//                usuario.setSexo(fmt.formatCellValue(row.getCell(7)));
//                usuario.setTelefono(fmt.formatCellValue(row.getCell(8)));
//                usuario.setCelular(fmt.formatCellValue(row.getCell(9)));
//                usuario.setCurp(fmt.formatCellValue(row.getCell(10)));
//                usuario.setTiposangre(fmt.formatCellValue(row.getCell(11)));
//
//                // Rol (col 12)
//                Rol rol = new Rol();
//                try {
//                    Cell cRol = row.getCell(12);
//                    int idRol = 0;
//                    if (cRol != null) {
//                        if (cRol.getCellType() == CellType.NUMERIC) {
//                            idRol = (int) cRol.getNumericCellValue();
//                        } else {
//                            String s = fmt.formatCellValue(cRol).trim();
//                            if (!s.isEmpty()) {
//                                idRol = Integer.parseInt(s);
//                            }
//                        }
//                    }
//                    rol.setIdRol(idRol);
//                } catch (Exception ignore) {
//                    rol.setIdRol(0);
//                }
//                usuario.setRol(rol);
//
//                // Dirección (13..16)
//                Direccion direccion = new Direccion();
//                direccion.setCalle(fmt.formatCellValue(row.getCell(13)));
//                direccion.setNumeroInterior(fmt.formatCellValue(row.getCell(14)));
//                direccion.setNumeroExterior(fmt.formatCellValue(row.getCell(15))); // <-- corregido
//
//                Colonia colonia = new Colonia();
//                try {
//                    Cell cCol = row.getCell(16);
//                    int idCol = 0;
//                    if (cCol != null) {
//                        if (cCol.getCellType() == CellType.NUMERIC) {
//                            idCol = (int) cCol.getNumericCellValue();
//                        } else {
//                            String s = fmt.formatCellValue(cCol).trim();
//                            if (!s.isEmpty()) {
//                                idCol = Integer.parseInt(s);
//                            }
//                        }
//                    }
//                    colonia.setIdColonia(idCol);
//                } catch (Exception ignore) {
//                    colonia.setIdColonia(0);
//                }
//                direccion.setColonia(colonia); // <-- usar setter, no campo público
//
//                List<Direccion> direcciones = new ArrayList<>();
//                direcciones.add(direccion);
//                usuario.setDirecciones(direcciones);
//
//                usuarios.add(usuario);
//            }
//
//            return usuarios;
//
//        } catch (Exception ex) {
//            System.out.println("error: " + ex.getMessage());
//            return null;
//        }
//    }
//
//    private List<ErrorCM> ValidarDatos(List<Usuario> usuarios) {
//
//        List<ErrorCM> errores = new ArrayList<>();
//        if (usuarios == null) {
//            return errores;
//
//        }
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        sdf.setLenient(false);
//
//        int linea = 1;
//        for (Usuario u : usuarios) {
//
//            // Obligatorios básicos
//            if (u.getNombre() == null || u.getNombre() == "") {
//                ErrorCM errorCM = new ErrorCM(linea, u.getNombre(), "Campo obligatorio: Nombre");
//                errores.add(errorCM);
//            }
//            if (u.getApellidopaterno() == null || u.getApellidopaterno() == "") {
//                ErrorCM errorCM = new ErrorCM(linea, u.getApellidopaterno(), "Campo obligatorio: ApellidoPaterno");
//                errores.add(errorCM);
//            }
//            if (u.getApellidomaterno() == null || u.getApellidomaterno() == "") {
//                ErrorCM errorCM = new ErrorCM(linea, u.getApellidomaterno(), "Campo obligatorio: ApellidoMaterno");
//                errores.add(errorCM);
//            }
//            if (u.getUsername() == null || u.getUsername() == "") {
//                ErrorCM errorCM = new ErrorCM(linea, u.getUsername(), "Campo obligatorio: Username");
//                errores.add(errorCM);
//            }
//            if (u.getEmail() == null || u.getEmail() == "") {
//                ErrorCM errorCM = new ErrorCM(linea, u.getEmail(), "Campo obligatorio: Email");
//                errores.add(errorCM);
//            } else {
//                String e = u.getEmail();
//                if (e.indexOf('@') == -1 || e.lastIndexOf('.') < e.indexOf('@') + 1) {
//                    ErrorCM errorCM = new ErrorCM(linea, u.getEmail(), "Formato inválido: Email");
//                    errores.add(errorCM);
//                }
//            }
//            if (u.getPassword() == null || u.getPassword() == "") {
//                ErrorCM errorCM = new ErrorCM(linea, u.getPassword(), "Campo obligatorio: Password");
//                errores.add(errorCM);
//            }
//
//            if (u.getFechaNacimiento() != null) {
//                try {
//                    sdf.format(u.getFechaNacimiento());
//                } catch (Exception ex) {
//                    ErrorCM errorCM = new ErrorCM(linea, "", "Fecha inválida (yyyy-MM-dd)");
//                    errores.add(errorCM);
//                }
//            }
//
//            // Sexo
//            if (u.getSexo() == null || u.getSexo() == "") {
//                ErrorCM errorCM = new ErrorCM(linea, u.getSexo(), "Campo obligatorio: Sexo");
//                errores.add(errorCM);
//            } else {
//                String sx = u.getSexo();
//                if (!"M".equalsIgnoreCase(sx) && !"F".equalsIgnoreCase(sx)) {
//                    ErrorCM errorCM = new ErrorCM(linea, u.getSexo(), "Sexo debe ser 'M' o 'F'");
//                    errores.add(errorCM);
//                }
//            }
//
//            // Teléfono y Celular (numéricos si vienen)
//            if (u.getTelefono() != null && u.getTelefono() != "") {
//                try {
//                    Long.parseLong(u.getTelefono());
//                } catch (Exception ex) {
//                    ErrorCM errorCM = new ErrorCM(linea, u.getTelefono(), "Teléfono debe ser numérico");
//                    errores.add(errorCM);
//                }
//            }
//            if (u.getCelular() != null && u.getCelular() != "") {
//                try {
//                    Long.parseLong(u.getCelular());
//                } catch (Exception ex) {
//                    ErrorCM errorCM = new ErrorCM(linea, u.getCelular(), "Celular debe ser numérico");
//                    errores.add(errorCM);
//                }
//            }
//
//            // CURP (opcional, solo valida longitud si viene)
//            if (u.getCurp() != null && u.getCurp() != "") {
//                if (u.getCurp().length() != 18) {
//                    ErrorCM errorCM = new ErrorCM(linea, u.getCurp(), "CURP debe tener 18 caracteres");
//                    errores.add(errorCM);
//                }
//            }
//
//            // Rol
//            if (u.getRol() == null || u.getRol().getIdRol() <= 0) {
//                String rolVal = (u.getRol() == null) ? "" : String.valueOf(u.getRol().getIdRol());
//                ErrorCM errorCM = new ErrorCM(linea, rolVal, "Campo obligatorio: IdRol > 0");
//                errores.add(errorCM);
//            }
//
//            // Dirección
//            if (u.getDirecciones() == null || u.getDirecciones().isEmpty()) {
//                ErrorCM errorCM = new ErrorCM(linea, "", "Debe existir al menos una dirección");
//                errores.add(errorCM);
//            } else {
//                Direccion d = u.getDirecciones().get(0);
//                if (d.getCalle() == null || d.getCalle() == "") {
//                    ErrorCM errorCM = new ErrorCM(linea, d.getCalle(), "Campo obligatorio: Calle");
//                    errores.add(errorCM);
//                }
//                if (d.getNumeroExterior() == null || d.getNumeroExterior() == "") {
//                    ErrorCM errorCM = new ErrorCM(linea, d.getNumeroExterior(), "Campo obligatorio: NumeroExterior");
//                    errores.add(errorCM);
//                }
//                if (d.getColonia() == null || d.getColonia().getIdColonia() <= 0) {
//                    String colVal = (d.getColonia() == null) ? "" : String.valueOf(d.getColonia().getIdColonia());
//                    ErrorCM errorCM = new ErrorCM(linea, colVal, "Campo obligatorio: IdColonia > 0");
//                    errores.add(errorCM);
//                }
//            }
//
//            linea++;
//        }
//
//        return errores;
//    }
//
//
//
}
