package com.visionastra.api.repository;

import com.visionastra.api.model.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    @Query("""
            SELECT p
            FROM Publicacion p
            JOIN p.campana c
            JOIN c.usuario u
            WHERE u.email = :email
              AND (:idCampana IS NULL OR c.idCampana = :idCampana)
              AND (:estado IS NULL OR p.estado = :estado)
              AND (:plataforma IS NULL OR p.plataforma = :plataforma)
            ORDER BY p.fechaCreacion DESC
            """)
    List<Publicacion> buscarPorFiltros(
            @Param("email") String email,
            @Param("idCampana") Integer idCampana,
            @Param("estado") String estado,
            @Param("plataforma") String plataforma
    );

    boolean existsByRecurso_IdRecursoAndPlataformaAndEstadoInAndIdPublicacionNotAndCampana_Usuario_IdUsuario(
            Integer idRecurso,
            String plataforma,
            Collection<String> estados,
            Integer idPublicacion,
            Long idUsuario
    );
}
