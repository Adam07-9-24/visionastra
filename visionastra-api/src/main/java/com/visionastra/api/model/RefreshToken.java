package com.visionastra.api.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_refresh_token")
    private Long idRefreshToken;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "revocado")
    private Boolean revocado;

    @Column(name = "id_sesion")
    private Integer idSesion;

    public RefreshToken() {
    }

    public RefreshToken(Long idRefreshToken, Usuario usuario, String tokenHash,
                        LocalDateTime fechaEmision, LocalDateTime fechaExpiracion, Boolean revocado) {
        this.idRefreshToken = idRefreshToken;
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.fechaEmision = fechaEmision;
        this.fechaExpiracion = fechaExpiracion;
        this.revocado = revocado;
    }

    public Long getIdRefreshToken() {
        return idRefreshToken;
    }

    public void setIdRefreshToken(Long idRefreshToken) {
        this.idRefreshToken = idRefreshToken;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public Boolean getRevocado() {
        return revocado;
    }

    public void setRevocado(Boolean revocado) {
        this.revocado = revocado;
    }

    public Integer getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Integer idSesion) {
        this.idSesion = idSesion;
    }
}