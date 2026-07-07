package com.visionastra.api.dto;

public class SesionEventoDTO {

    private String tipo;
    private String mensaje;
    private Integer idSesion;
    private Long idUsuario;

    public SesionEventoDTO() {
    }

    public SesionEventoDTO(String tipo, String mensaje, Integer idSesion, Long idUsuario) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.idSesion = idSesion;
        this.idUsuario = idUsuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Integer getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Integer idSesion) {
        this.idSesion = idSesion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}