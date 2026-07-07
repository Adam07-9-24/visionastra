package com.visionastra.api.util;

public class DeviceDetector {

    public static String detectarDispositivo(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Dispositivo no identificado";
        }

        String ua = userAgent.toLowerCase();

        String sistemaOperativo = "Sistema desconocido";
        String navegador = "Navegador desconocido";

        if (ua.contains("windows")) {
            sistemaOperativo = "Windows";
        } else if (ua.contains("android")) {
            sistemaOperativo = "Android";
        } else if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            sistemaOperativo = "iOS";
        } else if (ua.contains("mac os") || ua.contains("macintosh")) {
            sistemaOperativo = "MacOS";
        } else if (ua.contains("linux")) {
            sistemaOperativo = "Linux";
        }

        if (ua.contains("postman")) {
            navegador = "Postman";
        } else if (ua.contains("edg")) {
            navegador = "Edge";
        } else if (ua.contains("chrome")) {
            navegador = "Chrome";
        } else if (ua.contains("firefox")) {
            navegador = "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            navegador = "Safari";
        }

        return navegador + " en " + sistemaOperativo;
    }
}