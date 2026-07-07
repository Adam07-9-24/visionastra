package com.visionastra.api.dto;

public class PromptIAResultadoDTO {

    private String resumenContexto;
    private String guionGenerado;
    private String promptFinalEspanol;
    private String promptFinal;

    public PromptIAResultadoDTO() {
    }

    public PromptIAResultadoDTO(
            String resumenContexto,
            String guionGenerado,
            String promptFinalEspanol,
            String promptFinal
    ) {
        this.resumenContexto = resumenContexto;
        this.guionGenerado = guionGenerado;
        this.promptFinalEspanol = promptFinalEspanol;
        this.promptFinal = promptFinal;
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
}