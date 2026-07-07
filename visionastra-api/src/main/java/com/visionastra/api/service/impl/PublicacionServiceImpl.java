package com.visionastra.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visionastra.api.dto.N8nPublicacionResponseDTO;
import com.visionastra.api.dto.PublicacionRequestDTO;
import com.visionastra.api.dto.PublicacionResponseDTO;
import com.visionastra.api.model.Campana;
import com.visionastra.api.model.Publicacion;
import com.visionastra.api.model.Recurso;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.CampanaRepository;
import com.visionastra.api.repository.PublicacionRepository;
import com.visionastra.api.repository.RecursoRepository;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.PublicacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import org.springframework.web.util.UriComponentsBuilder;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PublicacionServiceImpl implements PublicacionService {

    private static final Set<String> PLATAFORMAS_VALIDAS = Set.of(
            "facebook", "instagram", "tiktok", "linkedin", "x", "youtube"
    );

    private static final Set<String> PRIVACIDADES_VALIDAS = Set.of(
            "private", "unlisted", "public"
    );

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "borrador", "lista", "programada", "enviada", "publicada", "error", "cancelada"
    );

    private static final Set<String> TIPOS_RECURSO_PERMITIDOS = Set.of(
            "imagen", "video", "copy"
    );

    private final PublicacionRepository publicacionRepository;
    private final CampanaRepository campanaRepository;
    private final RecursoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${n8n.webhook.publicaciones.url:}")
    private String n8nWebhookPublicacionesUrl;

    @Value("${visionastra.uploads.path}")
    private String uploadsPath;

    public PublicacionServiceImpl(
            PublicacionRepository publicacionRepository,
            CampanaRepository campanaRepository,
            RecursoRepository recursoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.publicacionRepository = publicacionRepository;
        this.campanaRepository = campanaRepository;
        this.recursoRepository = recursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> listar(String email, Integer idCampana, String estado, String plataforma) {
        String estadoNormalizado = normalizarOpcional(estado);
        String plataformaNormalizada = normalizarOpcional(plataforma);

        if (estadoNormalizado != null && !ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            throw new RuntimeException("Estado de publicación no válido.");
        }

        if (plataformaNormalizada != null && !PLATAFORMAS_VALIDAS.contains(plataformaNormalizada)) {
            throw new RuntimeException("Plataforma no válida.");
        }

        return publicacionRepository
                .buscarPorFiltros(email, idCampana, estadoNormalizado, plataformaNormalizada)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicacionResponseDTO obtenerPorId(Integer idPublicacion, String email) {
        Publicacion publicacion = obtenerPublicacionDelUsuario(idPublicacion, email);
        return toResponse(publicacion);
    }

    @Override
    @Transactional
    public PublicacionResponseDTO crear(PublicacionRequestDTO request, String email) {
        Usuario usuario = obtenerUsuario(email);
        Campana campana = obtenerCampanaDelUsuario(request.getIdCampana(), usuario);

        validarCampanaParaPublicacion(campana);

        Recurso recurso = null;
        if (request.getIdRecurso() != null) {
            recurso = obtenerRecursoValido(request.getIdRecurso(), campana);
        }

        Publicacion publicacion = new Publicacion();
        publicacion.setCampana(campana);
        publicacion.setRecurso(recurso);

        aplicarDatosEditables(publicacion, request);

        Publicacion guardada = publicacionRepository.save(publicacion);
        return toResponse(guardada);
    }

    @Override
    @Transactional
    public PublicacionResponseDTO actualizar(Integer idPublicacion, PublicacionRequestDTO request, String email) {
        Usuario usuario = obtenerUsuario(email);
        Publicacion publicacion = obtenerPublicacionDelUsuario(idPublicacion, email);

        Campana campana = publicacion.getCampana();

        if (request.getIdCampana() != null && !request.getIdCampana().equals(campana.getIdCampana())) {
            campana = obtenerCampanaDelUsuario(request.getIdCampana(), usuario);
            validarCampanaParaPublicacion(campana);
            publicacion.setCampana(campana);
        } else {
            validarCampanaParaPublicacion(campana);
        }

        if (request.getIdRecurso() != null) {
            Recurso recurso = obtenerRecursoValido(request.getIdRecurso(), campana);
            publicacion.setRecurso(recurso);
        } else {
            publicacion.setRecurso(null);
        }

        aplicarDatosEditables(publicacion, request);

        Publicacion actualizada = publicacionRepository.save(publicacion);
        return toResponse(actualizada);
    }

    @Override
    @Transactional
    public PublicacionResponseDTO cancelar(Integer idPublicacion, String email) {
        Publicacion publicacion = obtenerPublicacionDelUsuario(idPublicacion, email);

        String estadoActual = publicacion.getEstado();

        if ("publicada".equalsIgnoreCase(estadoActual)) {
            throw new RuntimeException("No se puede cancelar una publicación que ya fue publicada.");
        }

        if ("enviada".equalsIgnoreCase(estadoActual)) {
            throw new RuntimeException("No se puede cancelar una publicación que ya fue enviada.");
        }

        if ("cancelada".equalsIgnoreCase(estadoActual)) {
            throw new RuntimeException("La publicación ya está cancelada.");
        }

        publicacion.setEstado("cancelada");

        Publicacion cancelada = publicacionRepository.save(publicacion);
        return toResponse(cancelada);
    }

    @Override
    @Transactional
    public PublicacionResponseDTO enviarAN8n(Integer idPublicacion, String email) {
        Publicacion publicacion = obtenerPublicacionDelUsuario(idPublicacion, email);

        validarPublicacionParaEnvioN8n(publicacion);

        Recurso recurso = publicacion.getRecurso();
        Path rutaArchivo = resolverArchivoLocal(recurso);

        try {
            N8nPublicacionResponseDTO response = enviarPublicacionAN8n(publicacion, recurso, rutaArchivo);

            if (response == null) {
                return guardarErrorN8n(publicacion, "n8n no devolvio respuesta.");
            }

            if (!Boolean.TRUE.equals(response.getSuccess())) {
                return guardarErrorN8n(publicacion, obtenerMensajeErrorN8n(response));
            }

            publicacion.setEstado("enviada");
            publicacion.setExternalId(response.getExternalId());
            publicacion.setUrlPublicacion(response.getExternalUrl());
            publicacion.setMensajeError(null);

            Publicacion guardada = publicacionRepository.save(publicacion);
            return toResponse(guardada);

        } catch (WebClientResponseException e) {
            String mensaje = "n8n respondio con status "
                    + e.getStatusCode().value()
                    + ". Respuesta: "
                    + limitarMensaje(e.getResponseBodyAsString());

            return guardarErrorN8n(publicacion, mensaje);

        } catch (Exception e) {
            return guardarErrorN8n(publicacion, "Error al enviar publicacion a n8n: " + limitarMensaje(e.getMessage()));
        }
    }

    private void aplicarDatosEditables(Publicacion publicacion, PublicacionRequestDTO request) {
        String titulo = limpiarTexto(request.getTitulo());
        if (titulo == null) {
            throw new RuntimeException("El título de la publicación es obligatorio.");
        }

        if (titulo.length() > 200) {
            throw new RuntimeException("El título no puede superar los 200 caracteres.");
        }

        String plataforma = normalizarObligatorio(request.getPlataforma(), "La plataforma es obligatoria.");
        if (!PLATAFORMAS_VALIDAS.contains(plataforma)) {
            throw new RuntimeException("Plataforma no válida.");
        }

        String privacidad = normalizarOpcional(request.getPrivacidad());
        if (privacidad != null && !PRIVACIDADES_VALIDAS.contains(privacidad)) {
            throw new RuntimeException("Privacidad no válida.");
        }

        String estado = normalizarOpcional(request.getEstado());
        if (estado == null) {
            estado = publicacion.getEstado() != null ? publicacion.getEstado() : "borrador";
        }

        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new RuntimeException("Estado de publicación no válido.");
        }

        if ("programada".equals(estado) && request.getFechaProgramada() == null) {
            throw new RuntimeException("Para programar una publicación debes indicar la fecha programada.");
        }

        publicacion.setTitulo(titulo);
        publicacion.setCopyTexto(limpiarTexto(request.getCopyTexto()));
        publicacion.setPlataforma(plataforma);
        publicacion.setPrivacidad(privacidad);
        publicacion.setEstado(estado);
        publicacion.setFechaProgramada(request.getFechaProgramada());
    }

    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado."));
    }

    private Campana obtenerCampanaDelUsuario(Integer idCampana, Usuario usuario) {
        if (idCampana == null) {
            throw new RuntimeException("La campaña es obligatoria.");
        }

        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada."));

        if (!campana.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No tienes permiso para usar esta campaña.");
        }

        return campana;
    }

    private void validarCampanaParaPublicacion(Campana campana) {
        if ("finalizada".equalsIgnoreCase(campana.getEstado())) {
            throw new RuntimeException("No se puede crear o editar una publicación para una campaña finalizada.");
        }
    }

    private Recurso obtenerRecursoValido(Integer idRecurso, Campana campana) {
        Recurso recurso = recursoRepository.findById(idRecurso)
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado."));

        if (!recurso.getCampana().getIdCampana().equals(campana.getIdCampana())) {
            throw new RuntimeException("El recurso seleccionado no pertenece a la campaña indicada.");
        }

        if (!"activo".equalsIgnoreCase(recurso.getEstado())) {
            throw new RuntimeException("El recurso seleccionado no está activo.");
        }

        String tipo = normalizarOpcional(recurso.getTipo());
        if (tipo == null || !TIPOS_RECURSO_PERMITIDOS.contains(tipo)) {
            throw new RuntimeException("Solo puedes usar recursos de tipo imagen, video o copy.");
        }

        return recurso;
    }

    private Publicacion obtenerPublicacionDelUsuario(Integer idPublicacion, String email) {
        if (idPublicacion == null) {
            throw new RuntimeException("La publicación es obligatoria.");
        }

        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada."));

        if (!publicacion.getCampana().getUsuario().getEmail().equals(email)) {
            throw new RuntimeException("No tienes permiso para acceder a esta publicación.");
        }

        return publicacion;
    }

    private void validarPublicacionParaEnvioN8n(Publicacion publicacion) {
        if (n8nWebhookPublicacionesUrl == null || n8nWebhookPublicacionesUrl.isBlank()) {
            throw new RuntimeException("Webhook de n8n no configurado.");
        }

        String estadoCampana = normalizarOpcional(publicacion.getCampana().getEstado());

        if (!"activa".equals(estadoCampana)) {
            throw new RuntimeException("Solo se pueden enviar publicaciones de campañas activas.");
        }

        String estado = normalizarOpcional(publicacion.getEstado());

        if ("enviada".equals(estado) || "publicada".equals(estado)) {
            throw new RuntimeException("La publicacion ya fue enviada o publicada.");
        }

        if ("cancelada".equals(estado)) {
            throw new RuntimeException("No se puede enviar una publicacion cancelada.");
        }

        if (publicacion.getCopyTexto() == null || publicacion.getCopyTexto().trim().isEmpty()) {
            throw new RuntimeException("La descripción de la publicación es obligatoria para enviar a n8n.");
        }

        Recurso recurso = publicacion.getRecurso();

        if (recurso == null) {
            throw new RuntimeException("La publicacion no tiene recurso asociado.");
        }

        if (!"activo".equalsIgnoreCase(recurso.getEstado())) {
            throw new RuntimeException("El recurso asociado no esta activo.");
        }

        if (!"video".equalsIgnoreCase(recurso.getTipo())) {
            throw new RuntimeException("El recurso asociado no es de tipo video.");
        }

        if (!esArchivoMp4(recurso)) {
            throw new RuntimeException("El archivo asociado debe ser mp4.");
        }

        validarVideoNoEnviadoMismaPlataforma(publicacion, recurso);
    }

    private void validarVideoNoEnviadoMismaPlataforma(Publicacion publicacion, Recurso recurso) {
        boolean existeDuplicado = publicacionRepository
                .existsByRecurso_IdRecursoAndPlataformaAndEstadoInAndIdPublicacionNotAndCampana_Usuario_IdUsuario(
                        recurso.getIdRecurso(),
                        publicacion.getPlataforma(),
                        List.of("enviada", "publicada"),
                        publicacion.getIdPublicacion(),
                        publicacion.getCampana().getUsuario().getIdUsuario()
                );

        if (existeDuplicado) {
            throw new RuntimeException("Este video ya fue enviado a esta plataforma.");
        }
    }

    private boolean esArchivoMp4(Recurso recurso) {
        String formato = normalizarOpcional(recurso.getFormato());
        String urlArchivo = normalizarOpcional(recurso.getUrlArchivo());

        return "mp4".equals(formato) || (urlArchivo != null && urlArchivo.endsWith(".mp4"));
    }

    private Path resolverArchivoLocal(Recurso recurso) {
        if (recurso.getUrlArchivo() == null || recurso.getUrlArchivo().isBlank()) {
            throw new RuntimeException("El recurso no tiene archivo asociado.");
        }

        String rutaRelativa = recurso.getUrlArchivo().trim().replace("\\", "/");

        if (rutaRelativa.startsWith("/uploads/")) {
            rutaRelativa = rutaRelativa.substring("/uploads/".length());
        }

        if (rutaRelativa.startsWith("uploads/")) {
            rutaRelativa = rutaRelativa.substring("uploads/".length());
        }

        Path carpetaBase = Paths.get(uploadsPath)
                .toAbsolutePath()
                .normalize();

        Path rutaArchivo = carpetaBase
                .resolve(rutaRelativa)
                .normalize();

        if (!rutaArchivo.startsWith(carpetaBase)) {
            throw new RuntimeException("Ruta de archivo no permitida.");
        }

        if (!Files.exists(rutaArchivo)) {
            throw new RuntimeException("Archivo no encontrado en el servidor.");
        }

        if (!Files.isRegularFile(rutaArchivo)) {
            throw new RuntimeException("La ruta asociada no es un archivo valido.");
        }

        return rutaArchivo;
    }

    private N8nPublicacionResponseDTO enviarPublicacionAN8n(
            Publicacion publicacion,
            Recurso recurso,
            Path rutaArchivo
    ) {
        String filename = rutaArchivo.getFileName().toString();
        FileSystemResource archivoResource = new FileSystemResource(rutaArchivo);

        HttpHeaders videoHeaders = new HttpHeaders();
        videoHeaders.setContentDisposition(ContentDisposition.formData()
                .name("video")
                .filename(filename)
                .build());
        videoHeaders.setContentType(MediaType.parseMediaType("video/mp4"));

        HttpEntity<FileSystemResource> videoPart = new HttpEntity<>(archivoResource, videoHeaders);

        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("video", videoPart);

        String privacidad = publicacion.getPrivacidad() != null
                ? publicacion.getPrivacidad()
                : "private";

        String webhookUrlConMetadata = UriComponentsBuilder
                .fromUriString(n8nWebhookPublicacionesUrl.trim())
                .queryParam("publicacionId", publicacion.getIdPublicacion())
                .queryParam("titulo", publicacion.getTitulo())
                .queryParam("descripcion", publicacion.getCopyTexto().trim())
                .queryParam("plataforma", publicacion.getPlataforma())
                .queryParam("privacidad", privacidad)
                .queryParam("campanaId", publicacion.getCampana().getIdCampana())
                .queryParam("recursoId", recurso.getIdRecurso())
                .build()
                .encode()
                .toUriString();

        return webClient.post()
                .uri(webhookUrlConMetadata)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBody))
                .exchangeToMono(response -> {
                    int statusCode = response.statusCode().value();

                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                if (response.statusCode().isError()) {
                                    return Mono.error(new RuntimeException(
                                            "n8n respondio con status " + statusCode + ". Body: " + limitarMensaje(body)
                                    ));
                                }

                                if (body.isBlank()) {
                                    return Mono.error(new RuntimeException(
                                            "n8n respondio con body vacio. Status: " + statusCode
                                    ));
                                }

                                try {
                                    return Mono.just(objectMapper.readValue(body, N8nPublicacionResponseDTO.class));
                                } catch (Exception e) {
                                    return Mono.error(new RuntimeException(
                                            "No se pudo parsear respuesta de n8n. Status: "
                                                    + statusCode
                                                    + ". Body: "
                                                    + limitarMensaje(body),
                                            e
                                    ));
                                }
                            });
                })
                .block(Duration.ofSeconds(120));
    }

    private PublicacionResponseDTO guardarErrorN8n(Publicacion publicacion, String mensaje) {
        publicacion.setEstado("error");
        publicacion.setMensajeError(limitarMensaje(mensaje));

        Publicacion guardada = publicacionRepository.save(publicacion);
        return toResponse(guardada);
    }

    private String obtenerMensajeErrorN8n(N8nPublicacionResponseDTO response) {
        String error = limpiarTexto(response.getError());
        if (error != null) {
            return error;
        }

        String mensaje = limpiarTexto(response.getMensaje());
        if (mensaje != null) {
            return mensaje;
        }

        return "n8n no confirmo el envio de la publicacion.";
    }

    private String limitarMensaje(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return "Error desconocido al enviar publicacion a n8n.";
        }

        String limpio = mensaje.trim();
        int maxLength = 1000;

        if (limpio.length() <= maxLength) {
            return limpio;
        }

        return limpio.substring(0, maxLength);
    }

    private PublicacionResponseDTO toResponse(Publicacion publicacion) {
        PublicacionResponseDTO response = new PublicacionResponseDTO();

        response.setIdPublicacion(publicacion.getIdPublicacion());

        Campana campana = publicacion.getCampana();
        response.setIdCampana(campana.getIdCampana());
        response.setNombreCampana(campana.getNombre());

        Recurso recurso = publicacion.getRecurso();
        if (recurso != null) {
            response.setIdRecurso(recurso.getIdRecurso());
            response.setTituloRecurso(recurso.getTitulo());
            response.setTipoRecurso(recurso.getTipo());
        }

        response.setTitulo(publicacion.getTitulo());
        response.setCopyTexto(publicacion.getCopyTexto());
        response.setPlataforma(publicacion.getPlataforma());
        response.setPrivacidad(publicacion.getPrivacidad());
        response.setEstado(publicacion.getEstado());
        response.setFechaProgramada(publicacion.getFechaProgramada());
        response.setFechaPublicada(publicacion.getFechaPublicada());
        response.setUrlPublicacion(publicacion.getUrlPublicacion());
        response.setExternalId(publicacion.getExternalId());
        response.setMensajeError(publicacion.getMensajeError());
        response.setFechaCreacion(publicacion.getFechaCreacion());
        response.setFechaActualizacion(publicacion.getFechaActualizacion());

        return response;
    }

    private String limpiarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private String normalizarOpcional(String valor) {
        String limpio = limpiarTexto(valor);
        return limpio == null ? null : limpio.toLowerCase(Locale.ROOT);
    }

    private String normalizarObligatorio(String valor, String mensajeError) {
        String normalizado = normalizarOpcional(valor);
        if (normalizado == null) {
            throw new RuntimeException(mensajeError);
        }
        return normalizado;
    }
}
