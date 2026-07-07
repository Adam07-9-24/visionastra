package com.visionastra.api.repository;

import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SesionRepository extends JpaRepository<Sesion, Integer> {

    List<Sesion> findByUsuarioAndEstado(Usuario usuario, String estado);

    Optional<Sesion> findByIdSesionAndEstado(Integer idSesion, String estado);

    List<Sesion> findByEstado(String estado);

    int countByUsuarioAndEstado(Usuario usuario, String estado);

    Optional<Sesion> findFirstByUsuarioAndEstadoOrderByFechaInicioAsc(Usuario usuario, String estado);

    // 🔥 NUEVO
    List<Sesion> findByUsuarioAndEstadoAndIpAddressAndUserAgent(
            Usuario usuario,
            String estado,
            String ipAddress,
            String userAgent
    );
}