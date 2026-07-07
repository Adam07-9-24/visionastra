package com.visionastra.api.dto;

import java.time.LocalDateTime;

public class SesionActivaDTO {

    private Integer idSesion;
    private String dispositivo;
    private String ipAddress;
    private LocalDateTime fechaInicio;
    private String estado;
    private boolean actual;

    public SesionActivaDTO(Integer idSesion, String dispositivo, String ipAddress,
                           LocalDateTime fechaInicio, String estado, boolean actual) {
        this.idSesion = idSesion;
        this.dispositivo = dispositivo;
        this.ipAddress = ipAddress;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
        this.actual = actual;
    }

    public Integer getIdSesion() { return idSesion; }
    public String getDispositivo() { return dispositivo; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public String getEstado() { return estado; }
    public boolean isActual() { return actual; }
}