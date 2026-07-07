package com.visionastra.api.service.impl;

import com.visionastra.api.dto.RecursoRequestDTO;
import com.visionastra.api.dto.RecursoResponseDTO;
import com.visionastra.api.model.Campana;
import com.visionastra.api.model.Recurso;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.CampanaRepository;
import com.visionastra.api.repository.RecursoRepository;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.RecursoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RecursoServiceImpl implements RecursoService {

    private final RecursoRepository recursoRepository;
    private final CampanaRepository campanaRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${visionastra.uploads.path}")
    private String uploadsPath;

    public RecursoServiceImpl(
            RecursoRepository recursoRepository,
            CampanaRepository campanaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.recursoRepository = recursoRepository;
        this.campanaRepository = campanaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<RecursoResponseDTO> listarRecursosPorCampana(String email, Integer idCampana) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Campana campana = obtenerCampanaDelUsuario(idCampana, usuario);

        return recursoRepository.findByCampanaOrderByFechaSubidaDesc(campana)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    public RecursoResponseDTO obtenerRecursoPorId(String email, Integer idRecurso) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Recurso recurso = obtenerRecursoDelUsuario(idRecurso, usuario);

        return convertirADTO(recurso);
    }

    @Override
    public RecursoResponseDTO crearRecurso(String email, RecursoRequestDTO request) {
        Usuario usuario = obtenerUsuarioPorEmail(email);

        validarRequestBasico(request);

        Campana campana = obtenerCampanaDelUsuario(request.getIdCampana(), usuario);
        validarCampanaEditable(campana);

        String tipoNormalizado = normalizarTipo(request.getTipo());

        validarTipo(tipoNormalizado);
        validarContenidoSegunTipo(tipoNormalizado, request);

        Recurso recurso = new Recurso();
        recurso.setCampana(campana);
        recurso.setTipo(tipoNormalizado);
        recurso.setTitulo(limpiarTexto(request.getTitulo()));
        recurso.setNombreArchivo(limpiarNombreArchivo(request.getNombreArchivo()));
        recurso.setUrlArchivo(limpiarTexto(request.getUrlArchivo()));
        recurso.setContenidoTexto(limpiarTexto(request.getContenidoTexto()));
        recurso.setPesoMb(validarPeso(request.getPesoMb()));
        recurso.setFormato(limpiarTexto(request.getFormato()));
        recurso.setEstado("activo");

        Recurso recursoGuardado = recursoRepository.save(recurso);

        return convertirADTO(recursoGuardado);
    }

    @Override
    public RecursoResponseDTO actualizarRecurso(String email, Integer idRecurso, RecursoRequestDTO request) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Recurso recurso = obtenerRecursoDelUsuario(idRecurso, usuario);

        validarCampanaEditable(recurso.getCampana());
        validarRequestBasico(request);

        String tipoNormalizado = normalizarTipo(request.getTipo());

        validarTipo(tipoNormalizado);
        validarContenidoSegunTipo(tipoNormalizado, request);

        recurso.setTipo(tipoNormalizado);
        recurso.setTitulo(limpiarTexto(request.getTitulo()));
        recurso.setNombreArchivo(limpiarNombreArchivo(request.getNombreArchivo()));
        recurso.setUrlArchivo(limpiarTexto(request.getUrlArchivo()));
        recurso.setContenidoTexto(limpiarTexto(request.getContenidoTexto()));
        recurso.setPesoMb(validarPeso(request.getPesoMb()));
        recurso.setFormato(limpiarTexto(request.getFormato()));

        Recurso recursoActualizado = recursoRepository.save(recurso);

        return convertirADTO(recursoActualizado);
    }

    @Override
    public RecursoResponseDTO actualizarTituloRecurso(String email, Integer idRecurso, String titulo) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Recurso recurso = obtenerRecursoDelUsuario(idRecurso, usuario);

        validarCampanaEditable(recurso.getCampana());

        String tituloLimpio = limpiarTexto(titulo);

        if (tituloLimpio == null) {
            throw new RuntimeException("El título del recurso es obligatorio.");
        }

        if (tituloLimpio.length() > 200) {
            throw new RuntimeException("El título del recurso no puede superar los 200 caracteres.");
        }

        recurso.setTitulo(tituloLimpio);

        Recurso recursoActualizado = recursoRepository.save(recurso);

        return convertirADTO(recursoActualizado);
    }

    @Override
    public RecursoResponseDTO archivarRecurso(String email, Integer idRecurso) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Recurso recurso = obtenerRecursoDelUsuario(idRecurso, usuario);

        validarCampanaEditable(recurso.getCampana());

        recurso.setEstado("archivado");

        Recurso recursoArchivado = recursoRepository.save(recurso);

        return convertirADTO(recursoArchivado);
    }

    @Override
    public RecursoResponseDTO desarchivarRecurso(String email, Integer idRecurso) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Recurso recurso = obtenerRecursoDelUsuario(idRecurso, usuario);

        validarCampanaEditable(recurso.getCampana());

        recurso.setEstado("activo");

        Recurso recursoDesarchivado = recursoRepository.save(recurso);

        return convertirADTO(recursoDesarchivado);
    }

    @Override
    public void eliminarRecurso(String email, Integer idRecurso) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Recurso recurso = obtenerRecursoDelUsuario(idRecurso, usuario);

        validarCampanaEditable(recurso.getCampana());

        recursoRepository.delete(recurso);
    }

    @Override
    public RecursoResponseDTO subirArchivoRecurso(
            String email,
            Integer idCampana,
            String tipo,
            String titulo,
            MultipartFile archivo
    ) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        Campana campana = obtenerCampanaDelUsuario(idCampana, usuario);

        validarCampanaEditable(campana);

        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Debe seleccionar un archivo");
        }

        if (tipo == null || tipo.trim().isEmpty()) {
            throw new RuntimeException("El tipo de recurso es obligatorio");
        }

        String tipoNormalizado = normalizarTipo(tipo);

        if ("copy".equals(tipoNormalizado)) {
            throw new RuntimeException("El tipo copy no debe usar subida de archivos");
        }

        if (
                !"imagen".equals(tipoNormalizado) &&
                        !"video".equals(tipoNormalizado) &&
                        !"documento".equals(tipoNormalizado)
        ) {
            throw new RuntimeException("Tipo de recurso no válido para subida de archivo");
        }

        String nombreOriginal = archivo.getOriginalFilename();

        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            throw new RuntimeException("El archivo debe tener un nombre válido");
        }

        if (nombreOriginal.trim().length() > 255) {
            throw new RuntimeException("El nombre del archivo no puede superar los 255 caracteres");
        }

        if (titulo != null && titulo.trim().length() > 150) {
            throw new RuntimeException("El título no puede superar los 150 caracteres");
        }

        String extension = obtenerExtension(nombreOriginal);

        validarExtensionPorTipo(tipoNormalizado, extension);
        validarPesoArchivoPorTipo(tipoNormalizado, archivo.getSize());

        try {
            Path carpetaDestino = Paths.get(uploadsPath, "recursos", idCampana.toString())
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(carpetaDestino);

            String nombreSeguro = limpiarNombreArchivoParaGuardar(nombreOriginal);
            String nombreGenerado = System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + nombreSeguro;

            Path rutaDestino = carpetaDestino.resolve(nombreGenerado).normalize();

            Files.copy(
                    archivo.getInputStream(),
                    rutaDestino,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String urlArchivo = "/uploads/recursos/" + idCampana + "/" + nombreGenerado;

            BigDecimal pesoMb = BigDecimal.valueOf(archivo.getSize())
                    .divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);

            Recurso recurso = new Recurso();
            recurso.setCampana(campana);
            recurso.setTipo(tipoNormalizado);
            recurso.setTitulo(limpiarTexto(titulo));
            recurso.setNombreArchivo(nombreOriginal.trim());
            recurso.setUrlArchivo(urlArchivo);
            recurso.setContenidoTexto(null);
            recurso.setPesoMb(pesoMb);
            recurso.setFormato(extension);
            recurso.setEstado("activo");

            Recurso recursoGuardado = recursoRepository.save(recurso);

            return convertirADTO(recursoGuardado);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo localmente");
        }
    }

    private Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Campana obtenerCampanaDelUsuario(Integer idCampana, Usuario usuario) {
        if (idCampana == null) {
            throw new RuntimeException("La campaña es obligatoria");
        }

        return campanaRepository.findById(idCampana)
                .filter(campana -> campana.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada o no pertenece al usuario"));
    }

    private Recurso obtenerRecursoDelUsuario(Integer idRecurso, Usuario usuario) {
        if (idRecurso == null) {
            throw new RuntimeException("El recurso es obligatorio");
        }

        return recursoRepository.findById(idRecurso)
                .filter(recurso -> recurso.getCampana().getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado o no pertenece al usuario"));
    }

    private void validarCampanaEditable(Campana campana) {
        if ("finalizada".equalsIgnoreCase(campana.getEstado())) {
            throw new RuntimeException("No se pueden administrar recursos de una campaña finalizada");
        }
    }

    private void validarRequestBasico(RecursoRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("Los datos del recurso son obligatorios");
        }

        if (request.getTipo() == null || request.getTipo().trim().isEmpty()) {
            throw new RuntimeException("El tipo de recurso es obligatorio");
        }

        if (request.getNombreArchivo() == null || request.getNombreArchivo().trim().isEmpty()) {
            throw new RuntimeException("El nombre del recurso es obligatorio");
        }

        if (request.getNombreArchivo().trim().length() > 255) {
            throw new RuntimeException("El nombre del recurso no puede superar los 255 caracteres");
        }

        if (request.getTitulo() != null && request.getTitulo().trim().length() > 150) {
            throw new RuntimeException("El título no puede superar los 150 caracteres");
        }

        if (request.getUrlArchivo() != null && request.getUrlArchivo().trim().length() > 500) {
            throw new RuntimeException("La URL del archivo no puede superar los 500 caracteres");
        }

        if (request.getFormato() != null && request.getFormato().trim().length() > 20) {
            throw new RuntimeException("El formato no puede superar los 20 caracteres");
        }
    }

    private String normalizarTipo(String tipo) {
        return tipo.trim().toLowerCase();
    }

    private void validarTipo(String tipo) {
        if (
                !"imagen".equals(tipo) &&
                        !"video".equals(tipo) &&
                        !"documento".equals(tipo) &&
                        !"copy".equals(tipo)
        ) {
            throw new RuntimeException("Tipo de recurso no válido");
        }
    }

    private void validarContenidoSegunTipo(String tipo, RecursoRequestDTO request) {
        if ("copy".equals(tipo)) {
            if (request.getContenidoTexto() == null || request.getContenidoTexto().trim().isEmpty()) {
                throw new RuntimeException("El contenido del copy es obligatorio");
            }

            return;
        }

        if (request.getUrlArchivo() == null || request.getUrlArchivo().trim().isEmpty()) {
            throw new RuntimeException("La URL del archivo es obligatoria para imagen, video o documento");
        }
    }

    private BigDecimal validarPeso(BigDecimal pesoMb) {
        if (pesoMb == null) {
            return null;
        }

        if (pesoMb.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El peso del recurso no puede ser negativo");
        }

        return pesoMb;
    }

    private String limpiarNombreArchivo(String nombreArchivo) {
        return nombreArchivo.trim();
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

    private String obtenerExtension(String nombreArchivo) {
        int index = nombreArchivo.lastIndexOf(".");

        if (index == -1 || index == nombreArchivo.length() - 1) {
            throw new RuntimeException("El archivo no tiene una extensión válida");
        }

        return nombreArchivo.substring(index + 1).toLowerCase();
    }

    private void validarExtensionPorTipo(String tipo, String extension) {
        Set<String> extensionesImagen = Set.of("jpg", "jpeg", "png", "webp");
        Set<String> extensionesVideo = Set.of("mp4", "webm", "mov");
        Set<String> extensionesDocumento = Set.of("pdf", "doc", "docx");

        switch (tipo) {
            case "imagen" -> {
                if (!extensionesImagen.contains(extension)) {
                    throw new RuntimeException("Formato de imagen no permitido. Usa jpg, jpeg, png o webp");
                }
            }
            case "video" -> {
                if (!extensionesVideo.contains(extension)) {
                    throw new RuntimeException("Formato de video no permitido. Usa mp4, webm o mov");
                }
            }
            case "documento" -> {
                if (!extensionesDocumento.contains(extension)) {
                    throw new RuntimeException("Formato de documento no permitido. Usa pdf, doc o docx");
                }
            }
            default -> throw new RuntimeException("Tipo de recurso no válido");
        }
    }

    private void validarPesoArchivoPorTipo(String tipo, long sizeBytes) {
        long mb = 1024L * 1024L;

        long maxImagen = 10L * mb;
        long maxVideo = 100L * mb;
        long maxDocumento = 20L * mb;

        switch (tipo) {
            case "imagen" -> {
                if (sizeBytes > maxImagen) {
                    throw new RuntimeException("La imagen no puede superar los 10 MB");
                }
            }
            case "video" -> {
                if (sizeBytes > maxVideo) {
                    throw new RuntimeException("El video no puede superar los 100 MB");
                }
            }
            case "documento" -> {
                if (sizeBytes > maxDocumento) {
                    throw new RuntimeException("El documento no puede superar los 20 MB");
                }
            }
            default -> throw new RuntimeException("Tipo de recurso no válido");
        }
    }

    private String limpiarNombreArchivoParaGuardar(String nombreArchivo) {
        return nombreArchivo
                .trim()
                .replaceAll("[^a-zA-Z0-9\\.\\-_]", "_")
                .replaceAll("_+", "_");
    }

    private RecursoResponseDTO convertirADTO(Recurso recurso) {
        return new RecursoResponseDTO(
                recurso.getIdRecurso(),
                recurso.getCampana().getIdCampana(),
                recurso.getCampana().getNombre(),
                recurso.getTipo(),
                recurso.getTitulo(),
                recurso.getNombreArchivo(),
                recurso.getUrlArchivo(),
                recurso.getContenidoTexto(),
                recurso.getPesoMb(),
                recurso.getFormato(),
                recurso.getEstado(),
                recurso.getFechaSubida()
        );
    }
}