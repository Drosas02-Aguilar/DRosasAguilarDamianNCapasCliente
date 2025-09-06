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

import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.InputStreamReader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

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

@Controller
@RequestMapping("usuario")
public class UsuarioController {

    @Autowired
    private RestTemplate restTemplate;

    // ========================= LISTADO =========================
    @GetMapping
    public String Index(Model model) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<List<Usuario>>> responseEntity = restTemplate.exchange(
                "http://localhost:8080/usuarioapi",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Usuario>>>() {
        }
        );

        if (responseEntity.getStatusCode() == HttpStatusCode.valueOf(200)) {

            Result<List<Usuario>> result = responseEntity.getBody();

            model.addAttribute("usuarios", result != null && result.correct ? result.object : null);

            // filtro
            Usuario filtro = new Usuario("", "", "", new Rol());
            filtro.getRol().setIdRol(0);
            model.addAttribute("usuariobusqueda", filtro);

            // roles igual (Result<List<Rol>>) → lee 'object'
            ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                    "http://localhost:8080/rolapi/getall",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Rol>>>() {
            }
            );
            Result<List<Rol>> rolesRs = rolesResponse.getBody();
            model.addAttribute("roles", rolesRs != null && rolesRs.correct ? rolesRs.object : java.util.Collections.emptyList());

        }

        return "UsuarioIndex";
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
    // ========================= NUEVO USUARIO (FORM COMPLETO) =========================    
    @GetMapping("add")
    public String add(Model model) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                "http://localhost:8080/rolapi/getall",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Rol>>>() {
        });

        ResponseEntity<Result<List<Pais>>> paisesResponse = restTemplate.exchange(
                "http://localhost:8080/catalogoapi/paises",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Pais>>>() {
        });

        Usuario usuario = new Usuario();

        model.addAttribute("Usuario", usuario);
        model.addAttribute("roles",
                (rolesResponse.getStatusCode() == HttpStatusCode.valueOf(200)
                && rolesResponse.getBody() != null
                && rolesResponse.getBody().correct)
                ? rolesResponse.getBody().object
                : java.util.Collections.emptyList()
        );
        model.addAttribute("paises",
                (paisesResponse.getStatusCode() == HttpStatusCode.valueOf(200)
                && paisesResponse.getBody() != null
                && paisesResponse.getBody().correct)
                ? paisesResponse.getBody().object
                : java.util.Collections.emptyList()
        );
        model.addAttribute("mode", "full");
        model.addAttribute("action", "add");

        return "UsuarioForm";

    }

    // ========================= GUARDAR NUEVO USUARIO =========================
    @PostMapping("add")
    public String Add(@Valid Usuario usuario, BindingResult br, Model model, @RequestParam("userFotoInput") MultipartFile imagen) {

        RestTemplate restTemplade = new RestTemplate();

        if (br.hasErrors()) {

            ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplade.exchange(
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

            model.addAttribute("roles",
                    (rolesResponse.getStatusCode() == HttpStatusCode.valueOf(200)
                    && rolesResponse.getBody() != null
                    && rolesResponse.getBody().correct)
                    ? rolesResponse.getBody().object
                    : Collections.emptyList()
            );

            model.addAttribute("paises",
                    (paisesResponse.getStatusCode() == HttpStatusCode.valueOf(200)
                    && paisesResponse.getBody() != null
                    && paisesResponse.getBody().correct)
                    ? paisesResponse.getBody().object
                    : Collections.emptyList()
            );

            model.addAttribute("mode", "full");
            model.addAttribute("action", "add");
            model.addAttribute("Usuario", usuario);
            return "UsuarioForm";
        } else {

            if (imagen != null) {
                String nombre = imagen.getOriginalFilename();
                String extension = nombre != null ? nombre.split("\\.")[1] : "";
                if (extension.equals("jpg")) {
                    try {
                        byte[] bytes = imagen.getBytes();
                        String base64Image = java.util.Base64.getEncoder().encodeToString(bytes);
                        usuario.setImagen(base64Image);
                    } catch (Exception ex) {
                        System.out.println("Error");
                    }
                }
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Usuario> entity = new HttpEntity<>(usuario, headers);

            ResponseEntity<Result<Usuario>> usaurioResponse = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/agregar",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            }
            );
            return "redirect:/usuario";
        }
    }

// ========================= EDITAR USUARIO (VISTA DETALLE/EDICIÓN) =========================
    @GetMapping("editarUsuario")
    public String EditarUsuario(@RequestParam("idUsuario") int idUsuario, Model model) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<Usuario>> usaurioResponse = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/direcciones/{id}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Usuario>>() {
        },
                idUsuario);

        if (!usaurioResponse.getStatusCode().is2xxSuccessful() || usaurioResponse.getBody() == null || !usaurioResponse.getBody().correct) {
            return "redirect:/usuarios";
        }

        Usuario usuario = usaurioResponse.getBody().object;

        ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                "http://localhost:8080/rolapi/getall",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Rol>>>() {
        }
        );

        model.addAttribute("roles",
                (rolesResponse.getStatusCode().is2xxSuccessful()
                && rolesResponse.getBody() != null
                && rolesResponse.getBody().correct)
                ? rolesResponse.getBody().object
                : java.util.Collections.emptyList()
        );

        model.addAttribute("usuario", usuario);

        if (usuario.getDirecciones() == null || usuario.getDirecciones().isEmpty()) {

            ResponseEntity<Result<List<Pais>>> paisesResponse = restTemplate.exchange(
                    "http://localhost:8080/catalogoapi/paises",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Pais>>>() {
            }
            );

            model.addAttribute("paises",
                    (paisesResponse.getStatusCode().is2xxSuccessful()
                    && paisesResponse.getBody() != null
                    && paisesResponse.getBody().correct)
                    ? paisesResponse.getBody().object
                    : java.util.Collections.emptyList()
            );

            Direccion direccion = new Direccion();
            Colonia colonia = new Colonia();
            Municipio municipio = new Municipio();
            Estado estado = new Estado();
            Pais pais = new Pais();

            estado.setPais(pais);
            municipio.setEstado(estado);
            colonia.setMunicipio(municipio);
            direccion.setColonia(colonia);

            model.addAttribute("direccion", direccion);
            model.addAttribute("mode", "direccion");
            model.addAttribute("action", "add");
            return "UsuarioForm";
        }

        return "EditarUsuario";

    }

