package com.visionastra.api.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long idUsuario) {
        super("Usuario no encontrado con id: " + idUsuario);
    }
}
