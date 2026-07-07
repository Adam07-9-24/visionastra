package com.visionastra.api.service;

import com.visionastra.api.dto.GeneracionIARequestDTO;
import com.visionastra.api.dto.GeneracionIAResponseDTO;

import java.util.List;

public interface GeneracionIAService {

    List<GeneracionIAResponseDTO> listarGeneracionesDelUsuario(String emailUsuario);

    GeneracionIAResponseDTO obtenerGeneracionPorId(Integer idGeneracion, String emailUsuario);

    GeneracionIAResponseDTO crearGeneracion(GeneracionIARequestDTO request, String emailUsuario);

    GeneracionIAResponseDTO marcarComoProcesando(Integer idGeneracion, String emailUsuario);

    GeneracionIAResponseDTO prepararPrompt(Integer idGeneracion, String emailUsuario);

    GeneracionIAResponseDTO generarVideo(Integer idGeneracion, String emailUsuario);

    GeneracionIAResponseDTO marcarComoError(Integer idGeneracion, String mensajeError, String emailUsuario);

    void eliminarGeneracion(Integer idGeneracion, String emailUsuario);

}