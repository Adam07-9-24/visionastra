package com.visionastra.api.repository;

import com.visionastra.api.model.Campana;
import com.visionastra.api.model.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Integer> {

    List<Recurso> findByCampanaOrderByFechaSubidaDesc(Campana campana);

    List<Recurso> findByCampanaAndEstadoOrderByFechaSubidaDesc(Campana campana, String estado);
}