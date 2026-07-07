package com.visionastra.api.repository;

import com.visionastra.api.model.Campana;
import com.visionastra.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampanaRepository extends JpaRepository<Campana, Integer> {

    List<Campana> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);

    List<Campana> findByUsuarioAndEstadoOrderByFechaCreacionDesc(Usuario usuario, String estado);
}