package com.visionastra.api.controller;

import com.visionastra.api.dto.GeneracionIARequestDTO;
import com.visionastra.api.dto.GeneracionIAResponseDTO;
import com.visionastra.api.service.GeneracionIAService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/generaciones-ia")
public class GeneracionIAController {

    private final GeneracionIAService generacionIAService;

    public GeneracionIAController(GeneracionIAService generacionIAService) {
        this.generacionIAService = generacionIAService;
    }

    @GetMapping
    public ResponseEntity<List<GeneracionIAResponseDTO>> listar(Authentication authentication) {
        String emailUsuario = authentication.getName();
        return ResponseEntity.ok(generacionIAService.listarGeneracionesDelUsuario(emailUsuario));
    }

    @GetMapping("/{idGeneracion}")
    public ResponseEntity<GeneracionIAResponseDTO> obtenerPorId(
            @PathVariable Integer idGeneracion,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();
        return ResponseEntity.ok(generacionIAService.obtenerGeneracionPorId(idGeneracion, emailUsuario));
    }

    @PostMapping
    public ResponseEntity<GeneracionIAResponseDTO> crear(
            @RequestBody GeneracionIARequestDTO request,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();
        return ResponseEntity.ok(generacionIAService.crearGeneracion(request, emailUsuario));
    }

    @PatchMapping("/{idGeneracion}/procesar")
    public ResponseEntity<GeneracionIAResponseDTO> marcarProcesando(
            @PathVariable Integer idGeneracion,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();
        return ResponseEntity.ok(generacionIAService.marcarComoProcesando(idGeneracion, emailUsuario));
    }

    @PatchMapping("/{idGeneracion}/preparar-prompt")
    public ResponseEntity<GeneracionIAResponseDTO> prepararPrompt(
            @PathVariable Integer idGeneracion,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();

        return ResponseEntity.ok(
                generacionIAService.prepararPrompt(idGeneracion, emailUsuario)
        );
    }

    @PatchMapping("/{idGeneracion}/error")
    public ResponseEntity<GeneracionIAResponseDTO> marcarError(
            @PathVariable Integer idGeneracion,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();

        String mensajeError = body != null ? body.get("mensajeError") : null;

        return ResponseEntity.ok(
                generacionIAService.marcarComoError(idGeneracion, mensajeError, emailUsuario)
        );
    }

    @DeleteMapping("/{idGeneracion}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer idGeneracion,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();
        generacionIAService.eliminarGeneracion(idGeneracion, emailUsuario);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idGeneracion}/generar-video")
    public ResponseEntity<GeneracionIAResponseDTO> generarVideo(
            @PathVariable Integer idGeneracion,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();
        GeneracionIAResponseDTO response = generacionIAService.generarVideo(idGeneracion, emailUsuario);
        return ResponseEntity.ok(response);
    }
}