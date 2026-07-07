package com.visionastra.api.service;

import com.visionastra.api.dto.PublicacionRequestDTO;
import com.visionastra.api.dto.PublicacionResponseDTO;

import java.util.List;

public interface PublicacionService {

    List<PublicacionResponseDTO> listar(String email, Integer idCampana, String estado, String plataforma);

    PublicacionResponseDTO obtenerPorId(Integer idPublicacion, String email);

    PublicacionResponseDTO crear(PublicacionRequestDTO request, String email);

    PublicacionResponseDTO actualizar(Integer idPublicacion, PublicacionRequestDTO request, String email);

    PublicacionResponseDTO cancelar(Integer idPublicacion, String email);

    PublicacionResponseDTO enviarAN8n(Integer idPublicacion, String email);
}
