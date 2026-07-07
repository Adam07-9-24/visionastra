package com.visionastra.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "generaciones_ia")
public class GeneracionIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_generacion")
    private Integer idGeneracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campana", nullable = false)
    private Campana campana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente")
    private AgenteIA agente;

    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "resumen_contexto", columnDefinition = "TEXT")
    private String resumenContexto;

    @Column(name = "guion_generado", columnDefinition = "TEXT")
    private String guionGenerado;

    @Column(name = "prompt_final", columnDefinition = "TEXT")
    private String promptFinal;

    @Column(name = "prompt_final_espanol", columnDefinition = "TEXT")
    private String promptFinalEspanol;

    @Column(name = "proveedor_prompt", length = 50)
    private String proveedorPrompt;

    @Column(name = "proveedor_video", length = 50)
    private String proveedorVideo;

    @Column(name = "tipo_salida", nullable = false, length = 20)
    private String tipoSalida;

    @Column(name = "estado", length = 20)
    private String estado = "pendiente";

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recurso_resultado")
    private Recurso recursoResultado;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public GeneracionIA() {
    }

    public Integer getIdGeneracion() {
        return idGeneracion;
    }

    public void setIdGeneracion(Integer idGeneracion) {
        this.idGeneracion = idGeneracion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Campana getCampana() {
        return campana;
    }

    public void setCampana(Campana campana) {
        this.campana = campana;
    }

    public AgenteIA getAgente() {
        return agente;
    }

    public void setAgente(AgenteIA agente) {
        this.agente = agente;
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

    public String getPromptFinal() {
        return promptFinal;
    }

    public void setPromptFinal(String promptFinal) {
        this.promptFinal = promptFinal;
    }

    public String getPromptFinalEspanol() {
        return promptFinalEspanol;
    }

    public void setPromptFinalEspanol(String promptFinalEspanol) {
        this.promptFinalEspanol = promptFinalEspanol;
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

    public Recurso getRecursoResultado() {
        return recursoResultado;
    }

    public void setRecursoResultado(Recurso recursoResultado) {
        this.recursoResultado = recursoResultado;
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