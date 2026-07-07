package com.visionastra.api.service.impl;

import com.visionastra.api.dto.CampanaRequestDTO;
import com.visionastra.api.dto.CampanaResponseDTO;
import com.visionastra.api.model.Campana;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.CampanaRepository;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.CampanaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CampanaServiceImpl implements CampanaService {

    private final CampanaRepository campanaRepository;
    private final UsuarioRepository usuarioRepository;

    public CampanaServiceImpl(
            CampanaRepository campanaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.campanaRepository = campanaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<CampanaResponseDTO> listarCampanasDelUsuario(String email) {
        Usuario usuario = obtenerUsuarioPorEmail(email);

        return campanaRepository.findByUsuarioOrderByFechaCreacionDesc(usuario)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    public List<CampanaResponseDTO> listarCampanasPorEstado(String email, String estado) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        validarEstado(normalizarEstado(estado));

        return campanaRepository.findByUsuarioAndEstadoOrderByFechaCreacionDesc(
                        usuario,
                        normalizarEstado(estado)
                )
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    public CampanaResponseDTO obtenerCampanaPorId(String email, Integer idCampana) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Campana campana = obtenerCampanaDelUsuario(idCampana, usuario);

        return convertirADTO(campana);
    }

    @Override
    public CampanaResponseDTO crearCampana(String email, CampanaRequestDTO request) {
        Usuario usuario = obtenerUsuarioPorEmail(email);

        validarRequest(request);

        Campana campana = new Campana();
        campana.setUsuario(usuario);
        campana.setNombre(request.getNombre().trim());
        campana.setObjetivo(limpiarTexto(request.getObjetivo()));
        campana.setDescripcion(limpiarTexto(request.getDescripcion()));
        campana.setPresupuesto(validarPresupuesto(request.getPresupuesto()));
        campana.setEstado(normalizarEstado(request.getEstado()));
        campana.setFechaInicio(request.getFechaInicio());
        campana.setFechaFin(request.getFechaFin());

        validarCampanaActiva(campana);

        Campana campanaGuardada = campanaRepository.save(campana);

        return convertirADTO(campanaGuardada);
    }

    @Override
    public CampanaResponseDTO actualizarCampana(String email, Integer idCampana, CampanaRequestDTO request) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Campana campana = obtenerCampanaDelUsuario(idCampana, usuario);

        validarRequest(request);

        campana.setNombre(request.getNombre().trim());
        campana.setObjetivo(limpiarTexto(request.getObjetivo()));
        campana.setDescripcion(limpiarTexto(request.getDescripcion()));
        campana.setPresupuesto(validarPresupuesto(request.getPresupuesto()));
        campana.setEstado(normalizarEstado(request.getEstado()));
        campana.setFechaInicio(request.getFechaInicio());
        campana.setFechaFin(request.getFechaFin());

        validarCampanaActiva(campana);

        Campana campanaActualizada = campanaRepository.save(campana);

        return convertirADTO(campanaActualizada);
    }

    @Override
    public CampanaResponseDTO cambiarEstadoCampana(String email, Integer idCampana, String estado) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Campana campana = obtenerCampanaDelUsuario(idCampana, usuario);

        String estadoNormalizado = normalizarEstado(estado);
        validarEstado(estadoNormalizado);

        campana.setEstado(estadoNormalizado);

        validarCampanaActiva(campana);

        Campana campanaActualizada = campanaRepository.save(campana);

        return convertirADTO(campanaActualizada);
    }

    @Override
    public void eliminarCampana(String email, Integer idCampana) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Campana campana = obtenerCampanaDelUsuario(idCampana, usuario);

        campanaRepository.delete(campana);
    }

    private Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Campana obtenerCampanaDelUsuario(Integer idCampana, Usuario usuario) {
        return campanaRepository.findById(idCampana)
                .filter(campana -> campana.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada o no pertenece al usuario"));
    }

    private void validarRequest(CampanaRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("Los datos de la campaña son obligatorios");
        }

        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre de la campaña es obligatorio");
        }

        if (request.getNombre().trim().length() > 150) {
            throw new RuntimeException("El nombre de la campaña no puede superar los 150 caracteres");
        }

        if (request.getObjetivo() != null && request.getObjetivo().trim().length() > 200) {
            throw new RuntimeException("El objetivo no puede superar los 200 caracteres");
        }

        if (
                request.getFechaInicio() != null &&
                        request.getFechaFin() != null &&
                        request.getFechaFin().isBefore(request.getFechaInicio())
        ) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        validarEstado(normalizarEstado(request.getEstado()));
    }

    private void validarCampanaActiva(Campana campana) {
        if (!"activa".equals(campana.getEstado())) {
            return;
        }

        if (campana.getObjetivo() == null || campana.getObjetivo().trim().isEmpty()) {
            throw new RuntimeException("Completa el objetivo antes de activar la campaña");
        }

        if (campana.getPresupuesto() == null) {
            throw new RuntimeException("Completa el presupuesto antes de activar la campaña");
        }

        if (campana.getPresupuesto().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El presupuesto no puede ser negativo");
        }

        if (campana.getFechaInicio() == null) {
            throw new RuntimeException("Completa la fecha de inicio antes de activar la campaña");
        }

        if (campana.getFechaFin() == null) {
            throw new RuntimeException("Completa la fecha de fin antes de activar la campaña");
        }

        if (campana.getFechaFin().isBefore(campana.getFechaInicio())) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    private BigDecimal validarPresupuesto(BigDecimal presupuesto) {
        if (presupuesto == null) {
            return null;
        }

        if (presupuesto.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El presupuesto no puede ser negativo");
        }

        return presupuesto;
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return "borrador";
        }

        return estado.trim().toLowerCase();
    }

    private void validarEstado(String estado) {
        if (
                !"borrador".equals(estado) &&
                        !"activa".equals(estado) &&
                        !"pausada".equals(estado) &&
                        !"finalizada".equals(estado)
        ) {
            throw new RuntimeException("Estado de campaña no válido");
        }
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        String textoLimpio = texto.trim();

        if (textoLimpio.isEmpty()) {
            return null;
        }

        return textoLimpio;
    }

    private CampanaResponseDTO convertirADTO(Campana campana) {
        return new CampanaResponseDTO(
                campana.getIdCampana(),
                campana.getUsuario().getIdUsuario(),
                campana.getNombre(),
                campana.getObjetivo(),
                campana.getDescripcion(),
                campana.getPresupuesto(),
                campana.getEstado(),
                campana.getFechaInicio(),
                campana.getFechaFin(),
                campana.getFechaCreacion(),
                campana.getFechaActualizacion()
        );
    }
}