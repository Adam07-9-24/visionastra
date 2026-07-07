package com.visionastra.api.dto;

public class RegisterResponse {

    private String mensaje;

    public RegisterResponse() {
    }

    public RegisterResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
