package com.visionastra.api.service.ia;

import com.visionastra.api.dto.PromptIAResultadoDTO;

public interface PromptIAService {

    PromptIAResultadoDTO prepararPromptProfesional(String contexto);
}