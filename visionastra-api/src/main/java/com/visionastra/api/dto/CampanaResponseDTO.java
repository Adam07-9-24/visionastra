package com.visionastra.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CampanaResponseDTO {

    private Integer idCampana;
    private Long idUsuario;
    private String nombre;
    private String objetivo;
    private String descripcion;
    private BigDecimal presupuesto;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public CampanaResponseDTO() {
    }

    public CampanaResponseDTO(
            Integer idCampana,
            Long idUsuario,
            String nombre,
            String objetivo,
            String descripcion,
            BigDecimal presupuesto,
            String estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion
    ) {
        this.idCampana = idCampana;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.descripcion = descripcion;
        this.presupuesto = presupuesto;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Integer getIdCampana() {
        return idCampana;
    }

    public void setIdCampana(Integer idCampana) {
        this.idCampana = idCampana;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(BigDecimal presupuesto) {
        this.presupuesto = presupuesto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}