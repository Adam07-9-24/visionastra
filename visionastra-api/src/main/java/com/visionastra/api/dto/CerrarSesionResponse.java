package com.visionastra.api.dto;

public class CerrarSesionResponse {

    private Integer idSesion;
    private String estado;
    private String mensaje;

    public CerrarSesionResponse() {
    }

    public CerrarSesionResponse(Integer idSesion, String estado, String mensaje) {
        this.idSesion = idSesion;
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public Integer getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Integer idSesion) {
        this.idSesion = idSesion;
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