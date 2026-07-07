package com.visionastra.api.service.impl;

import com.visionastra.api.dto.SesionEventoDTO;
import com.visionastra.api.model.RefreshToken;
import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.RefreshTokenRepository;
import com.visionastra.api.repository.SesionRepository;
import com.visionastra.api.service.SesionService;
import com.visionastra.api.service.SesionWebSocketService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SesionServiceImpl implements SesionService {

    private final SesionRepository sesionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SesionWebSocketService sesionWebSocketService;

    public SesionServiceImpl(
            SesionRepository sesionRepository,
            RefreshTokenRepository refreshTokenRepository,
            SesionWebSocketService sesionWebSocketService
    ) {
        this.sesionRepository = sesionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.sesionWebSocketService = sesionWebSocketService;
    }

    @Override
    public Sesion crearSesion(
            Usuario usuario,
            String dispositivo,
            String ipAddress,
            String userAgent,
            LocalDateTime fechaExpiracion
    ) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setDispositivo(dispositivo);
        sesion.setIpAddress(ipAddress);
        sesion.setUserAgent(userAgent);
        sesion.setFechaInicio(LocalDateTime.now());
        sesion.setFechaExpiracion(fechaExpiracion);
        sesion.setEstado("activa");

        Sesion sesionGuardada = sesionRepository.save(sesion);

        notificarCambioSesion(
                sesionGuardada.getUsuario(),
                sesionGuardada.getIdSesion(),
                "SESION_CREADA",
                "Una nueva sesión fue iniciada"
        );

        return sesionGuardada;
    }

    @Override
    public List<Sesion> listarSesionesActivasPorUsuario(Usuario usuario) {
        expirarSesionesVencidas();

        return sesionRepository.findByUsuarioAndEstado(usuario, "activa");
    }

    @Override
    public Sesion cerrarSesionPorIdYUsuario(Integer idSesion, Usuario usuario) {
        Sesion sesion = sesionRepository.findByIdSesionAndEstado(idSesion, "activa")
                .orElseThrow(() -> new RuntimeException("Sesión activa no encontrada"));

        if (!sesion.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No tienes permiso para cerrar esta sesión");
        }

        sesion.setEstado("cerrada");
        Sesion sesionGuardada = sesionRepository.save(sesion);

        revocarRefreshTokensPorSesion(idSesion);

        notificarCambioSesion(
                sesionGuardada.getUsuario(),
                sesionGuardada.getIdSesion(),
                "SESION_CERRADA",
                "Una sesión fue cerrada"
        );

        return sesionGuardada;
    }

    @Override
    public Sesion marcarSesionComoExpirada(Integer idSesion) {
        Sesion sesion = sesionRepository.findByIdSesionAndEstado(idSesion, "activa")
                .orElseThrow(() -> new RuntimeException("Sesión activa no encontrada"));

        sesion.setEstado("expirada");
        Sesion sesionGuardada = sesionRepository.save(sesion);

        revocarRefreshTokensPorSesion(idSesion);

        notificarCambioSesion(
                sesionGuardada.getUsuario(),
                sesionGuardada.getIdSesion(),
                "SESION_EXPIRADA",
                "Una sesión expiró por inactividad"
        );

        return sesionGuardada;
    }

    @Override
    public void expirarSesionesVencidas() {
        List<Sesion> sesionesActivas = sesionRepository.findByEstado("activa");

        LocalDateTime ahora = LocalDateTime.now();

        for (Sesion sesion : sesionesActivas) {
            if (sesion.getFechaExpiracion().isBefore(ahora)) {
                sesion.setEstado("expirada");
                Sesion sesionGuardada = sesionRepository.save(sesion);

                revocarRefreshTokensPorSesion(sesionGuardada.getIdSesion());

                notificarCambioSesion(
                        sesionGuardada.getUsuario(),
                        sesionGuardada.getIdSesion(),
                        "SESION_EXPIRADA",
                        "Una sesión expiró por inactividad"
                );
            }
        }
    }

    @Override
    public void actualizarActividadSesion(Integer idSesion) {
        Sesion sesion = sesionRepository
                .findByIdSesionAndEstado(idSesion, "activa")
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        sesion.setFechaExpiracion(LocalDateTime.now().plusMinutes(25));

        sesionRepository.save(sesion);
    }

    @Override
    public Sesion obtenerSesionActiva(Integer idSesion) {
        return sesionRepository
                .findByIdSesionAndEstado(idSesion, "activa")
                .orElse(null);
    }

    @Override
    public void cerrarSesionesActivasDelMismoDispositivo(
            Usuario usuario,
            String ipAddress,
            String userAgent
    ) {
        List<Sesion> sesionesMismoDispositivo =
                sesionRepository.findByUsuarioAndEstadoAndIpAddressAndUserAgent(
                        usuario,
                        "activa",
                        ipAddress,
                        userAgent
                );

        for (Sesion sesion : sesionesMismoDispositivo) {
            sesion.setEstado("cerrada");
            Sesion sesionGuardada = sesionRepository.save(sesion);

            revocarRefreshTokensPorSesion(sesionGuardada.getIdSesion());

            notificarCambioSesion(
                    sesionGuardada.getUsuario(),
                    sesionGuardada.getIdSesion(),
                    "SESION_CERRADA",
                    "Una sesión anterior del mismo dispositivo fue cerrada"
            );
        }
    }

    private void revocarRefreshTokensPorSesion(Integer idSesion) {
        List<RefreshToken> tokens = refreshTokenRepository.findByIdSesion(idSesion);

        for (RefreshToken token : tokens) {
            token.setRevocado(true);
            refreshTokenRepository.save(token);
        }
    }

    private void notificarCambioSesion(
            Usuario usuario,
            Integer idSesion,
            String tipo,
            String mensaje
    ) {
        if (usuario == null || usuario.getIdUsuario() == null) {
            return;
        }

        SesionEventoDTO evento = new SesionEventoDTO(
                tipo,
                mensaje,
                idSesion,
                usuario.getIdUsuario()
        );

        sesionWebSocketService.notificarCambioSesion(
                usuario.getIdUsuario(),
                evento
        );
    }
}