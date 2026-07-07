package com.visionastra.api.service;

import com.visionastra.api.model.AuditoriaLogin;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.AuditoriaLoginRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaLoginService {

    private final AuditoriaLoginRepository auditoriaLoginRepository;

    public AuditoriaLoginService(AuditoriaLoginRepository auditoriaLoginRepository) {
        this.auditoriaLoginRepository = auditoriaLoginRepository;
    }

    public void registrarLoginExitoso(Usuario usuario, String ipAddress, String userAgent) {
        AuditoriaLogin auditoria = new AuditoriaLogin();
        auditoria.setUsuario(usuario);
        auditoria.setEmailIntentado(usuario.getEmail());
        auditoria.setIpAddress(ipAddress);
        auditoria.setUserAgent(userAgent);
        auditoria.setResultado("exitoso");
        auditoria.setMotivo("Login correcto");
        auditoria.setFechaEvento(LocalDateTime.now());

        auditoriaLoginRepository.save(auditoria);
    }

    public void registrarLoginFallido(String emailIntentado, String motivo, String ipAddress, String userAgent) {
        AuditoriaLogin auditoria = new AuditoriaLogin();
        auditoria.setUsuario(null);
        auditoria.setEmailIntentado(emailIntentado);
        auditoria.setIpAddress(ipAddress);
        auditoria.setUserAgent(userAgent);
        auditoria.setResultado("fallido");
        auditoria.setMotivo(motivo);
        auditoria.setFechaEvento(LocalDateTime.now());

        auditoriaLoginRepository.save(auditoria);
    }
    public void registrarRefreshExitoso(Usuario usuario, String ipAddress, String userAgent) {
        AuditoriaLogin auditoria = new AuditoriaLogin();
        auditoria.setUsuario(usuario);
        auditoria.setEmailIntentado(usuario.getEmail());
        auditoria.setIpAddress(ipAddress);
        auditoria.setUserAgent(userAgent);
        auditoria.setResultado("exitoso");
        auditoria.setMotivo("Refresh token rotado");
        auditoria.setFechaEvento(LocalDateTime.now());

        auditoriaLoginRepository.save(auditoria);
    }

    public void registrarLogoutExitoso(Usuario usuario, String ipAddress, String userAgent) {
        AuditoriaLogin auditoria = new AuditoriaLogin();
        auditoria.setUsuario(usuario);
        auditoria.setEmailIntentado(usuario.getEmail());
        auditoria.setIpAddress(ipAddress);
        auditoria.setUserAgent(userAgent);
        auditoria.setResultado("exitoso");
        auditoria.setMotivo("Logout manual");
        auditoria.setFechaEvento(LocalDateTime.now());

        auditoriaLoginRepository.save(auditoria);
    }
}