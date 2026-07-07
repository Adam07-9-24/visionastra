package com.visionastra.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GeneracionIAResponseDTO {

    private Integer idGeneracion;

    private Long idUsuario;
    private String nombreUsuario;

    private Integer idCampana;
    private String nombreCampana;

    private Integer idAgente;
    private String nombreAgente;

    private String prompt;
    private String resumenContexto;
    private String guionGenerado;
    private String promptFinalEspanol;
    private String promptFinal;
    private String proveedorPrompt;
    private String proveedorVideo;

    private String tipoSalida;
    private String estado;
    private String mensajeError;

    private Integer idRecursoResultado;
    private String tituloRecursoResultado;
    private String tipoRecursoResultado;

    private List<RecursoEntradaDTO> recursosEntrada;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public GeneracionIAResponseDTO() {
    }

    public Integer getIdGeneracion() {
        return idGeneracion;
    }

    public void setIdGeneracion(Integer idGeneracion) {
        this.idGeneracion = idGeneracion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
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

    public Integer getIdAgente() {
        return idAgente;
    }

    public void setIdAgente(Integer idAgente) {
        this.idAgente = idAgente;
    }

    public String getNombreAgente() {
        return nombreAgente;
    }

    public void setNombreAgente(String nombreAgente) {
        this.nombreAgente = nombreAgente;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResumenContexto() {
        return resumenContexto;
    }

    public void setResumenContexto(String resumenContexto) {
        this.resumenContexto = resumenContexto;
    }

    public String getGuionGenerado() {
        return guionGenerado;
    }

    public void setGuionGenerado(String guionGenerado) {
        this.guionGenerado = guionGenerado;
    }

    public String getPromptFinalEspanol() {
        return promptFinalEspanol;
    }

    public void setPromptFinalEspanol(String promptFinalEspanol) {
        this.promptFinalEspanol = promptFinalEspanol;
    }

    public String getPromptFinal() {
        return promptFinal;
    }

    public void setPromptFinal(String promptFinal) {
        this.promptFinal = promptFinal;
    }

    public String getProveedorPrompt() {
        return proveedorPrompt;
    }

    public void setProveedorPrompt(String proveedorPrompt) {
        this.proveedorPrompt = proveedorPrompt;
    }

    public String getProveedorVideo() {
        return proveedorVideo;
    }

    public void setProveedorVideo(String proveedorVideo) {
        this.proveedorVideo = proveedorVideo;
    }

    public String getTipoSalida() {
        return tipoSalida;
    }

    public void setTipoSalida(String tipoSalida) {
        this.tipoSalida = tipoSalida;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public Integer getIdRecursoResultado() {
        return idRecursoResultado;
    }

    public void setIdRecursoResultado(Integer idRecursoResultado) {
        this.idRecursoResultado = idRecursoResultado;
    }

    public String getTituloRecursoResultado() {
        return tituloRecursoResultado;
    }

    public void setTituloRecursoResultado(String tituloRecursoResultado) {
        this.tituloRecursoResultado = tituloRecursoResultado;
    }

    public String getTipoRecursoResultado() {
        return tipoRecursoResultado;
    }

    public void setTipoRecursoResultado(String tipoRecursoResultado) {
        this.tipoRecursoResultado = tipoRecursoResultado;
    }

    public List<RecursoEntradaDTO> getRecursosEntrada() {
        return recursosEntrada;
    }

    public void setRecursosEntrada(List<RecursoEntradaDTO> recursosEntrada) {
        this.recursosEntrada = recursosEntrada;
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

    public static class RecursoEntradaDTO {

        private Integer idRecurso;
        private String titulo;
        private String tipo;
        private String nombreArchivo;
        private String rolRecurso;

        public RecursoEntradaDTO() {
        }

        public Integer getIdRecurso() {
            return idRecurso;
        }

        public void setIdRecurso(Integer idRecurso) {
            this.idRecurso = idRecurso;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getNombreArchivo() {
            return nombreArchivo;
        }

        public void setNombreArchivo(String nombreArchivo) {
            this.nombreArchivo = nombreArchivo;
        }

        public String getRolRecurso() {
            return rolRecurso;
        }

        public void setRolRecurso(String rolRecurso) {
            this.rolRecurso = rolRecurso;
        }
    }
}