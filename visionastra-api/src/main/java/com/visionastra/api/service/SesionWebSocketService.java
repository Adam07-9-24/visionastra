package com.visionastra.api.service;

import com.visionastra.api.dto.SesionEventoDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SesionWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public SesionWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notificarCambioSesion(Long idUsuario, SesionEventoDTO evento) {
        if (idUsuario == null || evento == null) {
            return;
        }

        try {
            messagingTemplate.convertAndSendToUser(
                    idUsuario.toString(),
                    "/queue/sesiones",
                    evento
            );
        } catch (Exception e) {
            System.err.println("No se pudo enviar evento WebSocket de sesión.");
        }
    }
}