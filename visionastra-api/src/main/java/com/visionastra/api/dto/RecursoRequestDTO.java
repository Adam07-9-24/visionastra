package com.visionastra.api.dto;

import java.math.BigDecimal;

public class RecursoRequestDTO {

    private Integer idCampana;
    private String tipo;
    private String titulo;
    private String nombreArchivo;
    private String urlArchivo;
    private String contenidoTexto;
    private BigDecimal pesoMb;
    private String formato;

    public RecursoRequestDTO() {
    }

    public Integer getIdCampana() {
        return idCampana;
    }

    public void setIdCampana(Integer idCampana) {
        this.idCampana = idCampana;
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
}