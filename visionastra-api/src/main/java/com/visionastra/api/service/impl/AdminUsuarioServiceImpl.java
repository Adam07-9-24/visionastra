package com.visionastra.api.service.impl;

import com.visionastra.api.dto.AdminUsuarioEstadoResponse;
import com.visionastra.api.dto.SesionEventoDTO;
import com.visionastra.api.exception.UsuarioNoEncontradoException;
import com.visionastra.api.model.RefreshToken;
import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.RefreshTokenRepository;
import com.visionastra.api.repository.SesionRepository;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.AdminUsuarioService;
import com.visionastra.api.service.SesionWebSocketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUsuarioServiceImpl implements AdminUsuarioService {

    private static final String ESTADO_USUARIO_ACTIVO = "activo";
    private static final String ESTADO_USUARIO_BLOQUEADO = "bloqueado";
    private static final String ESTADO_SESION_ACTIVA = "activa";
    private static final String ESTADO_SESION_CERRADA = "cerrada";

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SesionWebSocketService sesionWebSocketService;

    public AdminUsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            SesionRepository sesionRepository,
            RefreshTokenRepository refreshTokenRepository,
            SesionWebSocketService sesionWebSocketService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.sesionWebSocketService = sesionWebSocketService;
    }

    @Override
    @Transactional
    public AdminUsuarioEstadoResponse bloquearUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idUsuario));

        usuario.setEstado(ESTADO_USUARIO_BLOQUEADO);

        List<Sesion> sesionesActivas = sesionRepository.findByUsuarioAndEstado(
                usuario,
                ESTADO_SESION_ACTIVA
        );

        List<SesionEventoDTO> eventos = new ArrayList<>();

        for (Sesion sesion : sesionesActivas) {
            sesion.setEstado(ESTADO_SESION_CERRADA);
            eventos.add(new SesionEventoDTO(
                    "SESION_CERRADA",
                    "Sesion cerrada por bloqueo administrativo",
                    sesion.getIdSesion(),
                    usuario.getIdUsuario()
            ));
        }

        if (!sesionesActivas.isEmpty()) {
            sesionRepository.saveAll(sesionesActivas);
        }

        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUsuario(usuario);

        for (RefreshToken refreshToken : refreshTokens) {
            if (!Boolean.TRUE.equals(refreshToken.getRevocado())) {
                refreshToken.setRevocado(true);
            }
        }

        if (!refreshTokens.isEmpty()) {
            refreshTokenRepository.saveAll(refreshTokens);
        }

        usuarioRepository.save(usuario);

        notificarDespuesDelCommit(eventos);

        return new AdminUsuarioEstadoResponse(
                usuario.getIdUsuario(),
                usuario.getEstado(),
                "Usuario bloqueado correctamente."
        );
    }

    @Override
    @Transactional
    public AdminUsuarioEstadoResponse activarUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idUsuario));

        usuario.setEstado(ESTADO_USUARIO_ACTIVO);
        usuarioRepository.save(usuario);

        return new AdminUsuarioEstadoResponse(
                usuario.getIdUsuario(),
                usuario.getEstado(),
                "Usuario activado correctamente."
        );
    }

    private void notificarDespuesDelCommit(List<SesionEventoDTO> eventos) {
        if (eventos.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificarEventos(eventos);
                }
            });
            return;
        }

        notificarEventos(eventos);
    }

    private void notificarEventos(List<SesionEventoDTO> eventos) {
        for (SesionEventoDTO evento : eventos) {
            try {
                sesionWebSocketService.notificarCambioSesion(evento.getIdUsuario(), evento);
            } catch (Exception e) {
                System.err.println("No se pudo enviar evento WebSocket de bloqueo administrativo.");
            }
        }
    }
}
