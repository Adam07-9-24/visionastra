package com.visionastra.api.repository;

import com.visionastra.api.model.AgenteIA;
import com.visionastra.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgenteIARepository extends JpaRepository<AgenteIA, Integer> {

    List<AgenteIA> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);

    List<AgenteIA> findByUsuarioAndEstadoOrderByFechaCreacionDesc(Usuario usuario, String estado);

    List<AgenteIA> findByUsuarioAndNombreAndEstado(Usuario usuario, String nombre, String estado);
}