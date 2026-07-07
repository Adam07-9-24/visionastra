package com.visionastra.api.service;

import com.visionastra.api.dto.RecursoRequestDTO;
import com.visionastra.api.dto.RecursoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecursoService {

    List<RecursoResponseDTO> listarRecursosPorCampana(String email, Integer idCampana);

    RecursoResponseDTO obtenerRecursoPorId(String email, Integer idRecurso);

    RecursoResponseDTO crearRecurso(String email, RecursoRequestDTO request);

    RecursoResponseDTO actualizarRecurso(String email, Integer idRecurso, RecursoRequestDTO request);

    RecursoResponseDTO archivarRecurso(String email, Integer idRecurso);

    RecursoResponseDTO desarchivarRecurso(String email, Integer idRecurso);

    RecursoResponseDTO actualizarTituloRecurso(String email, Integer idRecurso, String titulo);

    void eliminarRecurso(String email, Integer idRecurso);

    RecursoResponseDTO subirArchivoRecurso(
            String email,
            Integer idCampana,
            String tipo,
            String titulo,
            MultipartFile archivo
    );
}