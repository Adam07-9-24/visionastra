package com.visionastra.api.service.ia.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visionastra.api.service.ia.VideoIAService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.Base64;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleVeoVideoIAServiceImpl implements VideoIAService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.veo.base-url}")
    private String baseUrl;

    @Value("${gemini.veo.model}")
    private String veoModel;

    @Value("${gemini.veo.resolution}")
    private String resolution;

    @Value("${gemini.veo.duration}")
    private Integer duration;

    @Value("${gemini.veo.aspect-ratio}")
    private String aspectRatio;

    public GoogleVeoVideoIAServiceImpl() {
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)
                )
                .build();

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generarVideoDesdePrompt(String promptFinal) {
        if (promptFinal == null || promptFinal.isBlank()) {
            throw new RuntimeException("El prompt final es obligatorio para generar video.");
        }

        return "Configuración Veo OK -> modelo: " + veoModel
                + ", resolución: " + resolution
                + ", duración: " + duration
                + "s, aspect ratio: " + aspectRatio;
    }

    @Override
    public String iniciarGeneracionVeoLite(String promptFinal) {
        if (promptFinal == null || promptFinal.isBlank()) {
            throw new RuntimeException("El prompt final es obligatorio para generar video.");
        }

        try {
            Map<String, Object> body = crearRequestVeo(promptFinal);

            String url = baseUrl + "/models/" + veoModel + ":predictLongRunning";

            String responseJson = webClient.post()
                    .uri(url)
                    .header("x-goog-api-key", geminiApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseJson == null || responseJson.isBlank()) {
                throw new RuntimeException("Gemini/Veo no devolvió respuesta.");
            }

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode nameNode = root.get("name");

            if (nameNode == null || !nameNode.isTextual()) {
                return responseJson;
            }

            return nameNode.asText();

        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar generación con Gemini/Veo: " + e.getMessage(), e);
        }
    }

    @Override
    public String iniciarGeneracionVeoLite(String promptFinal, Path imagenReferencia) {
        if (promptFinal == null || promptFinal.isBlank()) {
            throw new RuntimeException("El prompt final es obligatorio para generar video.");
        }

        if (imagenReferencia == null) {
            return iniciarGeneracionVeoLite(promptFinal);
        }

        if (!Files.exists(imagenReferencia)) {
            throw new RuntimeException("La imagen de referencia no existe físicamente.");
        }

        try {
            Map<String, Object> body = crearRequestVeoConImagen(promptFinal, imagenReferencia);

            String url = baseUrl + "/models/" + veoModel + ":predictLongRunning";

            String responseJson = webClient.post()
                    .uri(url)
                    .header("x-goog-api-key", geminiApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseJson == null || responseJson.isBlank()) {
                throw new RuntimeException("Gemini/Veo no devolvió respuesta.");
            }

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode nameNode = root.get("name");

            if (nameNode == null || !nameNode.isTextual()) {
                return responseJson;
            }

            return nameNode.asText();

        } catch (WebClientResponseException e) {
            String cuerpoError = e.getResponseBodyAsString();

            throw new RuntimeException(
                    "Error al iniciar generación con Gemini/Veo usando imagen. " +
                            "Status: " + e.getStatusCode() +
                            ". Respuesta Google: " + cuerpoError,
                    e
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar generación con Gemini/Veo usando imagen: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> crearRequestVeoConImagen(String promptFinal, Path imagenReferencia) {
        try {
            byte[] imagenBytes = Files.readAllBytes(imagenReferencia);
            String imagenBase64 = Base64.getEncoder().encodeToString(imagenBytes);

            String mimeType = detectarMimeType(imagenReferencia);

            Map<String, Object> image = new LinkedHashMap<>();
            image.put("bytesBase64Encoded", imagenBase64);
            image.put("mimeType", mimeType);

            Map<String, Object> referenceImage = new LinkedHashMap<>();
            referenceImage.put("image", image);
            referenceImage.put("referenceType", "asset");

            Map<String, Object> instance = new LinkedHashMap<>();
            instance.put("prompt", promptFinal.trim());
            instance.put("referenceImages", List.of(referenceImage));

            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("aspectRatio", aspectRatio);
            parameters.put("durationSeconds", duration);
            parameters.put("resolution", resolution);
            parameters.put("personGeneration", "allow_all");

            Map<String, Object> request = new LinkedHashMap<>();
            request.put("instances", List.of(instance));
            request.put("parameters", parameters);

            return request;

        } catch (Exception e) {
            throw new RuntimeException("No se pudo preparar la imagen de referencia para Veo: " + e.getMessage(), e);
        }
    }

    private String detectarMimeType(Path imagenReferencia) {
        try {
            String mimeType = Files.probeContentType(imagenReferencia);

            if (mimeType != null && mimeType.startsWith("image/")) {
                return mimeType;
            }

            String nombre = imagenReferencia.getFileName().toString().toLowerCase();

            if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
                return "image/jpeg";
            }

            if (nombre.endsWith(".png")) {
                return "image/png";
            }

            if (nombre.endsWith(".webp")) {
                return "image/webp";
            }

            throw new RuntimeException("Formato de imagen no soportado. Usa PNG, JPG, JPEG o WEBP.");

        } catch (Exception e) {
            throw new RuntimeException("No se pudo detectar el tipo de imagen: " + e.getMessage(), e);
        }
    }

    @Override
    public String generarYDescargarVideo(String promptFinal, Path imagenReferencia) {
        if (promptFinal == null || promptFinal.isBlank()) {
            throw new RuntimeException("El prompt final es obligatorio para generar video.");
        }

        if (imagenReferencia == null) {
            return generarYDescargarVideo(promptFinal);
        }

        try {
            String operationName = iniciarGeneracionVeoLite(promptFinal, imagenReferencia);

            int maxIntentos = 30;
            int segundosEspera = 10;

            for (int intento = 1; intento <= maxIntentos; intento++) {
                Thread.sleep(segundosEspera * 1000L);

                String respuestaOperacion = consultarOperacionVeo(operationName);

                JsonNode root = objectMapper.readTree(respuestaOperacion);
                boolean terminado = root.has("done") && root.get("done").asBoolean();

                if (!terminado) {
                    continue;
                }

                String videoUri = extraerVideoUri(root);

                if (videoUri == null || videoUri.isBlank()) {
                    throw new RuntimeException("La operación terminó, pero no se encontró la URL del video.");
                }

                return descargarVideoVeo(videoUri);
            }

            throw new RuntimeException("El video aún no terminó de generarse después del tiempo máximo de espera.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("La generación de video fue interrumpida.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar video con imagen de referencia en Gemini/Veo: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> crearRequestVeo(String promptFinal) {

        Map<String, Object> instance = new LinkedHashMap<>();
        instance.put("prompt", promptFinal.trim());

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("aspectRatio", aspectRatio);
        parameters.put("durationSeconds", duration);
        parameters.put("resolution", resolution);
        parameters.put("personGeneration", "allow_all");

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("instances", List.of(instance));
        request.put("parameters", parameters);

        return request;
    }



    @Override
    public String consultarOperacionVeo(String operationName) {
        if (operationName == null || operationName.isBlank()) {
            throw new RuntimeException("El nombre de la operación es obligatorio.");
        }

        try {
            String operacionLimpia = operationName.trim();

            String url = baseUrl + "/" + operacionLimpia;

            String responseJson = webClient.get()
                    .uri(url)
                    .header("x-goog-api-key", geminiApiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseJson == null || responseJson.isBlank()) {
                throw new RuntimeException("Gemini/Veo no devolvió respuesta al consultar la operación.");
            }

            return responseJson;

        } catch (Exception e) {
            throw new RuntimeException("Error al consultar operación de Gemini/Veo: " + e.getMessage(), e);
        }
    }

    @Override
    public String descargarVideoVeo(String videoUri) {
        if (videoUri == null || videoUri.isBlank()) {
            throw new RuntimeException("La URL del video es obligatoria.");
        }

        try {
            String uriLimpia = videoUri.trim();

            String uriConKey = uriLimpia.contains("?")
                    ? uriLimpia + "&key=" + geminiApiKey
                    : uriLimpia + "?key=" + geminiApiKey;

            byte[] videoBytes = webClient.get()
                    .uri(uriConKey)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (videoBytes == null || videoBytes.length == 0) {
                throw new RuntimeException("No se pudo descargar el video generado.");
            }

            if (videoBytes.length < 1000) {
                String posibleError = new String(videoBytes, java.nio.charset.StandardCharsets.UTF_8);
                throw new RuntimeException("La descarga no parece ser un video. Respuesta recibida: " + posibleError);
            }

            java.nio.file.Path carpeta = java.nio.file.Paths.get("uploads", "videos-prueba");
            java.nio.file.Files.createDirectories(carpeta);

            String nombreArchivo = "veo-prueba-" + System.currentTimeMillis() + ".mp4";
            java.nio.file.Path rutaArchivo = carpeta.resolve(nombreArchivo);

            java.nio.file.Files.write(rutaArchivo, videoBytes);

            return rutaArchivo.toAbsolutePath().toString();

        } catch (Exception e) {
            throw new RuntimeException("Error al descargar video de Gemini/Veo: " + e.getMessage(), e);
        }
    }

    @Override
    public String generarYDescargarVideo(String promptFinal) {
        if (promptFinal == null || promptFinal.isBlank()) {
            throw new RuntimeException("El prompt final es obligatorio para generar video.");
        }

        try {
            String operationName = iniciarGeneracionVeoLite(promptFinal);

            int maxIntentos = 30;
            int segundosEspera = 10;

            for (int intento = 1; intento <= maxIntentos; intento++) {
                Thread.sleep(segundosEspera * 1000L);

                String respuestaOperacion = consultarOperacionVeo(operationName);

                JsonNode root = objectMapper.readTree(respuestaOperacion);
                boolean terminado = root.has("done") && root.get("done").asBoolean();

                if (!terminado) {
                    continue;
                }

                String videoUri = extraerVideoUri(root);

                if (videoUri == null || videoUri.isBlank()) {
                    throw new RuntimeException("La operación terminó, pero no se encontró la URL del video.");
                }

                return descargarVideoVeo(videoUri);
            }

            throw new RuntimeException("El video aún no terminó de generarse después del tiempo máximo de espera.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("La generación de video fue interrumpida.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar y descargar video con Gemini/Veo: " + e.getMessage(), e);
        }
    }

    private String extraerVideoUri(JsonNode root) {
        JsonNode generatedSamples = root
                .path("response")
                .path("generateVideoResponse")
                .path("generatedSamples");

        if (generatedSamples.isArray() && !generatedSamples.isEmpty()) {
            JsonNode uriNode = generatedSamples
                    .get(0)
                    .path("video")
                    .path("uri");

            if (uriNode.isTextual()) {
                return uriNode.asText();
            }
        }

        return null;
    }
}