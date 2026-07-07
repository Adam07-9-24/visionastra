package com.visionastra.api.controller;

import com.visionastra.api.dto.RecursoRequestDTO;
import com.visionastra.api.dto.RecursoResponseDTO;
import com.visionastra.api.service.RecursoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.visionastra.api.dto.RecursoTituloRequestDTO;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recursos")
public class RecursoController {

    private final RecursoService recursoService;

    @Value("${visionastra.uploads.path}")
    private String uploadsPath;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping("/campana/{idCampana}")
    public ResponseEntity<List<RecursoResponseDTO>> listarRecursosPorCampana(
            Authentication authentication,
            @PathVariable Integer idCampana
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                recursoService.listarRecursosPorCampana(email, idCampana)
        );
    }

    @GetMapping("/{idRecurso}")
    public ResponseEntity<RecursoResponseDTO> obtenerRecursoPorId(
            Authentication authentication,
            @PathVariable Integer idRecurso
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                recursoService.obtenerRecursoPorId(email, idRecurso)
        );
    }

    @PostMapping
    public ResponseEntity<RecursoResponseDTO> crearRecurso(
            Authentication authentication,
            @RequestBody RecursoRequestDTO request
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                recursoService.crearRecurso(email, request)
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<RecursoResponseDTO> subirArchivoRecurso(
            Authentication authentication,
            @RequestParam Integer idCampana,
            @RequestParam String tipo,
            @RequestParam(required = false) String titulo,
            @RequestParam MultipartFile archivo
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                recursoService.subirArchivoRecurso(
                        email,
                        idCampana,
                        tipo,
                        titulo,
                        archivo
                )
        );
    }

    @PatchMapping("/{idRecurso}/titulo")
    public ResponseEntity<RecursoResponseDTO> actualizarTituloRecurso(
            Authentication authentication,
            @PathVariable Integer idRecurso,
            @RequestBody RecursoTituloRequestDTO request
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                recursoService.actualizarTituloRecurso(email, idRecurso, request.getTitulo())
        );
    }

    @GetMapping("/archivo/{idRecurso}")
    public ResponseEntity<UrlResource> verArchivoRecurso(
            Authentication authentication,
            @PathVariable Integer idRecurso
    ) {
        String email = authentication.getName();

        RecursoResponseDTO recurso = recursoService.obtenerRecursoPorId(email, idRecurso);

        if (recurso.getUrlArchivo() == null || recurso.getUrlArchivo().isBlank()) {
            throw new RuntimeException("El recurso no tiene archivo asociado");
        }

        try {
            String rutaRelativa = recurso.getUrlArchivo().trim();

            // Normalizar separadores por seguridad
            rutaRelativa = rutaRelativa.replace("\\", "/");

            // Si en BD viene como "/uploads/recursos/30/video.mp4"
            if (rutaRelativa.startsWith("/uploads/")) {
                rutaRelativa = rutaRelativa.substring("/uploads/".length());
            }

            // Si en BD viene como "uploads/recursos/30/video.mp4"
            if (rutaRelativa.startsWith("uploads/")) {
                rutaRelativa = rutaRelativa.substring("uploads/".length());
            }

            Path carpetaBase = Paths.get(uploadsPath)
                    .toAbsolutePath()
                    .normalize();

            Path rutaArchivo = carpetaBase
                    .resolve(rutaRelativa)
                    .normalize();

            if (!rutaArchivo.startsWith(carpetaBase)) {
                throw new RuntimeException("Ruta de archivo no permitida");
            }

            if (!Files.exists(rutaArchivo) || !Files.isRegularFile(rutaArchivo)) {
                throw new RuntimeException("Archivo no encontrado en el servidor");
            }

            UrlResource archivo = new UrlResource(rutaArchivo.toUri());

            if (!archivo.exists() || !archivo.isReadable()) {
                throw new RuntimeException("El archivo no se puede leer");
            }

            String contentType = Files.probeContentType(rutaArchivo);

            if (contentType == null) {
                if ("mp4".equalsIgnoreCase(recurso.getFormato())) {
                    contentType = "video/mp4";
                } else if ("png".equalsIgnoreCase(recurso.getFormato())) {
                    contentType = "image/png";
                } else if ("jpg".equalsIgnoreCase(recurso.getFormato())
                        || "jpeg".equalsIgnoreCase(recurso.getFormato())) {
                    contentType = "image/jpeg";
                } else {
                    contentType = "application/octet-stream";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + recurso.getNombreArchivo() + "\""
                    )
                    .body(archivo);

        } catch (MalformedURLException e) {
            throw new RuntimeException("No se pudo leer el archivo");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PutMapping("/{idRecurso}")
    public ResponseEntity<RecursoResponseDTO> actualizarRecurso(
            Authentication authentication,
            @PathVariable Integer idRecurso,
            @RequestBody RecursoRequestDTO request
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                recursoService.actualizarRecurso(email, idRecurso, request)
        );
    }

    @PatchMapping("/{idRecurso}/archivar")
    public ResponseEntity<RecursoResponseDTO> archivarRecurso(
            Authentication authentication,
            @PathVariable Integer idRecurso
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                recursoService.archivarRecurso(email, idRecurso)
        );
    }

    @PatchMapping("/{idRecurso}/desarchivar")
    public ResponseEntity<RecursoResponseDTO> desarchivarRecurso(
            Authentication authentication,
            @PathVariable Integer idRecurso
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                recursoService.desarchivarRecurso(email, idRecurso)
        );
    }

    @DeleteMapping("/{idRecurso}")
    public ResponseEntity<Map<String, String>> eliminarRecurso(
            Authentication authentication,
            @PathVariable Integer idRecurso
    ) {
        String email = authentication.getName();

        recursoService.eliminarRecurso(email, idRecurso);

        return ResponseEntity.ok(
                Map.of("mensaje", "Recurso eliminado correctamente")
        );
    }
}