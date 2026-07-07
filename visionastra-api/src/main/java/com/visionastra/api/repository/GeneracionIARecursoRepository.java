package com.visionastra.api.repository;

import com.visionastra.api.model.GeneracionIA;
import com.visionastra.api.model.GeneracionIARecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneracionIARecursoRepository extends JpaRepository<GeneracionIARecurso, Integer> {

    List<GeneracionIARecurso> findByGeneracionIA(GeneracionIA generacionIA);

    void deleteByGeneracionIA(GeneracionIA generacionIA);
}