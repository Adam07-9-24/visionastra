package com.visionastra.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agentes_ia")
public class AgenteIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agente")
    private Integer idAgente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 20)
    private String nombre;

    @Column(name = "modelo_ia", length = 100)
    private String modeloIa = "gpt-4.1";

    @Column(name = "prompt_base", columnDefinition = "TEXT")
    private String promptBase;

    @Column(name = "configuracion", columnDefinition = "json")
    private String configuracion;

    @Column(name = "tokens_consumidos")
    private Integer tokensConsumidos = 0;

    @Column(name = "costo_estimado", precision = 10, scale = 4)
    private BigDecimal costoEstimado = BigDecimal.ZERO;

    @Column(name = "ultima_ejecucion")
    private LocalDateTime ultimaEjecucion;

    @Column(name = "estado", length = 20)
    private String estado = "activo";

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public AgenteIA() {
    }

    public Integer getIdAgente() {
        return idAgente;
    }

    public void setIdAgente(Integer idAgente) {
        this.idAgente = idAgente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getModeloIa() {
        return modeloIa;
    }

    public void setModeloIa(String modeloIa) {
        this.modeloIa = modeloIa;
    }

    public String getPromptBase() {
        return promptBase;
    }

    public void setPromptBase(String promptBase) {
        this.promptBase = promptBase;
    }

    public String getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(String configuracion) {
        this.configuracion = configuracion;
    }

    public Integer getTokensConsumidos() {
        return tokensConsumidos;
    }

    public void setTokensConsumidos(Integer tokensConsumidos) {
        this.tokensConsumidos = tokensConsumidos;
    }

    public BigDecimal getCostoEstimado() {
        return costoEstimado;
    }

    public void setCostoEstimado(BigDecimal costoEstimado) {
        this.costoEstimado = costoEstimado;
    }

    public LocalDateTime getUltimaEjecucion() {
        return ultimaEjecucion;
    }

    public void setUltimaEjecucion(LocalDateTime ultimaEjecucion) {
        this.ultimaEjecucion = ultimaEjecucion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}