package com.visionastra.api.controller;

import com.visionastra.api.dto.PublicacionRequestDTO;
import com.visionastra.api.dto.PublicacionResponseDTO;
import com.visionastra.api.service.PublicacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    @GetMapping
    public ResponseEntity<List<PublicacionResponseDTO>> listar(
            Authentication authentication,
            @RequestParam(required = false) Integer idCampana,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String plataforma
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(publicacionService.listar(email, idCampana, estado, plataforma));
    }

    @GetMapping("/{idPublicacion}")
    public ResponseEntity<PublicacionResponseDTO> obtenerPorId(
            @PathVariable Integer idPublicacion,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(publicacionService.obtenerPorId(idPublicacion, email));
    }

    @PatchMapping("/{idPublicacion}/cancelar")
    public ResponseEntity<PublicacionResponseDTO> cancelar(
            @PathVariable Integer idPublicacion,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(publicacionService.cancelar(idPublicacion, email));
    }

    @PatchMapping("/{idPublicacion}/enviar-n8n")
    public ResponseEntity<PublicacionResponseDTO> enviarAN8n(
            @PathVariable Integer idPublicacion,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(publicacionService.enviarAN8n(idPublicacion, email));
    }

    @PostMapping
    public ResponseEntity<PublicacionResponseDTO> crear(
            @RequestBody PublicacionRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(publicacionService.crear(request, email));
    }

    @PutMapping("/{idPublicacion}")
    public ResponseEntity<PublicacionResponseDTO> actualizar(
            @PathVariable Integer idPublicacion,
            @RequestBody PublicacionRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(publicacionService.actualizar(idPublicacion, request, email));
    }
}
