package com.visionastra.api.service.impl;

import com.visionastra.api.dto.GeneracionIARequestDTO;
import com.visionastra.api.dto.GeneracionIAResponseDTO;
import com.visionastra.api.model.*;
import com.visionastra.api.repository.*;
import com.visionastra.api.service.GeneracionIAService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.visionastra.api.dto.PromptIAResultadoDTO;
import com.visionastra.api.service.ia.PromptIAService;
import com.visionastra.api.service.ia.VideoIAService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeneracionIAServiceImpl implements GeneracionIAService {

    private final GeneracionIARepository generacionIARepository;
    private final GeneracionIARecursoRepository generacionIARecursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CampanaRepository campanaRepository;
    private final RecursoRepository recursoRepository;
    private final AgenteIARepository agenteIARepository;
    private final PromptIAService promptIAService;
    private final VideoIAService videoIAService;

    public GeneracionIAServiceImpl(
            GeneracionIARepository generacionIARepository,
            GeneracionIARecursoRepository generacionIARecursoRepository,
            UsuarioRepository usuarioRepository,
            CampanaRepository campanaRepository,
            RecursoRepository recursoRepository,
            AgenteIARepository agenteIARepository,
            PromptIAService promptIAService,
            VideoIAService videoIAService
    ) {
        this.generacionIARepository = generacionIARepository;
        this.generacionIARecursoRepository = generacionIARecursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.campanaRepository = campanaRepository;
        this.recursoRepository = recursoRepository;
        this.agenteIARepository = agenteIARepository;
        this.promptIAService = promptIAService;
        this.videoIAService = videoIAService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeneracionIAResponseDTO> listarGeneracionesDelUsuario(String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);

        return generacionIARepository.findByUsuarioOrderByFechaCreacionDesc(usuario)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneracionIAResponseDTO obtenerGeneracionPorId(Integer idGeneracion, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        GeneracionIA generacion = obtenerGeneracionPropia(idGeneracion, usuario);

        return convertirAResponse(generacion);
    }

    @Override
    @Transactional
    public GeneracionIAResponseDTO crearGeneracion(GeneracionIARequestDTO request, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);

        validarRequest(request);

        Campana campana = campanaRepository.findById(request.getIdCampana())
                .orElseThrow(() -> new RuntimeException("La campaña no existe."));

        if (!campana.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No puedes usar una campaña que no te pertenece.");
        }

        validarCampanaActiva(campana);

        AgenteIA agente = null;
        if (request.getIdAgente() != null) {
            agente = agenteIARepository.findById(request.getIdAgente())
                    .orElseThrow(() -> new RuntimeException("El agente IA no existe."));

            if (!agente.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new RuntimeException("No puedes usar un agente IA que no te pertenece.");
            }

            if (!"activo".equalsIgnoreCase(agente.getEstado())) {
                throw new RuntimeException("El agente IA seleccionado no está activo.");
            }
        }

        GeneracionIA generacion = new GeneracionIA();
        generacion.setUsuario(usuario);
        generacion.setCampana(campana);
        generacion.setAgente(agente);
        generacion.setPrompt(request.getPrompt().trim());
        generacion.setTipoSalida(request.getTipoSalida().trim().toLowerCase());
        generacion.setProveedorPrompt("openai");
        generacion.setProveedorVideo("google_veo");
        generacion.setEstado("pendiente");

        GeneracionIA guardada = generacionIARepository.save(generacion);

        if (request.getIdsRecursos() != null && !request.getIdsRecursos().isEmpty()) {
            for (Integer idRecurso : request.getIdsRecursos()) {
                Recurso recurso = recursoRepository.findById(idRecurso)
                        .orElseThrow(() -> new RuntimeException("Uno de los recursos seleccionados no existe."));

                if (!recurso.getCampana().getIdCampana().equals(campana.getIdCampana())) {
                    throw new RuntimeException("Todos los recursos deben pertenecer a la campaña seleccionada.");
                }

                if (!"activo".equalsIgnoreCase(recurso.getEstado())) {
                    throw new RuntimeException("Solo puedes usar recursos activos para una generación IA.");
                }

                GeneracionIARecurso generacionRecurso = new GeneracionIARecurso();
                generacionRecurso.setGeneracionIA(guardada);
                generacionRecurso.setRecurso(recurso);
                generacionRecurso.setRolRecurso("entrada");

                generacionIARecursoRepository.save(generacionRecurso);
            }
        }

        GeneracionIA generacionRecargada = generacionIARepository.findById(guardada.getIdGeneracion())
                .orElseThrow(() -> new RuntimeException("No se pudo recargar la generación IA creada."));

        return convertirAResponse(generacionRecargada);
    }

    private Path obtenerPrimeraImagenReferencia(GeneracionIA generacion) {
        List<GeneracionIARecurso> relaciones =
                generacionIARecursoRepository.findByGeneracionIA(generacion);

        for (GeneracionIARecurso relacion : relaciones) {
            Recurso recurso = relacion.getRecurso();

            if (recurso == null) {
                continue;
            }

            if (!"imagen".equalsIgnoreCase(recurso.getTipo())) {
                continue;
            }

            if (!"activo".equalsIgnoreCase(recurso.getEstado())) {
                throw new RuntimeException("La imagen de referencia debe estar activa.");
            }

            if (recurso.getCampana() == null
                    || !recurso.getCampana().getIdCampana().equals(generacion.getCampana().getIdCampana())) {
                throw new RuntimeException("La imagen de referencia no pertenece a la campaña seleccionada.");
            }

            if (recurso.getUrlArchivo() == null || recurso.getUrlArchivo().isBlank()) {
                throw new RuntimeException("La imagen de referencia no tiene archivo asociado.");
            }

            String rutaGuardada = recurso.getUrlArchivo().trim().replace("\\", "/");

            // Si viene como "/uploads/...", quitar el "/" inicial para que sea relativo al proyecto.
            if (rutaGuardada.startsWith("/")) {
                rutaGuardada = rutaGuardada.substring(1);
            }

            Path rutaImagen = Paths.get(rutaGuardada).normalize();

            if (!rutaImagen.isAbsolute()) {
                rutaImagen = Paths.get("").toAbsolutePath().resolve(rutaImagen).normalize();
            }

            if (!Files.exists(rutaImagen)) {
                throw new RuntimeException("No se encontró el archivo físico de la imagen de referencia.");
            }

            return rutaImagen;
        }

        return null;
    }


    @Override
    @Transactional
    public GeneracionIAResponseDTO generarVideo(Integer idGeneracion, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        GeneracionIA generacion = obtenerGeneracionPropia(idGeneracion, usuario);

        validarCampanaActiva(generacion.getCampana());

        if (generacion.getPromptFinal() == null || generacion.getPromptFinal().isBlank()) {
            throw new RuntimeException("Primero debes preparar el prompt final antes de generar el video.");
        }

        if ("completado".equalsIgnoreCase(generacion.getEstado())) {
            throw new RuntimeException("Esta generación IA ya está completada.");
        }

        try {
            generacion.setEstado("procesando");
            generacion.setMensajeError(null);
            generacionIARepository.save(generacion);

            Path imagenReferencia = obtenerPrimeraImagenReferencia(generacion);

            String rutaVideoTemporal = imagenReferencia != null
                    ? videoIAService.generarYDescargarVideo(generacion.getPromptFinal(), imagenReferencia)
                    : videoIAService.generarYDescargarVideo(generacion.getPromptFinal());

            Recurso recursoVideo = crearRecursoVideoGenerado(generacion, rutaVideoTemporal);

            generacion.setRecursoResultado(recursoVideo);
            generacion.setEstado("completado");
            generacion.setMensajeError(null);

            GeneracionIA actualizada = generacionIARepository.save(generacion);

            return convertirAResponse(actualizada);

        } catch (Exception e) {
            generacion.setEstado("error");
            generacion.setMensajeError(e.getMessage());
            generacionIARepository.save(generacion);

            throw new RuntimeException("No se pudo generar el video con Gemini/Veo: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional
    public GeneracionIAResponseDTO marcarComoProcesando(Integer idGeneracion, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        GeneracionIA generacion = obtenerGeneracionPropia(idGeneracion, usuario);

        if (!"pendiente".equalsIgnoreCase(generacion.getEstado())) {
            throw new RuntimeException("Solo una generación pendiente puede pasar a procesando.");
        }

        generacion.setEstado("procesando");
        generacion.setMensajeError(null);

        return convertirAResponse(generacionIARepository.save(generacion));
    }

    @Override
    @Transactional
    public GeneracionIAResponseDTO prepararPrompt(Integer idGeneracion, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        GeneracionIA generacion = obtenerGeneracionPropia(idGeneracion, usuario);

        validarCampanaActiva(generacion.getCampana());

        if ("completado".equalsIgnoreCase(generacion.getEstado())) {
            throw new RuntimeException("No puedes preparar el prompt de una generación ya completada.");
        }

        if ("error".equalsIgnoreCase(generacion.getEstado())) {
            throw new RuntimeException("No puedes preparar el prompt de una generación en error.");
        }

        List<GeneracionIARecurso> relaciones =
                generacionIARecursoRepository.findByGeneracionIA(generacion);

        StringBuilder contextoRecursos = new StringBuilder();

        for (GeneracionIARecurso relacion : relaciones) {
            Recurso recurso = relacion.getRecurso();

            contextoRecursos
                    .append("- Recurso: ")
                    .append(recurso.getTitulo() != null ? recurso.getTitulo() : recurso.getNombreArchivo())
                    .append(" | Tipo: ")
                    .append(recurso.getTipo());

            if ("copy".equalsIgnoreCase(recurso.getTipo()) && recurso.getContenidoTexto() != null) {
                contextoRecursos
                        .append(" | Texto: ")
                        .append(recurso.getContenidoTexto());
            }

            contextoRecursos.append("\n");
        }

        String contexto = """
            Campaña:
            Nombre: %s
            Objetivo: %s
            Descripción: %s
            Estado: %s

            Prompt original del usuario:
            %s

            Tipo de salida solicitado:
            %s

            Recursos seleccionados:
            %s
            """.formatted(
                generacion.getCampana().getNombre(),
                generacion.getCampana().getObjetivo() != null ? generacion.getCampana().getObjetivo() : "No definido",
                generacion.getCampana().getDescripcion() != null ? generacion.getCampana().getDescripcion() : "No definida",
                generacion.getCampana().getEstado(),
                generacion.getPrompt(),
                generacion.getTipoSalida(),
                contextoRecursos.length() > 0 ? contextoRecursos.toString() : "No se seleccionaron recursos."
        );

        try {
            generacion.setEstado("procesando");
            generacion.setMensajeError(null);

            PromptIAResultadoDTO resultadoIA = promptIAService.prepararPromptProfesional(contexto);

            generacion.setResumenContexto(resultadoIA.getResumenContexto());
            generacion.setGuionGenerado(resultadoIA.getGuionGenerado());
            generacion.setPromptFinalEspanol(resultadoIA.getPromptFinalEspanol());
            generacion.setPromptFinal(resultadoIA.getPromptFinal());
            generacion.setProveedorPrompt("openai");
            generacion.setProveedorVideo("google_veo");

            GeneracionIA actualizada = generacionIARepository.save(generacion);

            return convertirAResponse(actualizada);
        } catch (Exception e) {
            generacion.setEstado("error");
            generacion.setMensajeError(e.getMessage());
            generacionIARepository.save(generacion);

            throw new RuntimeException("No se pudo preparar el prompt con OpenAI: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public GeneracionIAResponseDTO marcarComoError(Integer idGeneracion, String mensajeError, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        GeneracionIA generacion = obtenerGeneracionPropia(idGeneracion, usuario);

        generacion.setEstado("error");
        generacion.setMensajeError(
                mensajeError == null || mensajeError.isBlank()
                        ? "Ocurrió un error durante la generación IA."
                        : mensajeError.trim()
        );

        return convertirAResponse(generacionIARepository.save(generacion));
    }

    @Override
    @Transactional
    public void eliminarGeneracion(Integer idGeneracion, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        GeneracionIA generacion = obtenerGeneracionPropia(idGeneracion, usuario);

        if ("procesando".equalsIgnoreCase(generacion.getEstado())) {
            throw new RuntimeException("No puedes eliminar una generación que está procesando.");
        }

        generacionIARecursoRepository.deleteByGeneracionIA(generacion);
        generacionIARepository.delete(generacion);
    }

    private void validarCampanaActiva(Campana campana) {
        if (campana == null || !"activa".equalsIgnoreCase(campana.getEstado())) {
            throw new RuntimeException("Esta campaña debe estar activa para usar el Generador IA.");
        }
    }

    private Recurso crearRecursoVideoGenerado(GeneracionIA generacion, String rutaVideoTemporal) {
        try {
            Path rutaTemporal = Paths.get(rutaVideoTemporal);

            if (!Files.exists(rutaTemporal)) {
                throw new RuntimeException("El video generado no existe en la ruta temporal.");
            }

            Campana campana = generacion.getCampana();

            Path carpetaDestino = Paths.get(
                    "uploads",
                    "recursos",
                    String.valueOf(campana.getIdCampana())
            );

            Files.createDirectories(carpetaDestino);

            String nombreArchivo = "video-ia-generacion-"
                    + generacion.getIdGeneracion()
                    + "-"
                    + System.currentTimeMillis()
                    + ".mp4";

            Path rutaDestino = carpetaDestino.resolve(nombreArchivo);

            Files.move(
                    rutaTemporal,
                    rutaDestino,
                    StandardCopyOption.REPLACE_EXISTING
            );

            long bytes = Files.size(rutaDestino);

            BigDecimal pesoMb = BigDecimal.valueOf(bytes)
                    .divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);

            Recurso recurso = new Recurso();
            recurso.setCampana(campana);
            recurso.setTipo("video");
            recurso.setTitulo("Video IA - " + campana.getNombre());
            recurso.setNombreArchivo(nombreArchivo);
            recurso.setUrlArchivo(rutaDestino.toString().replace("\\", "/"));
            recurso.setContenidoTexto(null);
            recurso.setPesoMb(pesoMb);
            recurso.setFormato("mp4");
            recurso.setEstado("activo");

            return recursoRepository.save(recurso);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo registrar el video generado como recurso: " + e.getMessage(), e);
        }
    }

    private void validarRequest(GeneracionIARequestDTO request) {
        if (request.getIdCampana() == null) {
            throw new RuntimeException("La campaña es obligatoria.");
        }

        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            throw new RuntimeException("El prompt es obligatorio.");
        }

        if (request.getPrompt().trim().length() < 10) {
            throw new RuntimeException("El prompt debe tener al menos 10 caracteres.");
        }

        if (request.getTipoSalida() == null || request.getTipoSalida().isBlank()) {
            throw new RuntimeException("El tipo de salida es obligatorio.");
        }

        String tipoSalida = request.getTipoSalida().trim().toLowerCase();

        if (!tipoSalida.equals("copy") && !tipoSalida.equals("imagen") && !tipoSalida.equals("video")) {
            throw new RuntimeException("Tipo de salida inválido. Usa copy, imagen o video.");
        }
    }

    private Usuario obtenerUsuarioPorEmail(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado."));
    }

    private GeneracionIA obtenerGeneracionPropia(Integer idGeneracion, Usuario usuario) {
        GeneracionIA generacion = generacionIARepository.findById(idGeneracion)
                .orElseThrow(() -> new RuntimeException("La generación IA no existe."));

        if (!generacion.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No tienes permiso para acceder a esta generación IA.");
        }

        return generacion;
    }

    private GeneracionIAResponseDTO convertirAResponse(GeneracionIA generacion) {
        GeneracionIAResponseDTO dto = new GeneracionIAResponseDTO();

        dto.setIdGeneracion(generacion.getIdGeneracion());

        if (generacion.getUsuario() != null) {
            dto.setIdUsuario(generacion.getUsuario().getIdUsuario());
            dto.setNombreUsuario(generacion.getUsuario().getNombres());
        }

        if (generacion.getCampana() != null) {
            dto.setIdCampana(generacion.getCampana().getIdCampana());
            dto.setNombreCampana(generacion.getCampana().getNombre());
        }

        if (generacion.getAgente() != null) {
            dto.setIdAgente(generacion.getAgente().getIdAgente());
            dto.setNombreAgente(generacion.getAgente().getNombre());
        }

        dto.setPrompt(generacion.getPrompt());
        dto.setResumenContexto(generacion.getResumenContexto());
        dto.setGuionGenerado(generacion.getGuionGenerado());
        dto.setPromptFinalEspanol(generacion.getPromptFinalEspanol());
        dto.setPromptFinal(generacion.getPromptFinal());
        dto.setProveedorPrompt(generacion.getProveedorPrompt());
        dto.setProveedorVideo(generacion.getProveedorVideo());
        dto.setTipoSalida(generacion.getTipoSalida());
        dto.setEstado(generacion.getEstado());
        dto.setMensajeError(generacion.getMensajeError());

        if (generacion.getRecursoResultado() != null) {
            dto.setIdRecursoResultado(generacion.getRecursoResultado().getIdRecurso());
            dto.setTituloRecursoResultado(generacion.getRecursoResultado().getTitulo());
            dto.setTipoRecursoResultado(generacion.getRecursoResultado().getTipo());
        }

        List<GeneracionIAResponseDTO.RecursoEntradaDTO> recursosEntrada = new ArrayList<>();

        List<GeneracionIARecurso> relaciones =
                generacionIARecursoRepository.findByGeneracionIA(generacion);

        for (GeneracionIARecurso relacion : relaciones) {
            Recurso recurso = relacion.getRecurso();

            GeneracionIAResponseDTO.RecursoEntradaDTO recursoDTO =
                    new GeneracionIAResponseDTO.RecursoEntradaDTO();

            recursoDTO.setIdRecurso(recurso.getIdRecurso());
            recursoDTO.setTitulo(recurso.getTitulo());
            recursoDTO.setTipo(recurso.getTipo());
            recursoDTO.setNombreArchivo(recurso.getNombreArchivo());
            recursoDTO.setRolRecurso(relacion.getRolRecurso());

            recursosEntrada.add(recursoDTO);
        }

        dto.setRecursosEntrada(recursosEntrada);
        dto.setFechaCreacion(generacion.getFechaCreacion());
        dto.setFechaActualizacion(generacion.getFechaActualizacion());

        return dto;
    }
}