package com.visionastra.api.controller;

import com.visionastra.api.service.ia.VideoIAService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video-ia")
public class VideoIAController {

    private final VideoIAService videoIAService;

    public VideoIAController(VideoIAService videoIAService) {
        this.videoIAService = videoIAService;
    }

    @PostMapping("/probar-configuracion")
    public ResponseEntity<String> probarConfiguracion(@RequestBody String prompt) {
        String resultado = videoIAService.generarVideoDesdePrompt(prompt);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/probar-veo-lite")
    public ResponseEntity<String> probarVeoLite(@RequestBody String prompt) {
        String resultado = videoIAService.iniciarGeneracionVeoLite(prompt);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/consultar-operacion")
    public ResponseEntity<String> consultarOperacion(@RequestBody String operationName) {
        String resultado = videoIAService.consultarOperacionVeo(operationName);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/descargar-video")
    public ResponseEntity<String> descargarVideo(@RequestBody String videoUri) {
        String resultado = videoIAService.descargarVideoVeo(videoUri);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/generar-y-descargar")
    public ResponseEntity<String> generarYDescargar(@RequestBody String prompt) {
        String resultado = videoIAService.generarYDescargarVideo(prompt);
        return ResponseEntity.ok(resultado);
    }
}