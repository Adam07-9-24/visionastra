package com.visionastra.api.dto;

import java.util.List;

public class GeneracionIARequestDTO {

    private Integer idCampana;
    private Integer idAgente;
    private String prompt;
    private String tipoSalida;
    private List<Integer> idsRecursos;

    public GeneracionIARequestDTO() {
    }

    public Integer getIdCampana() {
        return idCampana;
    }

    public void setIdCampana(Integer idCampana) {
        this.idCampana = idCampana;
    }

    public Integer getIdAgente() {
        return idAgente;
    }

    public void setIdAgente(Integer idAgente) {
        this.idAgente = idAgente;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getTipoSalida() {
        return tipoSalida;
    }

    public void setTipoSalida(String tipoSalida) {
        this.tipoSalida = tipoSalida;
    }

    public List<Integer> getIdsRecursos() {
        return idsRecursos;
    }

    public void setIdsRecursos(List<Integer> idsRecursos) {
        this.idsRecursos = idsRecursos;
    }
}