//    // ========================= EDITAR SOLO INFO DE USUARIO =========================
    @GetMapping("editarInfo")
    public String EditarInfoUsuario(@RequestParam("idUsuario") int idUsuario, Model model) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<Usuario>> usuarioResp = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/get/{id}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Usuario>>() {
        },
                idUsuario
        );

        if (usuarioResp.getStatusCode() != HttpStatusCode.valueOf(200)
                || usuarioResp.getBody() == null
                || !usuarioResp.getBody().correct
                || usuarioResp.getBody().object == null) {
            return "Error";
        }

        Usuario usuario = usuarioResp.getBody().object;

        ResponseEntity<Result<List<Rol>>> rolesResp = restTemplate.exchange(
                "http://localhost:8080/rolapi/getall",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Rol>>>() {
        }
        );

        model.addAttribute("Usuario", usuario);
        model.addAttribute("roles",
                (rolesResp.getStatusCode() == HttpStatusCode.valueOf(200)
                && rolesResp.getBody() != null
                && rolesResp.getBody().correct)
                ? rolesResp.getBody().object
                : java.util.Collections.emptyList()
        );
        model.addAttribute("mode", "usuario");
        model.addAttribute("action", "edit");

        return "UsuarioForm";
    }

    // ========================= ACTUALIZAR INFO DE USUARIO =========================
    @PostMapping("update")
    public String Update(@Valid @ModelAttribute("Usuario") Usuario usuario,
            BindingResult bindingResult,
            Model model,
            @RequestParam("userFotoInput") MultipartFile imagen) {

        RestTemplate restTemplate = new RestTemplate();

        if (bindingResult.hasErrors()) {

            ResponseEntity<Result<List<Rol>>> rolesResponse = restTemplate.exchange(
                    "http://localhost:8080/rolapi/getall",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Rol>>>() {
            }
            );

            model.addAttribute("roles",
                    (rolesResponse.getStatusCode() == HttpStatusCode.valueOf(200)
                    && rolesResponse.getBody() != null
                    && rolesResponse.getBody().correct)
                    ? rolesResponse.getBody().object
                    : java.util.Collections.emptyList()
            );

            model.addAttribute("mode", "usuario");
            model.addAttribute("action", "edit");
            model.addAttribute("Usuario", "usuario");
        } else {
            if (imagen != null && !imagen.isEmpty()) {
                String nombre = imagen.getOriginalFilename();
                String extension = (nombre != null && nombre.contains(".")) ? nombre.substring(nombre.lastIndexOf('.') + 1) : "";
                if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension)) {
                    try {
                        byte[] bytes = imagen.getBytes();
                        String base64Image = java.util.Base64.getEncoder().encodeToString(bytes);
                        usuario.setImagen(base64Image);
                    } catch (Exception ex) {
                        System.out.println("Error");
                    }
                }

            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Usuario> entity = new HttpEntity<>(usuario, headers);
        ResponseEntity<Result<Usuario>> usuarioResponse = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/update/{id}",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Result<Usuario>>() {
        },
                usuario.getIdUsuario()
        );
        if (usuarioResponse.getStatusCode().is2xxSuccessful()
                && usuarioResponse.getBody() != null
                && usuarioResponse.getBody().correct) {
            return "redirect:/usuario/editarUsuario?idUsuario=" + usuario.getIdUsuario();
        }

        ResponseEntity<Result<List<Rol>>> rolesResp = restTemplate.exchange(
                "http://localhost:8080/rolapi/getall",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Rol>>>() {
        }
        );

        model.addAttribute("roles",
                (rolesResp.getStatusCode() == HttpStatusCode.valueOf(200)
                && rolesResp.getBody() != null
                && rolesResp.getBody().correct)
                ? rolesResp.getBody().object
                : java.util.Collections.emptyList()
        );
        model.addAttribute("mode", "usuario");
        model.addAttribute("action", "edit");
        model.addAttribute("Usuario", usuario);
        model.addAttribute("error",
                (usuarioResponse.getBody() != null && usuarioResponse.getBody().errorMessage != null)
                ? usuarioResponse.getBody().errorMessage
                : "No fue posible actualizar el usuario."
        );
        return "UsuarioForm";
    }

