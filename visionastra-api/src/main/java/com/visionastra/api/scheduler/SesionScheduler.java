package com.visionastra.api.scheduler;

import com.visionastra.api.service.SesionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SesionScheduler {

    private final SesionService sesionService;

    public SesionScheduler(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    // Se ejecuta cada 1 minuto
    @Scheduled(fixedRate = 60000)
    public void ejecutarExpiracionSesiones() {
        System.out.println("⏳ Verificando sesiones vencidas...");
        sesionService.expirarSesionesVencidas();
    }
}