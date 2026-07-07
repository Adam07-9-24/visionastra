package com.visionastra.api.service.ia;

import java.nio.file.Path;

public interface VideoIAService {

    String generarVideoDesdePrompt(String promptFinal);

    String iniciarGeneracionVeoLite(String promptFinal);

    String iniciarGeneracionVeoLite(String promptFinal, Path imagenReferencia);

    String consultarOperacionVeo(String operationName);

    String descargarVideoVeo(String videoUri);

    String generarYDescargarVideo(String promptFinal);

    String generarYDescargarVideo(String promptFinal, Path imagenReferencia);
}