//    // ========================= AGREGAR DIRECCIÓN A USUARIO (FORM) =========================
    @GetMapping("direccion/add")
    public String DireccionAddForm(@RequestParam("idUsuario") int idUsuario, Model model) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<Usuario>> usuarioResp = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/direcciones/{id}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Usuario>>() {
        },
                idUsuario
        );

        if (usuarioResp.getStatusCode() != HttpStatusCode.valueOf(200)
                || usuarioResp.getBody() == null
                || !usuarioResp.getBody().correct
                || usuarioResp.getBody().object == null) {
            return "redirect:/usuario";
        }

        Usuario usuario = usuarioResp.getBody().object;

        ResponseEntity<Result<List<Pais>>> paisesResp = restTemplate.exchange(
                "http://localhost:8080/catalogoapi/paises",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Pais>>>() {
        }
        );

        List<Pais> paises = (paisesResp.getStatusCode() == HttpStatusCode.valueOf(200)
                && paisesResp.getBody() != null
                && paisesResp.getBody().correct)
                ? paisesResp.getBody().object
                : java.util.Collections.emptyList();

        Direccion direccion = new Direccion();
        Colonia colonia = new Colonia();
        Municipio municipio = new Municipio();
        Estado estado = new Estado();
        Pais pais = new Pais();

        estado.setPais(pais);
        municipio.setEstado(estado);
        colonia.setMunicipio(municipio);
        direccion.setColonia(colonia);

        model.addAttribute("usuario", usuario);
        model.addAttribute("direccion", direccion);
        model.addAttribute("paises", paises);
        model.addAttribute("mode", "direccion");
        model.addAttribute("action", "add");
        return "UsuarioForm";
    }

    // ========================= AGREGAR DIRECCIÓN (POST) =========================
    @PostMapping("direccion/add")
    public String DireccionAdd(@RequestParam("idUsuario") int idUsuario,
            @Valid Direccion direccion,
            BindingResult br,
            Model model) {

        RestTemplate restTemplate = new RestTemplate();

        if (br.hasErrors()) {
            ResponseEntity<Result<Usuario>> usuarioResp = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/direcciones/{id}",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            },
                    idUsuario
            );

            ResponseEntity<Result<List<Pais>>> paisesResp = restTemplate.exchange(
                    "http://localhost:8080/catalogoapi/paises",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Pais>>>() {
            }
            );

            model.addAttribute("usuario",
                    (usuarioResp.getStatusCode().is2xxSuccessful()
                    && usuarioResp.getBody() != null
                    && usuarioResp.getBody().correct)
                    ? usuarioResp.getBody().object
                    : null
            );
            model.addAttribute("direccion", direccion);
            model.addAttribute("paises",
                    (paisesResp.getStatusCode().is2xxSuccessful()
                    && paisesResp.getBody() != null
                    && paisesResp.getBody().correct)
                    ? paisesResp.getBody().object
                    : java.util.Collections.emptyList()
            );
            model.addAttribute("mode", "direccion");
            model.addAttribute("action", "add");
            return "UsuarioForm";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Direccion> entity = new HttpEntity<>(direccion, headers);

        ResponseEntity<Result<Direccion>> addResp = restTemplate.exchange(
                "http://localhost:8080/direccionapi/usuario/{idUsuario}/agregar",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Result<Direccion>>() {
        },
                idUsuario
        );

        if (addResp.getStatusCode().is2xxSuccessful()
                && addResp.getBody() != null
                && addResp.getBody().correct) {
            return "redirect:/usuario/editarUsuario?idUsuario=" + idUsuario;
        }

        ResponseEntity<Result<Usuario>> usuarioResp = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/direcciones/{id}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Usuario>>() {
        },
                idUsuario
        );
        ResponseEntity<Result<List<Pais>>> paisesResp = restTemplate.exchange(
                "http://localhost:8080/catalogoapi/paises",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Pais>>>() {
        }
        );

        model.addAttribute("usuario",
                (usuarioResp.getStatusCode().is2xxSuccessful()
                && usuarioResp.getBody() != null
                && usuarioResp.getBody().correct)
                ? usuarioResp.getBody().object
                : null
        );
        model.addAttribute("direccion", direccion);
        model.addAttribute("paises",
                (paisesResp.getStatusCode().is2xxSuccessful()
                && paisesResp.getBody() != null
                && paisesResp.getBody().correct)
                ? paisesResp.getBody().object
                : java.util.Collections.emptyList()
        );
        model.addAttribute("mode", "direccion");
        model.addAttribute("action", "add");
        model.addAttribute("error",
                (addResp.getBody() != null && addResp.getBody().errorMessage != null)
                ? addResp.getBody().errorMessage
                : "No fue posible agregar la dirección."
        );
        return "UsuarioForm";
    }

