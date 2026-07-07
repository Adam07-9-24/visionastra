package com.visionastra.api.websocket;

import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.SesionRepository;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;

    public WebSocketAuthChannelInterceptor(
            JwtService jwtService,
            SesionRepository sesionRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.jwtService = jwtService;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Token WebSocket no enviado");
            }

            String token = authorizationHeader.substring(7);

            String email = jwtService.extractUsername(token);
            Integer idSesion = jwtService.extractIdSesion(token);

            if (email == null || idSesion == null) {
                throw new IllegalArgumentException("Token WebSocket inválido");
            }

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            if (!jwtService.isTokenValid(token, usuario.getEmail())) {
                throw new IllegalArgumentException("Token WebSocket expirado o inválido");
            }

            Sesion sesion = sesionRepository.findByIdSesionAndEstado(idSesion, "activa")
                    .orElseThrow(() -> new IllegalArgumentException("Sesión WebSocket no activa"));

            if (!sesion.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new IllegalArgumentException("La sesión no pertenece al usuario");
            }

            accessor.setUser(
                    new StompPrincipal(usuario.getIdUsuario().toString())
            );
        }

        return message;
    }
}