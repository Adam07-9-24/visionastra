package com.visionastra.api.dto;

import java.time.LocalDateTime;

public class SesionResponse {

    private Integer idSesion;
    private String dispositivo;
    private String ipAddress;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaExpiracion;
    private String estado;

    public SesionResponse() {
    }

    public SesionResponse(Integer idSesion, String dispositivo, String ipAddress,
                          LocalDateTime fechaInicio, LocalDateTime fechaExpiracion,
                          String estado) {
        this.idSesion = idSesion;
        this.dispositivo = dispositivo;
        this.ipAddress = ipAddress;
        this.fechaInicio = fechaInicio;
        this.fechaExpiracion = fechaExpiracion;
        this.estado = estado;
    }

    public Integer getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Integer idSesion) {
        this.idSesion = idSesion;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}