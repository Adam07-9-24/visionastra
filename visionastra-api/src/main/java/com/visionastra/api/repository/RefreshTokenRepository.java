package com.visionastra.api.repository;

import com.visionastra.api.model.RefreshToken;
import com.visionastra.api.model.Usuario; // 🔥 FALTABA ESTO
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    List<RefreshToken> findByRevocadoFalse();

    List<RefreshToken> findByUsuario(Usuario usuario);

    List<RefreshToken> findByIdSesion(Integer idSesion);
}