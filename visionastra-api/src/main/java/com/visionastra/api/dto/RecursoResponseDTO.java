package com.visionastra.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecursoResponseDTO {

    private Integer idRecurso;
    private Integer idCampana;
    private String nombreCampana;
    private String tipo;
    private String titulo;
    private String nombreArchivo;
    private String urlArchivo;
    private String contenidoTexto;
    private BigDecimal pesoMb;
    private String formato;
    private String estado;
    private LocalDateTime fechaSubida;

    public RecursoResponseDTO() {
    }

    public RecursoResponseDTO(
            Integer idRecurso,
            Integer idCampana,
            String nombreCampana,
            String tipo,
            String titulo,
            String nombreArchivo,
            String urlArchivo,
            String contenidoTexto,
            BigDecimal pesoMb,
            String formato,
            String estado,
            LocalDateTime fechaSubida
    ) {
        this.idRecurso = idRecurso;
        this.idCampana = idCampana;
        this.nombreCampana = nombreCampana;
        this.tipo = tipo;
        this.titulo = titulo;
        this.nombreArchivo = nombreArchivo;
        this.urlArchivo = urlArchivo;
        this.contenidoTexto = contenidoTexto;
        this.pesoMb = pesoMb;
        this.formato = formato;
        this.estado = estado;
        this.fechaSubida = fechaSubida;
    }

    public Integer getIdRecurso() {
        return idRecurso;
    }

    public void setIdRecurso(Integer idRecurso) {
        this.idRecurso = idRecurso;
    }

    public Integer getIdCampana() {
        return idCampana;
    }

    public void setIdCampana(Integer idCampana) {
        this.idCampana = idCampana;
    }

    public String getNombreCampana() {
        return nombreCampana;
    }

    public void setNombreCampana(String nombreCampana) {
        this.nombreCampana = nombreCampana;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getUrlArchivo() {
        return urlArchivo;
    }

    public void setUrlArchivo(String urlArchivo) {
        this.urlArchivo = urlArchivo;
    }

    public String getContenidoTexto() {
        return contenidoTexto;
    }

    public void setContenidoTexto(String contenidoTexto) {
        this.contenidoTexto = contenidoTexto;
    }

    public BigDecimal getPesoMb() {
        return pesoMb;
    }

    public void setPesoMb(BigDecimal pesoMb) {
        this.pesoMb = pesoMb;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
}