package com.visionastra.api.controller;

import com.visionastra.api.dto.UsuarioResponse;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private UsuarioResponse convertirAResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : null,
                usuario.getEstado()
        );
    }
}