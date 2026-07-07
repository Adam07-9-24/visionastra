package com.visionastra.api.service;

import com.visionastra.api.dto.CampanaRequestDTO;
import com.visionastra.api.dto.CampanaResponseDTO;

import java.util.List;

public interface CampanaService {

    List<CampanaResponseDTO> listarCampanasDelUsuario(String email);

    List<CampanaResponseDTO> listarCampanasPorEstado(String email, String estado);

    CampanaResponseDTO obtenerCampanaPorId(String email, Integer idCampana);

    CampanaResponseDTO crearCampana(String email, CampanaRequestDTO request);

    CampanaResponseDTO actualizarCampana(String email, Integer idCampana, CampanaRequestDTO request);

    CampanaResponseDTO cambiarEstadoCampana(String email, Integer idCampana, String estado);

    void eliminarCampana(String email, Integer idCampana);
}