//
    // ========================= ACTUALIZAR DIRECCIÓN (FORM) =========================
    @GetMapping("direccion/edit")
    public String DireccionEditForm(@RequestParam("idUsuario") int idUsuario, @RequestParam("idDireccion") int idDireccion, Model model) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<Usuario>> usuarioResponse = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/direcciones/{id}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Usuario>>() {
        },
                idUsuario
        );
        if (usuarioResponse.getStatusCode() != HttpStatusCode.valueOf(200)
                || usuarioResponse.getBody() == null
                || !usuarioResponse.getBody().correct
                || usuarioResponse.getBody().object == null) {
            return "redirect:/usuario";
        }
        ResponseEntity<Result<Direccion>> dirResp = restTemplate.exchange(
                "http://localhost:8080/direccionapi/get/{idDireccion}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Direccion>>() {
        },
                idDireccion
        );
        if (dirResp.getStatusCode() != HttpStatusCode.valueOf(200)
                || dirResp.getBody() == null
                || !dirResp.getBody().correct
                || dirResp.getBody().object == null) {
            return "redirect:/usuario/editarUsuario?idUsuario=" + idUsuario;
        }
        ResponseEntity<Result<List<Pais>>> paisesResp = restTemplate.exchange(
                "http://localhost:8080/catalogoapi/paises",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Pais>>>() {
        }
        );

        model.addAttribute("usuario", usuarioResponse.getBody().object);
        model.addAttribute("direccion", dirResp.getBody().object);
        model.addAttribute("paises",
                (paisesResp.getStatusCode().is2xxSuccessful()
                && paisesResp.getBody() != null
                && paisesResp.getBody().correct)
                ? paisesResp.getBody().object
                : java.util.Collections.emptyList()
        );
        model.addAttribute("mode", "direccion");
        model.addAttribute("action", "edit");
        return "UsuarioForm";
    }

    // ========================= ACTUALIZAR DIRECCIÓN (POST) =========================
    @PostMapping("direccion/update")
    public String DireccionUpdate(@RequestParam("idUsuario") int idUsuario, @RequestParam ("idDireccion") int idDireccion,
            @Valid Direccion direccion,
            BindingResult bindingResult,
            Model model) {

        RestTemplate restTemplate = new RestTemplate();

        if (bindingResult.hasErrors()) {
            ResponseEntity<Result<Usuario>> usuarioResponse = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/direcciones/{id}",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            },
                    idUsuario
            );
            ResponseEntity<Result<List<Pais>>> paisesResp = restTemplate.exchange(
                    "http://localhost:8080/catalogoapi/paises",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Pais>>>() {
            }
            );

            model.addAttribute("usuario",
                    (usuarioResponse.getStatusCode().is2xxSuccessful()
                    && usuarioResponse.getBody() != null
                    && usuarioResponse.getBody().correct)
                    ? usuarioResponse.getBody().object
                    : null
            );
            model.addAttribute("direccion", direccion);
            model.addAttribute("paises",
                    (paisesResp.getStatusCode().is2xxSuccessful()
                    && paisesResp.getBody() != null
                    && paisesResp.getBody().correct)
                    ? paisesResp.getBody().object
                    : java.util.Collections.emptyList()
            );
            model.addAttribute("mode", "direccion");
            model.addAttribute("action", "edit");
            return "UsuarioForm";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<Direccion> entity = new HttpEntity<>(direccion, headers);

        ResponseEntity<Result<Direccion>> updResp = restTemplate.exchange(
                "http://localhost:8080/direccionapi/update/{idDireccion}",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Result<Direccion>>(){},
                idDireccion
        );

        if (updResp.getStatusCode().is2xxSuccessful()
                && updResp.getBody() != null
                && updResp.getBody().correct) {
            return "redirect:/usuario/editarUsuario?idUsuario=" + idUsuario;
        }

        // Si falló, recargar datos y volver al formulario con error
        ResponseEntity<Result<Usuario>> usuarioResp = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/direcciones/{id}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Usuario>>() {
        },
                idUsuario
        );
        ResponseEntity<Result<List<Pais>>> paisesResp = restTemplate.exchange(
                "http://localhost:8080/catalogoapi/paises",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Pais>>>() {
        }
        );

        model.addAttribute("usuario",
                (usuarioResp.getStatusCode().is2xxSuccessful()
                && usuarioResp.getBody() != null
                && usuarioResp.getBody().correct)
                ? usuarioResp.getBody().object
                : null
        );
        model.addAttribute("direccion", direccion);
        model.addAttribute("paises",
                (paisesResp.getStatusCode().is2xxSuccessful()
                && paisesResp.getBody() != null
                && paisesResp.getBody().correct)
                ? paisesResp.getBody().object
                : java.util.Collections.emptyList()
        );
        model.addAttribute("mode", "direccion");
        model.addAttribute("action", "edit");
        model.addAttribute("error",
                (updResp.getBody() != null && updResp.getBody().errorMessage != null)
                ? updResp.getBody().errorMessage
                : "No fue posible actualizar la dirección."
        );
        return "UsuarioForm";
    }

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
    
    @GetMapping("direccion/delete")
public String DireccionDelete(@RequestParam int idDireccion, @RequestParam int idUsuario) {
    RestTemplate restTemplate = new RestTemplate();                            

    ResponseEntity<Result<Direccion>> deleteResponse = restTemplate.exchange(
            "http://localhost:8080/direccionapi/delete/{idDireccion}",
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            new ParameterizedTypeReference<Result<Direccion>>() {},
            idDireccion
    );

    // Podrías validar si realmente se eliminó
    if (deleteResponse.getStatusCode().is2xxSuccessful() &&
        deleteResponse.getBody() != null &&
        deleteResponse.getBody().correct) {
        System.out.println("Dirección eliminada con éxito");
    } else {
        System.out.println("Error al eliminar la dirección");
    }

        return "redirect:/usuario/editarUsuario?idUsuario=" + idUsuario;
}

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
