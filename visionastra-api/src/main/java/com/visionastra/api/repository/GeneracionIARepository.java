package com.visionastra.api.repository;

import com.visionastra.api.model.GeneracionIA;
import com.visionastra.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneracionIARepository extends JpaRepository<GeneracionIA, Integer> {

    List<GeneracionIA> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);

    List<GeneracionIA> findByUsuarioAndEstadoOrderByFechaCreacionDesc(Usuario usuario, String estado);
}