package com.visionastra.api.controller;

import com.visionastra.api.dto.CerrarSesionResponse;
import com.visionastra.api.dto.SesionResponse;
import com.visionastra.api.dto.SesionActivaDTO;
import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.JwtService;
import com.visionastra.api.service.SesionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {

    private final SesionService sesionService;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public SesionController(SesionService sesionService,
                            UsuarioRepository usuarioRepository,
                            JwtService jwtService) {
        this.sesionService = sesionService;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    //  ENDPOINT ACTUAL (NO TOCAR)
    @GetMapping("/activas")
    public ResponseEntity<List<SesionResponse>> listarSesionesActivas(Authentication authentication) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        List<Sesion> sesiones = sesionService.listarSesionesActivasPorUsuario(usuario);

        List<SesionResponse> response = sesiones.stream()
                .map(sesion -> new SesionResponse(
                        sesion.getIdSesion(),
                        sesion.getDispositivo(),
                        sesion.getIpAddress(),
                        sesion.getFechaInicio(),
                        sesion.getFechaExpiracion(),
                        sesion.getEstado()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 🟢 NUEVO ENDPOINT PRO 🔥
    @GetMapping("/activas-v2")
    public ResponseEntity<List<SesionActivaDTO>> listarSesionesActivasV2(
            Authentication authentication,
            HttpServletRequest request
    ) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // 🔥 EXTRAER TOKEN
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token JWT no enviado o inválido");
        }

        String token = authHeader.replace("Bearer ", "");

        // 🔥 EXTRAER ID DE SESIÓN
        Integer idSesionActual = jwtService.extractIdSesion(token);

        List<Sesion> sesiones = sesionService.listarSesionesActivasPorUsuario(usuario);

        List<SesionActivaDTO> response = sesiones.stream()
                .map(sesion -> new SesionActivaDTO(
                        sesion.getIdSesion(),
                        sesion.getDispositivo(),
                        sesion.getIpAddress(),
                        sesion.getFechaInicio(),
                        sesion.getEstado(),
                        sesion.getIdSesion().equals(idSesionActual) // 🔥 CLAVE
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    // 🔴 CERRAR SESIÓN
    @PatchMapping("/{idSesion}/cerrar")
    public ResponseEntity<?> cerrarSesion(
            @PathVariable Integer idSesion,
            Authentication authentication,
            HttpServletRequest request
    ) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // 🔥 EXTRAER TOKEN
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token no válido");
        }

        String token = authHeader.replace("Bearer ", "");

        // 🔥 OBTENER ID DE SESIÓN ACTUAL
        Integer idSesionActual = jwtService.extractIdSesion(token);

        // 🚫 VALIDACIÓN (NO CERRAR TU PROPIA SESIÓN)
        if (idSesion.equals(idSesionActual)) {
            return ResponseEntity.badRequest().body("No puedes cerrar tu sesión actual");
        }

        // 🔥 CERRAR SESIÓN
        Sesion sesionCerrada = sesionService.cerrarSesionPorIdYUsuario(idSesion, usuario);

        CerrarSesionResponse response = new CerrarSesionResponse(
                sesionCerrada.getIdSesion(),
                sesionCerrada.getEstado(),
                "Sesión cerrada correctamente"
        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/heartbeat")
    public ResponseEntity<String> heartbeat(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token no enviado");
        }

        String token = authHeader.replace("Bearer ", "");

        Integer idSesion = jwtService.extractIdSesion(token);

        sesionService.actualizarActividadSesion(idSesion);

        return ResponseEntity.ok("Sesión actualizada");
    }
}