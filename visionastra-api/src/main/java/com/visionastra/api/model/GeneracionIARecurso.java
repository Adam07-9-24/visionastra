package com.visionastra.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "generaciones_ia_recursos")
public class GeneracionIARecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_generacion_recurso")
    private Integer idGeneracionRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_generacion", nullable = false)
    private GeneracionIA generacionIA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recurso", nullable = false)
    private Recurso recurso;

    @Column(name = "rol_recurso", length = 20)
    private String rolRecurso = "entrada";

    public GeneracionIARecurso() {
    }

    public Integer getIdGeneracionRecurso() {
        return idGeneracionRecurso;
    }

    public void setIdGeneracionRecurso(Integer idGeneracionRecurso) {
        this.idGeneracionRecurso = idGeneracionRecurso;
    }

    public GeneracionIA getGeneracionIA() {
        return generacionIA;
    }

    public void setGeneracionIA(GeneracionIA generacionIA) {
        this.generacionIA = generacionIA;
    }

    public Recurso getRecurso() {
        return recurso;
    }

    public void setRecurso(Recurso recurso) {
        this.recurso = recurso;
    }

    public String getRolRecurso() {
        return rolRecurso;
    }

    public void setRolRecurso(String rolRecurso) {
        this.rolRecurso = rolRecurso;
    }
}