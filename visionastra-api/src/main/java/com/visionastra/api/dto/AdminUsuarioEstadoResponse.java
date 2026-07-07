package com.visionastra.api.dto;

public class AdminUsuarioEstadoResponse {

    private Long idUsuario;
    private String estado;
    private String mensaje;

    public AdminUsuarioEstadoResponse() {
    }

    public AdminUsuarioEstadoResponse(Long idUsuario, String estado, String mensaje) {
        this.idUsuario = idUsuario;
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
