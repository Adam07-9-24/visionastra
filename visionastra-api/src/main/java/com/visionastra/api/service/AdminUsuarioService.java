package com.visionastra.api.service;

import com.visionastra.api.dto.AdminUsuarioEstadoResponse;

public interface AdminUsuarioService {

    AdminUsuarioEstadoResponse bloquearUsuario(Long idUsuario);

    AdminUsuarioEstadoResponse activarUsuario(Long idUsuario);
}
