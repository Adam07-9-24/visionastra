package com.visionastra.api.controller;

import com.visionastra.api.dto.CampanaRequestDTO;
import com.visionastra.api.dto.CampanaResponseDTO;
import com.visionastra.api.service.CampanaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campanas")
public class CampanaController {

    private final CampanaService campanaService;

    public CampanaController(CampanaService campanaService) {
        this.campanaService = campanaService;
    }

    @GetMapping
    public ResponseEntity<List<CampanaResponseDTO>> listarCampanas(
            Authentication authentication,
            @RequestParam(required = false) String estado
    ) {
        String email = authentication.getName();

        if (estado != null && !estado.trim().isEmpty()) {
            return ResponseEntity.ok(
                    campanaService.listarCampanasPorEstado(email, estado)
            );
        }

        return ResponseEntity.ok(
                campanaService.listarCampanasDelUsuario(email)
        );
    }

    @GetMapping("/{idCampana}")
    public ResponseEntity<CampanaResponseDTO> obtenerCampanaPorId(
            Authentication authentication,
            @PathVariable Integer idCampana
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                campanaService.obtenerCampanaPorId(email, idCampana)
        );
    }

    @PostMapping
    public ResponseEntity<CampanaResponseDTO> crearCampana(
            Authentication authentication,
            @RequestBody CampanaRequestDTO request
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                campanaService.crearCampana(email, request)
        );
    }

    @PutMapping("/{idCampana}")
    public ResponseEntity<CampanaResponseDTO> actualizarCampana(
            Authentication authentication,
            @PathVariable Integer idCampana,
            @RequestBody CampanaRequestDTO request
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                campanaService.actualizarCampana(email, idCampana, request)
        );
    }

    @PatchMapping("/{idCampana}/estado")
    public ResponseEntity<CampanaResponseDTO> cambiarEstadoCampana(
            Authentication authentication,
            @PathVariable Integer idCampana,
            @RequestBody Map<String, String> body
    ) {
        String email = authentication.getName();
        String estado = body.get("estado");

        return ResponseEntity.ok(
                campanaService.cambiarEstadoCampana(email, idCampana, estado)
        );
    }

    @DeleteMapping("/{idCampana}")
    public ResponseEntity<Map<String, String>> eliminarCampana(
            Authentication authentication,
            @PathVariable Integer idCampana
    ) {
        String email = authentication.getName();

        campanaService.eliminarCampana(email, idCampana);

        return ResponseEntity.ok(
                Map.of("mensaje", "Campaña eliminada correctamente")
        );
    }
}