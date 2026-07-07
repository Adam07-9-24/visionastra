package com.visionastra.api.service;

import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public interface SesionService {

    Sesion crearSesion(Usuario usuario, String dispositivo, String ipAddress, String userAgent, LocalDateTime fechaExpiracion);

    List<Sesion> listarSesionesActivasPorUsuario(Usuario usuario);

    Sesion cerrarSesionPorIdYUsuario(Integer idSesion, Usuario usuario);

    Sesion marcarSesionComoExpirada(Integer idSesion);

    // 🔥 NUEVO → HEARTBEAT
    void actualizarActividadSesion(Integer idSesion);

    Sesion obtenerSesionActiva(Integer idSesion);
    // 🔥 NUEVO: expirar automáticamente sesiones vencidas
    void expirarSesionesVencidas();

    // 🔥 NUEVO: evitar sesiones duplicadas del mismo navegador/dispositivo
    void cerrarSesionesActivasDelMismoDispositivo(Usuario usuario, String ipAddress, String userAgent);

}
