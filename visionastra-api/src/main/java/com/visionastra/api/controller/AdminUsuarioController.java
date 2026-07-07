package com.visionastra.api.controller;

import com.visionastra.api.dto.AdminUsuarioEstadoResponse;
import com.visionastra.api.service.AdminUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/admin/usuarios")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    public AdminUsuarioController(AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    @PatchMapping("/{idUsuario}/bloquear")
    public ResponseEntity<AdminUsuarioEstadoResponse> bloquearUsuario(
            @PathVariable Long idUsuario
    ) {
        return ResponseEntity.ok(adminUsuarioService.bloquearUsuario(idUsuario));
    }

    @PatchMapping("/{idUsuario}/activar")
    public ResponseEntity<AdminUsuarioEstadoResponse> activarUsuario(
            @PathVariable Long idUsuario
    ) {
        return ResponseEntity.ok(adminUsuarioService.activarUsuario(idUsuario));
    }
}
