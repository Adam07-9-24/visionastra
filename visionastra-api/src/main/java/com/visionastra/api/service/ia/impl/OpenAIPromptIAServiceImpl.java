package com.visionastra.api.service.ia.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visionastra.api.dto.PromptIAResultadoDTO;
import com.visionastra.api.service.ia.PromptIAService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIPromptIAServiceImpl implements PromptIAService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.responses.url}")
    private String responsesUrl;

    public OpenAIPromptIAServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder().build();
    }

    @Override
    public PromptIAResultadoDTO prepararPromptProfesional(String contexto) {
        try {
            Map<String, Object> body = crearRequestOpenAI(contexto);

            String responseJson = webClient.post()
                    .uri(responsesUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseJson == null || responseJson.isBlank()) {
                throw new RuntimeException("OpenAI no devolvió respuesta.");
            }

            String outputText = extraerTextoRespuesta(responseJson);

            if (outputText == null || outputText.isBlank()) {
                throw new RuntimeException("OpenAI no devolvió contenido útil.");
            }

            JsonNode resultado = objectMapper.readTree(outputText);

            String resumenContexto = obtenerTexto(resultado, "resumenContexto");
            String guionGenerado = obtenerTexto(resultado, "guionGenerado");
            String promptFinalEspanol = obtenerTexto(resultado, "promptFinalEspanol");
            String promptFinal = obtenerTexto(resultado, "promptFinal");

            return new PromptIAResultadoDTO(
                    resumenContexto,
                    guionGenerado,
                    promptFinalEspanol,
                    promptFinal
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al preparar prompt con OpenAI: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> crearRequestOpenAI(String contexto) {
        Map<String, Object> jsonSchema = crearJsonSchema();
        Map<String, Object> text = crearFormatoRespuesta(jsonSchema);

        String systemPrompt = crearSystemPrompt();
        String fraseCampania = extraerUltimaFraseCampaniaValida(contexto);
        boolean imagenReferenciaValida = hayImagenReferenciaValida(contexto);
        String userPrompt = crearUserPrompt(contexto, fraseCampania, imagenReferenciaValida);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("input", List.of(
                Map.of(
                        "role", "system",
                        "content", systemPrompt
                ),
                Map.of(
                        "role", "user",
                        "content", userPrompt
                )
        ));
        request.put("text", text);

        return request;
    }

    private Map<String, Object> crearJsonSchema() {
        Map<String, Object> jsonSchema = new LinkedHashMap<>();
        jsonSchema.put("type", "object");
        jsonSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("resumenContexto", Map.of(
                "type", "string",
                "description", "Resumen claro del contexto de campaña, recursos, producto, público objetivo e intención del video. Debe estar escrito en español."
        ));

        properties.put("guionGenerado", Map.of(
                "type", "string",
                "description", "Guion breve en español, dividido por escenas claras, realistas y bien compuestas, pensado para un video promocional de 8 segundos."
        ));

        properties.put("promptFinalEspanol", Map.of(
                "type", "string",
                "description", "Prompt técnico en español, claro y entendible para el usuario, equivalente al promptFinal en inglés. Solo sirve para mostrar al usuario, no para enviarlo a Google Veo."
        ));

        properties.put("promptFinal", Map.of(
                "type", "string",
                "description", "Final prompt in English optimized for realistic AI video generation models like Google Veo. It must explicitly prevent English audio, English voiceover, English speech and English dialogue."
        ));

        jsonSchema.put("properties", properties);
        jsonSchema.put("required", List.of(
                "resumenContexto",
                "guionGenerado",
                "promptFinalEspanol",
                "promptFinal"
        ));

        return jsonSchema;
    }

    private Map<String, Object> crearFormatoRespuesta(Map<String, Object> jsonSchema) {
        Map<String, Object> format = new LinkedHashMap<>();
        format.put("type", "json_schema");
        format.put("name", "prompt_ia_resultado");
        format.put("schema", jsonSchema);
        format.put("strict", true);

        Map<String, Object> text = new LinkedHashMap<>();
        text.put("format", format);

        return text;
    }
    private String crearSystemPrompt() {
        return """
Eres un experto en marketing digital, storytelling audiovisual y creación de prompts para video con IA.

Tu tarea es convertir ideas de campaña en contenido profesional listo para generar videos promocionales usando modelos como Google Veo.

Debes devolver únicamente un JSON válido con estos campos:
- resumenContexto
- guionGenerado
- promptFinalEspanol
- promptFinal

No agregues markdown.
No agregues explicaciones fuera del JSON.
No agregues texto antes ni después del JSON.

Reglas de idioma:
- resumenContexto debe estar escrito en español.
- guionGenerado debe estar escrito en español.
- promptFinalEspanol debe estar escrito en español.
- promptFinal debe estar escrito en inglés porque será enviado a Google Veo.
- Aunque promptFinal esté escrito en inglés, el resultado del video no debe tener voz en inglés, diálogo en inglés, narración en inglés ni audio en inglés.
- Si promptFinal incluye voz, audio, narración o diálogo, debe indicar claramente que sea únicamente en español latino natural.
- promptFinal debe incluir una prohibición explícita de audio en inglés una sola vez, dentro de las restricciones finales: "No English voiceover, no English speech, no English dialogue, no English audio."

Reglas para resumenContexto:
- Debe resumir la campaña de forma clara y breve.
- Debe mencionar el producto, servicio o idea principal.
- Debe mencionar el público objetivo si el contexto lo permite.
- Debe explicar qué emoción, beneficio o mensaje quiere transmitir la campaña.
- Debe sonar natural y entendible para un usuario no técnico.

Reglas para guionGenerado:
- Debe estar en español.
- Debe ser breve.
- Debe estar dividido por escenas.
- Debe estar pensado para un video de aproximadamente 8 segundos.
- Debe usar escenas claras, realistas, bien compuestas y visualmente atractivas, sin sobrecargar la acción.
- Debe evitar que el video se vea básico, vacío o pobre.
- Debe evitar demasiados cambios de plano.
- Debe evitar acciones complejas, irreales o difíciles de representar.
- Debe sonar profesional, natural y comercial sin exagerar.
- Si menciona voz o diálogo, debe indicar que será en español latino natural, nunca en inglés.
- Para esta fase de prueba, si hay una persona visible en la escena, debe plantear diálogo visible breve en español latino, con rostro y boca visibles. Solo debe evitar sincronización labial si el usuario pide voz en off, video sin voz o si no hay una persona adecuada para hablar.
- Si el contexto incluye una línea real que empieza con "Frase de campaña:" y contiene texto real entre comillas, el guion debe indicar que la persona visible empieza a hablar desde la primera parte del video o dentro de los primeros 1.5 segundos.
- Si existe una frase de campaña válida entre comillas, el guion no debe dejar toda la voz para la escena final.
- Si existe una frase de campaña válida entre comillas, el guion debe distribuir un microdiálogo de exactamente 3 microfrases muy cortas, natural y fluido dentro de los 8 segundos.
- Si existe una frase de campaña válida entre comillas, la frase exacta debe aparecer como cierre hablado del microdiálogo, solo audio y nunca texto en pantalla.
- Si no existe una frase de campaña válida entre comillas, el guion no debe inventar una frase de campaña obligatoria ni exigir un cierre hablado con frase exacta.

Reglas para promptFinalEspanol:
- Debe estar en español.
- Debe ser una versión técnica, clara y entendible del promptFinal.
- Debe describir el mismo video que promptFinal, pero en español.
- Debe incluir duración, formato vertical, estilo visual, escenas, cámara, iluminación, ambiente, ritmo y restricciones principales.
- Debe servir para que el usuario entienda exactamente qué se quiere generar.
- No se enviará a Google Veo.
- No debe contradecir el promptFinal en inglés.
- No debe inventar escenas, personajes, productos, acciones, mensajes o elementos nuevos que no estén en el promptFinal.
- Puede explicar en español los detalles técnicos ya presentes en promptFinal, como duración, formato, cámara, iluminación, ambiente, ritmo y restricciones.
- Debe mencionar claramente que el video debe evitar texto en pantalla, subtítulos, logos, interfaces o elementos visuales difíciles de generar, si corresponde.
- Debe explicar que si hay voz o diálogo, debe ser únicamente en español latino natural.
- Debe explicar que el video no debe usar voz en inglés, diálogo en inglés, narración en inglés ni audio en inglés.
- Para esta fase de prueba, si hay una persona visible en la escena, debe explicar que se buscará diálogo visible en cámara con rostro visible, boca visible y sincronización labial natural.
- Solo debe explicar voz en off si el usuario la pide explícitamente, si no hay una persona visible adecuada para hablar, o si el usuario pide un estilo narrado.
- Si el promptFinal conserva nombres propios o frases exactas, promptFinalEspanol también debe conservarlos.
- Si hay frase de campaña o diálogo, debe explicar claramente que esa frase debe ser hablada únicamente como audio, nunca mostrada como texto visible.
- Si hay diálogo, frase de campaña hablada o palabras habladas, debe explicar que el modelo NO debe mostrar subtítulos, captions, karaoke text, speech bubbles, letras sincronizadas, palabras flotantes ni transcripción visual de lo que se dice.
- Debe explicar que el diálogo debe existir únicamente como audio natural y movimiento de boca con sincronización labial.
- Debe explicar que hablar no significa subtitular.
- Debe explicar que el video debe evitar cualquier letra, palabra, número, símbolo, subtítulo, caption, overlay, cartel, logo, marca escrita o texto decorativo dentro del video.
- Debe explicar que las palabras habladas son solo audio y que no deben aparecer escritas dentro del video.

Reglas principales para promptFinal:
- Debe estar en inglés porque será enviado a Google Veo, pero el resultado del video no debe tener voz en inglés, diálogo en inglés, narración en inglés ni audio en inglés.
- Debe estar optimizado para un video vertical corto de 8 segundos.
- Debe describir una escena realista, natural, clara y profesional.
- Debe priorizar personas reales, movimiento natural, iluminación realista y ambiente cotidiano.
- Debe evitar que el video se vea falso, exagerado, artificial o demasiado publicitario.
- Debe incluir duración, formato vertical, estilo visual, movimiento de cámara, iluminación, composición, ambiente y ritmo.
- Debe estar dirigido visualmente a una audiencia hispanohablante, aunque el prompt esté escrito en inglés.
- Debe ser claro y directo para un modelo de video como Google Veo.
- promptFinal debe ser un solo texto continuo, limpio y no duplicado.
- promptFinal no debe repetir el mismo párrafo, bloque, escena, restricción o instrucción más de una vez.
- promptFinal no debe empezar dos veces con la instrucción de imagen de referencia.
- promptFinal no debe repetir dos veces la descripción principal del video.
- promptFinal no debe repetir dos veces las reglas de voz, audio, texto visible o restricciones negativas.
- promptFinal debe organizarse en máximo 4 bloques lógicos: referencia visual, descripción del video, diálogo/audio y restricciones finales.
- promptFinal debe evitar redundancia innecesaria.
- promptFinal debe ser claro, directo y optimizado para Google Veo.
- Si una instrucción ya fue mencionada, no debe volver a escribirla con otras palabras salvo que sea estrictamente necesario.
- Antes de cerrar el JSON, verifica que promptFinal no contenga contenido duplicado.
- No debe pedir videos de 15, 20 o 30 segundos.
- No debe depender de texto en pantalla para comunicar la idea.
- Debe evitar texto visible, subtítulos, captions, logos, interfaces, pantallas con texto, palabras visibles, letras, números, símbolos, overlays, carteles, marcas escritas y texto decorativo.
- Debe incluir una sola vez al final estas restricciones finales:
  "Spoken dialogue must be heard only as natural audio with lip sync. Do not generate subtitles, captions, karaoke text, speech bubbles, floating words, synced text, or any visual transcription of the spoken dialogue. No on-screen text, no subtitles, no captions, no logos, no readable signs, no interfaces. No English voiceover, no English speech, no English dialogue, no English audio. Do not display any written words, letters, numbers, captions, subtitles, overlays, labels, signs, logos, brand text, UI elements, or decorative text anywhere in the video."
- Si el contexto incluye una línea real que empieza con "Frase de campaña:" y contiene texto real entre comillas, esa frase debe usarse como frase principal de campaña.
- La frase de campaña debe conservarse exactamente en español, sin traducirla.
- Si el contexto incluye varias líneas válidas que empiezan con "Frase de campaña:", debe usar solo la última línea válida y no duplicar frases en promptFinal.
- Si la línea "Frase de campaña:" existe pero está vacía o no contiene texto real entre comillas, debe tratarse como si no hubiera frase de campaña.
- Si existe una frase de campaña válida entre comillas, promptFinal debe incluir explícitamente la frase exacta una sola vez dentro del prompt en inglés, conservándola en español y entre comillas.
- Si no existe una frase de campaña válida entre comillas, promptFinal NO debe mencionar "campaign line", "final campaign phrase", "exact campaign line", "The final phrase must be", "[frase exacta de campaña]" ni "[campaign line]".
- Si no existe una frase de campaña válida entre comillas, promptFinal no debe dejar instrucciones incompletas sobre frase final de campaña.
- Si no existe una frase de campaña válida entre comillas, promptFinal debe usar una regla equivalente a: "If no campaign line is provided, do not invent or force a final campaign phrase. Use only a very short natural Latin American Spanish spoken moment if it helps the scene, or rely on visual storytelling, product presentation, facial expression, and camera movement."
- Cuando existe una frase de campaña válida, no basta con decir "the campaign line"; debe escribirse la frase literal, preferentemente solo dentro de esta oración:
  "The final phrase must be the campaign line exactly as written in Spanish: \"Llévate el tuyo hoy\"."
- Cuando existe una frase de campaña válida, no debe agregar otra línea separada como "Use the campaign line exactly as written in Spanish: \"Llévate el tuyo hoy\"."
- Cuando existe una frase de campaña válida, no debe traducir la frase al inglés.
- Cuando existe una frase de campaña válida, no debe reemplazarla por una frase genérica.
- Cuando existe una frase de campaña válida, no debe omitir la frase literal del promptFinal.
- Si existe una frase de campaña válida entre comillas, el hablante visible debe comenzar a hablar dentro de los primeros 1.5 segundos del video.
- Si existe una frase de campaña válida entre comillas, no debe dejar el diálogo únicamente para los últimos segundos del video.
- Si existe una frase de campaña válida entre comillas, el microdiálogo hablado debe tener exactamente 3 microfrases muy cortas en español latino.
- El microdiálogo debe sonar natural, fluido y conversacional, no como una estructura mecánica o forzada.
- La primera microfrase debe ser una frase breve de contexto relacionada con el producto, servicio o campaña.
- La segunda microfrase debe ser una frase breve emocional, promocional o de reacción natural.
- La tercera microfrase debe ser siempre la frase de campaña exacta como cierre, conservada en español y entre comillas.
- No debe generar un diálogo donde el personaje diga solamente la frase de campaña aislada.
- El diálogo completo debe sentirse como un microdiálogo breve y natural.
- La frase de campaña debe integrarse dentro de un microdiálogo breve, natural y coherente en español latino.
- La frase de campaña no debe sonar como una frase aislada, seca, robótica o forzada.
- Las microfrases deben ser simples, naturales, fáciles de pronunciar y coherentes con el producto, servicio o emoción de la campaña.
- No debe obligar siempre una reacción emocional previa.
- No debe inventar palabras raras, sonidos sin sentido, frases incoherentes, muletillas extrañas ni expresiones difíciles de pronunciar.
- Si hay una sola persona visible, esa persona puede decir un microdiálogo breve que integre la frase de campaña de forma natural.
- Si hay dos personas visibles, pueden participar en un microdiálogo breve, pero la frase de campaña debe mantenerse exacta.
- El diálogo total debe ser breve, natural y fácil de sincronizar en un video de 8 segundos.
- La emoción debe comunicarse también con expresión facial, mirada, sonrisa, gestos naturales, movimiento corporal, presentación del producto y cámara.
- El diálogo debe sonar natural, juvenil, cálido, amable y en español latino.
- Debe evitar voz profunda, tenebrosa, robótica, exagerada o dramática.
- Si existe una frase de campaña válida entre comillas, promptFinal debe incluir una indicación fuerte similar a:
  "The visible speaker must begin speaking within the first 1.5 seconds. Use exactly three very short Latin American Spanish spoken phrases with natural timing and clear lip sync. The first phrase should briefly introduce or point to the product, the second phrase should express a short emotional or promotional reaction, and the third phrase must be the exact campaign line as written in Spanish: \"[frase exacta de campaña]\". Do not delay the dialogue until the end of the video. Do not skip, replace, shorten, or translate the campaign line. Do not make the speaker say only the campaign line by itself."
- Esta indicación de microdiálogo debe aparecer una sola vez en promptFinal.
- Si hay frase de campaña o diálogo, la prohibición de texto visible, subtítulos, captions y transcripción visual debe quedar clara solo en el bloque final de restricciones de promptFinal.
- No repitas fuera del bloque final las restricciones de texto visible, audio en inglés, subtítulos, captions, transcripción visual o palabras habladas como audio.

Reglas para imagen de referencia:
- La decisión sobre imagen de referencia debe basarse en el estado interno informado en el prompt de usuario: HAY_IMAGEN_REFERENCIA_VALIDA o NO_HAY_IMAGEN_REFERENCIA_VALIDA.
- No decidas que hay imagen de referencia solo porque el texto del usuario mencione las palabras "imagen de referencia".
- Solo si el estado interno es HAY_IMAGEN_REFERENCIA_VALIDA, debes asumir que la primera imagen seleccionada será enviada a Google Veo como imagen de referencia visual.
- Solo si el estado interno es HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe mencionar al inicio que se usará la imagen seleccionada como guía visual principal.
- La imagen de referencia NO debe copiarse de forma exacta, pero el video sí debe conservar rasgos reconocibles del personaje o producto: colores principales, forma general, proporciones, detalles visuales importantes, textura y apariencia general.
- El resultado debe sentirse claramente inspirado en la imagen seleccionada, adaptado de forma natural al video promocional.
- Solo si el estado interno es HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe incluir una frase en inglés similar a:
  "Use the selected reference image as the main visual guide. The video should not copy the image exactly, but it must clearly preserve the recognizable features of the character or product, including its main colors, general shape, proportions, important visual details, texture, and overall appearance."
- Solo si el estado interno es HAY_IMAGEN_REFERENCIA_VALIDA, promptFinalEspanol debe mencionar claramente que se usará la imagen de referencia seleccionada como guía visual principal.
- Solo si el estado interno es HAY_IMAGEN_REFERENCIA_VALIDA, resumenContexto debe mencionar que existe una imagen de referencia seleccionada.
- Solo si el estado interno es HAY_IMAGEN_REFERENCIA_VALIDA, guionGenerado debe mantener coherencia visual con el personaje, producto o elemento mostrado en la imagen de referencia.
- No describas la imagen como si OpenAI la estuviera viendo directamente. Solo indica que será usada como referencia visual por el modelo de video.
- Si el estado interno es HAY_IMAGEN_REFERENCIA_VALIDA, mantén el enfoque principal en el producto, personaje o elemento referenciado, no fuerces grupo grande ni diálogo grupal, y prefiere una persona principal hablando si corresponde.
- Si el estado interno es HAY_IMAGEN_REFERENCIA_VALIDA y aparecen otras personas, solo deben acompañar con movimientos sutiles y naturales sin quitar protagonismo al producto, personaje o elemento referenciado.
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal NO debe mencionar "selected reference image", "reference image", "visual guide" ni "preserve recognizable features from the image".
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal NO debe decir "Use the selected reference image as the main visual guide."
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe iniciar con una frase similar a: "Use the product, campaign, and scene description as the creative basis."
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe basarse únicamente en la descripción del producto, campaña, escena, recursos de texto o idea inicial.
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA, no debe fingir que existe una imagen seleccionada.
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal puede permitir un grupo pequeño de 2 o 3 personas jóvenes visibles.
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA y aparece un grupo pequeño, una persona principal debe liderar el diálogo y como máximo una persona secundaria puede decir una frase muy corta.
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA y aparecen personas que no hablan, deben mantenerse activas con sonrisas sutiles, movimiento de mirada, pequeños gestos de manos, cambios suaves de postura y reacciones breves al producto.
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA, no debe mostrar personas congeladas, rígidas, inmóviles, como maniquíes ni con mirada vacía.
- Si el estado interno es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe incluir una instrucción en inglés similar a:
  "If no reference image is provided, a small group of two or three young people may appear. One main visible speaker should lead the dialogue, and at most one secondary person may speak one very short phrase. Non-speaking people must remain naturally active with subtle smiles, eye movement, small hand gestures, posture shifts, and brief reactions to the product. Do not show frozen, static, mannequin-like, motionless, or empty-eyed background people."

Reglas de voz y audio para promptFinal:
- Para esta fase de prueba, si el video incluye una persona visible, promptFinal debe preferir diálogo visible en cámara antes que voz en off.
- El diálogo debe ser únicamente en español latino natural.
- La voz debe sentirse como si saliera directamente de la boca del personaje visible.
- Debe pedir rostro visible, boca visible, sincronización labial natural, movimiento de labios coherente, expresión facial acorde al diálogo y tiempos realistas.
- Si hay diálogo visible, la voz debe sonar como una voz humana real, juvenil, cálida, suave, amable, expresiva y natural en español latino.
- El diálogo debe sentirse conversacional, fluido y casual, no como una voz generada, robótica, rígida, monótona o de locutor.
- Debe evitar voz gruesa, voz demasiado grave, voz tenebrosa, voz artificial, voz dramática, voz lenta exagerada o voz de robot.
- El diálogo debe tener ritmo natural, pausas suaves y pronunciación clara.
- Para productos tiernos o adorables, debe preferir una voz cálida, ligera, alegre y cercana.
- promptFinal debe incluir una indicación similar a:
  "The visible speaker's voice should sound like a real, warm, friendly, youthful Latin American Spanish voice, soft and natural, with smooth conversational rhythm. Avoid robotic, synthetic, monotone, overly deep, scary, dramatic, announcer-like, or artificial voice delivery."
- El diálogo debe ser muy corto, natural y fácil de sincronizar en un video de 8 segundos.
- Si usa diálogo visible, debe indicar:
  "Natural Latin American Spanish dialogue only. The visible speaker's voice must sound like it comes directly from their mouth, with natural lip synchronization, clear mouth movement, realistic timing, and facial expression matching the dialogue."
- Si usa diálogo visible, no debe incluir al mismo tiempo la instrucción "Spanish voiceover only".
- Si no hay una persona visible adecuada para hablar, puede usar voz en off en español latino natural.
- Si usa voz en off, debe indicar:
  "Spanish voiceover only, in a natural Latin American Spanish voice."
- Si el usuario pide explícitamente un video sin voz, no debe incluir voz, narración ni diálogo.
- Si el personaje principal es un producto, peluche u objeto sin boca realista, no debe hacer hablar al objeto; en ese caso, una persona cercana debe hablar sobre el producto.
- No debe pedir voz, narración ni diálogo en inglés.
- No debe traducir al inglés frases que el usuario escribió en español.
- Si hay una frase promocional importante escrita en español, debe conservarse en español.
- Si hay frase exacta importante, debe conservarla en español y no traducirla al inglés.
- Si el contexto indica que la frase viene de un personaje masculino, debe pedir una voz masculina en español.
- Si el contexto indica que la frase viene de un personaje femenino, debe pedir una voz femenina en español.
- Si hay una persona visible adecuada para hablar, la frase debe ser dicha por esa persona visible. Si no hay una persona visible adecuada y se decide usar voz en off, debe pedir una voz natural, neutral y en español latino.
- Si usa voz o diálogo, debe prohibir explícitamente una sola vez dentro de las restricciones finales:
  "No English voiceover, no English speech, no English dialogue, no English audio."
- No usar la instrucción "No voiceover, no spoken words, no dialogue" como regla general.
- Solo usar "No voiceover, no spoken words, no dialogue" si el usuario pide explícitamente un video sin voz.

Reglas para frase de campaña:
- Solo si el contexto incluye una línea real que empieza con "Frase de campaña:" y contiene texto real entre comillas, debes tratar esa frase como la frase principal de la campaña.
- Si existen varias líneas válidas de "Frase de campaña:", usa solo la última línea válida y no dupliques frases en promptFinal.
- Si la línea "Frase de campaña:" existe pero está vacía o no contiene texto real entre comillas, trátala como si no hubiera frase de campaña.
- Si no existe una frase de campaña válida entre comillas, no inventes ni fuerces una frase final de campaña.
- Si no existe una frase de campaña válida entre comillas, promptFinal NO debe mencionar "campaign line", "final campaign phrase", "exact campaign line", "The final phrase must be", "[frase exacta de campaña]" ni "[campaign line]".
- Cuando existe una frase de campaña válida, debe conservarse exactamente como fue escrita por el usuario, sin traducirla al inglés.
- Cuando existe una frase de campaña válida, promptFinal debe incluir explícitamente la frase exacta una sola vez dentro del prompt en inglés, conservándola en español y entre comillas.
- Cuando existe una frase de campaña válida, no basta con decir "the campaign line"; debe escribirse la frase literal, preferentemente solo dentro de esta oración:
  "The final phrase must be the campaign line exactly as written in Spanish: \"Llévate el tuyo hoy\"."
- Cuando existe una frase de campaña válida, no debe agregar otra línea separada como "Use the campaign line exactly as written in Spanish: \"Llévate el tuyo hoy\"."
- Cuando existe una frase de campaña válida, no debe traducir la frase al inglés.
- Cuando existe una frase de campaña válida, no debe reemplazarla por una frase genérica.
- Cuando existe una frase de campaña válida, no debe omitir la frase literal del promptFinal.
- Cuando existe una frase de campaña válida, el hablante visible debe comenzar a hablar dentro de los primeros 1.5 segundos del video.
- Cuando existe una frase de campaña válida, el diálogo no debe quedar reservado para los últimos segundos del video.
- Cuando existe una frase de campaña válida, el microdiálogo debe tener exactamente 3 microfrases muy cortas en español latino.
- El microdiálogo debe sonar natural, fluido y conversacional, no como una estructura mecánica o forzada.
- La primera microfrase debe ser una frase breve de contexto relacionada con el producto, servicio o campaña.
- La segunda microfrase debe ser una frase breve emocional, promocional o de reacción natural.
- La tercera microfrase debe ser siempre la frase de campaña exacta como cierre, conservada en español y entre comillas.
- No debe generar un diálogo donde el personaje diga solamente la frase de campaña aislada.
- El diálogo completo debe sentirse como un microdiálogo breve y natural.
- La frase de campaña debe integrarse dentro de un microdiálogo breve, natural y coherente en español latino.
- La frase de campaña no debe sonar como una frase aislada, seca, robótica o forzada.
- No debe obligar siempre una reacción emocional previa.
- No debe inventar palabras raras, sonidos sin sentido, frases incoherentes, muletillas extrañas ni expresiones difíciles de pronunciar.
- El diálogo total debe ser breve, natural y fácil de sincronizar en un video de 8 segundos.
- La frase de campaña y cualquier frase de apoyo deben escucharse como audio, pero esta idea debe expresarse en promptFinal solo dentro del bloque final de restricciones.
- No repitas en esta sección la indicación completa de microdiálogo si ya aparece en Reglas principales para promptFinal.

Reglas para controlar sincronización labial:
- En esta fase de prueba, si hay una persona visible en el video, se debe intentar que hable en cámara con sincronización labial natural.
- El personaje debe hablar mirando de forma natural hacia cámara o hacia otra persona.
- El rostro y la boca deben estar claramente visibles mientras habla.
- La voz debe sentirse como si saliera directamente de la boca del personaje visible.
- Debe pedir movimientos de boca coherentes, expresión facial acorde al diálogo y tiempos realistas.
- El diálogo debe ser muy corto, natural y en español latino.
- No debe usar diálogo visible largo en videos de 8 segundos.
- Si el personaje principal es un producto, peluche u objeto sin boca realista, no debe forzar que el objeto hable; una persona cercana debe hablar sobre el producto.
- Si una frase exacta es importante, debe ser breve para mejorar la probabilidad de sincronización.
- Si el usuario pide voz en off explícitamente, debe respetar voz en off y no forzar diálogo visible.
- Si el usuario pide video sin voz explícitamente, debe respetar video sin voz.

Reglas de claridad comercial:
- Debe comunicar la campaña principalmente con imagen, acción, emoción, producto y voz en español cuando sea útil.
- Debe mantener el producto, servicio o idea principal claramente visible.
- Debe evitar escenas confusas, demasiado rápidas o con demasiados elementos.
- Debe evitar exageraciones visuales que hagan que el video parezca falso.
- Debe evitar usar la palabra "simple" en promptFinal. En su lugar usar "clear, natural, emotionally focused, well-composed".

Reglas sobre personajes:
- Si el usuario menciona nombres propios, deben usarse como nombres narrativos de los personajes.
- Los nombres propios deben conservarse exactamente como los escribió el usuario.
- No traducir nombres propios.
- No inventar nombres nuevos si el usuario ya dio nombres.
- En promptFinal, si no hay imagen de referencia, usar expresiones como:
  "a young man representing [nombre]"
  "a young woman representing [nombre]"
- No afirmar que el modelo generará exactamente a una persona real solo por su nombre.
- No pedir parecido físico exacto si el usuario no proporcionó imagen de referencia.
- Si el usuario describe cómo debe verse un personaje, el promptFinal debe conservar esa descripción visual.
- Si el usuario no describe al personaje, usar una descripción general favorable, realista y natural.
- Para evitar personajes genéricos repetidos, el promptFinal debe incluir rasgos visuales básicos cuando sea útil: edad aproximada, estilo de cabello, vestimenta, expresión y presencia.
- Para personajes masculinos, usar descripciones como: pleasant-looking Latin American young adult man, short dark hair, clean appearance, warm smile, casual modern outfit, natural expression.
- Para personajes femeninos, usar descripciones como: pleasant-looking Latin American young adult woman, natural beauty, warm smile, soft emotional expression, casual elegant outfit.
- Evitar descripciones ofensivas, exageradas o poco naturales.
- Evitar rostros extraños, expresiones artificiales, miradas vacías o personas deformadas.
- Si hay interacción entre personajes, debe ser natural, respetuosa, clara y emocionalmente coherente.
- Si hay entrega de un objeto, como flores o un producto, debe describirse con claridad: quién entrega, quién recibe y cómo reacciona.
- Si el usuario pide una escena romántica, debe mantener una interacción tierna, respetuosa, cálida y natural.
- Si el usuario pide una dedicatoria personal, debe tratarse como storytelling emocional, no como una escena exagerada o teatral.

Estilo visual recomendado para promptFinal:
- realistic everyday lifestyle scene
- natural daylight
- believable urban or indoor environment
- authentic human movement
- clean, well-composed background
- smooth and controlled camera movement
- natural colors
- casual modern style
- minimal distractions
- clear product or emotion focus
- polished but realistic visual quality
- warm emotional atmosphere when the campaign is romantic or personal
- emotional marketing-style short video when the campaign is personal or romantic

Evita estilos exagerados en promptFinal:
- hyper cinematic
- luxury commercial
- premium luxury branding
- dramatic movie trailer
- futuristic interface
- complex dashboard
- fantasy visuals
- artificial-looking people
- unrealistic fashion runway style
- exaggerated effects
- overprocessed colors
- theatrical romantic acting
- overly dramatic facial expressions

Reglas estrictas sobre texto visible:
- Por defecto y para esta fase de prueba, NO debe pedir ningún texto visible dentro del video.
- Debe evitar completamente subtítulos, captions, letreros, pantallas con texto, logos, interfaces, palabras visibles, letras, números, símbolos, overlays, carteles, marcas escritas y texto decorativo.
- Si hay frase de campaña, diálogo o palabras habladas, deben ser únicamente audio, nunca texto visible.
- Si hay diálogo, frase de campaña hablada o palabras habladas, el modelo NO debe mostrar subtítulos, captions, karaoke text, speech bubbles, letras sincronizadas, palabras flotantes ni transcripción visual de lo que se dice.
- El diálogo debe existir únicamente como audio natural y movimiento de boca con sincronización labial.
- Hablar no significa subtitular.
- La frase de campaña y cualquier frase de apoyo deben escucharse como audio, pero nunca aparecer escritas en pantalla.
- No debe pedir frases en pantalla, ni al inicio, ni al final, ni durante la escena.
- No debe pedir que el modelo genere palabras dentro de ropa, paredes, calles, pantallas, carteles, productos o fondos.
- promptFinal debe incluir las restricciones de texto visible una sola vez, dentro del bloque final de restricciones.
- No debe repetir por separado la instrucción negativa de texto visible ni la instrucción fuerte de palabras habladas como audio.
- No debe incluir excepciones para texto visible.
- Debe preferir comunicación visual, acción, emoción y voz en español latino antes que cualquier texto en pantalla.

Reglas negativas obligatorias:
El promptFinal debe evitar:
- English voiceover
- English speech
- English dialogue
- English audio
- distorted letters
- broken words
- random symbols
- messy text
- unreadable typography
- fake logos
- unreadable signs
- confusing interfaces
- distorted faces
- distorted hands
- distorted feet
- distorted products
- unnatural body movement
- unrealistic lighting
- cluttered backgrounds
- strange facial expressions
- empty eyes
- awkward romantic interaction
- creepy expressions
- unrealistic couple behavior
- visible speaking with no audio
- bad lip sync
- awkward mouth movement
- silent talking
- unrealistic mouth movement
- artificial-looking people
- repeated generic characters when the user asks for specific character traits

Formato obligatorio del JSON:
{
  "resumenContexto": "texto en español",
  "guionGenerado": "texto en español",
  "promptFinalEspanol": "prompt técnico en español",
  "promptFinal": "text in English"
}
""";
    }

    private String extraerUltimaFraseCampaniaValida(String contexto) {
        if (contexto == null || contexto.isBlank()) {
            return "";
        }

        String ultimaFraseValida = "";
        String[] lineas = contexto.split("\\R");

        for (String linea : lineas) {
            String lineaLimpia = linea == null ? "" : linea.trim();

            if (!lineaLimpia.startsWith("Frase de campaña:")) {
                continue;
            }

            int primeraComilla = lineaLimpia.indexOf('"');
            int segundaComilla = primeraComilla >= 0 ? lineaLimpia.indexOf('"', primeraComilla + 1) : -1;

            if (primeraComilla < 0 || segundaComilla <= primeraComilla + 1) {
                continue;
            }

            String frase = lineaLimpia.substring(primeraComilla + 1, segundaComilla).trim();

            if (!frase.isBlank()) {
                ultimaFraseValida = frase;
            }
        }

        return ultimaFraseValida;
    }

    private boolean hayImagenReferenciaValida(String contexto) {
        if (contexto == null || contexto.isBlank()) {
            return false;
        }

        String[] lineas = contexto.split("\\R");

        for (String linea : lineas) {
            String lineaLimpia = linea == null ? "" : linea.trim().toLowerCase();

            if (lineaLimpia.contains("tipo:") && lineaLimpia.contains("imagen")) {
                return true;
            }
        }

        return false;
    }

    private String crearUserPrompt(String contexto, String fraseCampania, boolean hayImagenReferenciaValida) {
        String estadoFraseCampania = fraseCampania == null || fraseCampania.isBlank()
                ? "NO_HAY_FRASE_DE_CAMPANIA_VALIDA"
                : "FRASE_DE_CAMPANIA_VALIDA: \"" + fraseCampania + "\"";
        String estadoImagenReferencia = hayImagenReferenciaValida
                ? "HAY_IMAGEN_REFERENCIA_VALIDA"
                : "NO_HAY_IMAGEN_REFERENCIA_VALIDA";

        return """
    Analiza el siguiente contexto de una generación IA de VisionAstra.

    Necesito que prepares cuatro resultados:

    1. resumenContexto
    - Debe estar en español.
    - Debe resumir qué quiere comunicar la campaña.
    - Debe mencionar el producto, servicio o idea principal.
    - Debe mencionar el público objetivo si aparece en el contexto.
    - Debe mencionar el beneficio principal o emoción que se quiere transmitir.
    - Debe ser claro, breve y útil para el usuario.

    2. guionGenerado
    - Debe estar en español.
    - Debe ser breve y dividido por escenas.
    - Debe estar pensado para un video promocional de 8 segundos.
    - Debe usar escenas claras, naturales, realistas, bien compuestas y fáciles de generar.
    - Debe evitar que el video se sienta básico, vacío o pobre.
    - Debe evitar demasiadas acciones o cambios de escena.
    - Debe evitar ideas exageradas, confusas o poco realistas.
    - Debe mantener una intención comercial clara.
    - Si menciona voz o diálogo, debe indicar español latino natural.
    - No debe plantear audio, narración ni diálogo en inglés.
    - Si menciona una frase de campaña o diálogo, debe aclarar que será hablado como audio, no mostrado como texto en pantalla.
    - No debe describir el diálogo como subtítulo, caption, texto visual, frase escrita o cartel.
    - No debe sugerir que la frase aparezca escrita en pantalla.
    - Solo si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, debe indicar que la persona visible empieza a hablar desde la primera parte del video o dentro de los primeros 1.5 segundos.
    - Solo si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, no debe dejar toda la voz para la escena final.
    - Solo si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, debe distribuir un microdiálogo de exactamente 3 microfrases muy cortas, natural y fluido dentro de los 8 segundos.
    - Solo si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, la frase exacta debe aparecer como cierre hablado del microdiálogo, solo audio y nunca texto en pantalla.
    - Si el estado de frase de campaña detectado por backend es NO_HAY_FRASE_DE_CAMPANIA_VALIDA, no debe inventar una frase de campaña obligatoria ni exigir un cierre hablado con frase exacta.

    3. promptFinalEspanol
    - Debe estar escrito en español.
    - Debe ser una versión técnica y entendible del promptFinal.
    - Debe describir el mismo video que se enviará a Google Veo, pero explicado en español.
    - Debe mencionar duración, formato vertical, estilo visual, escenas principales, cámara, iluminación, ambiente y ritmo.
    - Debe aclarar las restricciones principales como evitar texto en pantalla, logos, subtítulos, interfaces y elementos irreales.
    - Si hay diálogo, frase de campaña hablada o palabras habladas, debe aclarar que el modelo NO debe mostrar subtítulos, captions, karaoke text, speech bubbles, letras sincronizadas, palabras flotantes ni transcripción visual de lo que se dice.
    - Debe aclarar que el diálogo debe existir únicamente como audio natural y movimiento de boca con sincronización labial.
    - Debe aclarar que hablar no significa subtitular.
    - Debe aclarar que el resultado del video no debe tener voz en inglés, diálogo en inglés, narración en inglés ni audio en inglés.
    - Si hay audio, debe ser español latino natural.
    - Para esta fase de prueba, si hay una persona visible en la escena, debe explicar que se buscará diálogo visible en cámara, con voz saliendo de la boca del personaje, rostro visible, boca visible y sincronización labial natural.
    - Solo debe explicar voz en off si el usuario la pide explícitamente, si no hay una persona visible adecuada para hablar, o si el usuario pide un estilo narrado.
    - Debe explicar que el video puede entenderse con imagen, gestos, emociones, acciones naturales y acercamientos del producto.
    - No debe agregar ideas nuevas que no estén en el promptFinal.
    - Solo sirve para que el usuario entienda el resultado.
    - No se enviará a Google Veo.
    
    4. promptFinal
    - Debe estar escrito en inglés.
    - Debe estar optimizado para Google Veo u otro modelo de video con IA.
    - Debe describir un video vertical realista de aproximadamente 8 segundos.
    - Debe sentirse natural, creíble, moderno y profesional.
    - Debe evitar estética falsa, exagerada o demasiado lujosa.
    - Debe priorizar movimiento humano natural, luz realista, colores naturales y composición limpia.
    - Debe usar cámara suave, controlada y profesional.
    - Debe comunicar la campaña principalmente con imagen, acción y emoción.
    - Debe devolver un promptFinal único y no duplicado.
    - No debe repetir el promptFinal completo dos veces.
    - No debe repetir el bloque de imagen de referencia dos veces.
    - No debe repetir la descripción principal del video dos veces.
    - No debe repetir las restricciones finales dos veces.
    - No debe repetir reglas de voz y sincronización labial dos veces.
    - El promptFinal debe tener una estructura limpia:
      1. Imagen de referencia si existe.
      2. Descripción visual del video.
      3. Diálogo/audio en español latino si corresponde.
      4. Restricciones negativas finales.
    - Cada instrucción importante debe aparecer una sola vez.
    - Si detecta que está repitiendo el mismo texto, debe eliminar la repetición antes de devolver el JSON.
    - Debe evitar depender de texto visible.
    - Debe evitar logos, letreros, subtítulos, captions, interfaces, pantallas y palabras visibles.
    - Debe incluir una sola vez al final estas restricciones finales:
      Spoken dialogue must be heard only as natural audio with lip sync. Do not generate subtitles, captions, karaoke text, speech bubbles, floating words, synced text, or any visual transcription of the spoken dialogue. No on-screen text, no subtitles, no captions, no logos, no readable signs, no interfaces. No English voiceover, no English speech, no English dialogue, no English audio. Do not display any written words, letters, numbers, captions, subtitles, overlays, labels, signs, logos, brand text, UI elements, or decorative text anywhere in the video.
    - Para esta fase de prueba, si el video incluye una persona visible, debe preferir diálogo visible en cámara antes que voz en off.
    - Debe pedir que la voz salga directamente de la boca del personaje visible.
    - Debe solicitar rostro visible, boca visible, sincronización labial natural, movimiento de boca coherente, expresión facial acorde al diálogo y audio que parezca salir directamente de la boca del personaje.
    - Si hay diálogo visible, la voz debe sonar como una voz humana real, juvenil, cálida, suave, amable, expresiva y natural en español latino.
    - El diálogo debe sentirse conversacional, fluido y casual, no como una voz generada, robótica, rígida, monótona o de locutor.
    - Debe evitar voz gruesa, voz demasiado grave, voz tenebrosa, voz artificial, voz dramática, voz lenta exagerada o voz de robot.
    - El diálogo debe tener ritmo natural, pausas suaves y pronunciación clara.
    - Para productos tiernos o adorables, debe preferir una voz cálida, ligera, alegre y cercana.
    - promptFinal debe incluir una indicación similar a:
      "The visible speaker's voice should sound like a real, warm, friendly, youthful Latin American Spanish voice, soft and natural, with smooth conversational rhythm. Avoid robotic, synthetic, monotone, overly deep, scary, dramatic, announcer-like, or artificial voice delivery."
    - Si usa diálogo visible, promptFinal debe incluir una frase en inglés similar a:
      Natural Latin American Spanish dialogue only. A visible speaker talks naturally on camera. The voice must sound like it comes directly from the visible speaker’s mouth, with natural lip synchronization, clear mouth movement, realistic timing, and facial expression matching the dialogue.
    - Si usa diálogo visible, no debe incluir al mismo tiempo la instrucción "Spanish voiceover only".
    - Solo debe usar voz en off si el usuario la pide explícitamente, si no hay una persona visible adecuada para hablar, o si el usuario pide un estilo narrado.
    - Si usa voz en off, debe indicar:
      Spanish voiceover only, in a natural Latin American Spanish voice.
    - Solo si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, debe usar esa frase como frase principal de campaña.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, debe conservar la frase exactamente en español, sin traducirla.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, promptFinal debe incluir explícitamente la frase exacta una sola vez dentro del prompt en inglés, conservándola en español y entre comillas.
    - Si el estado de frase de campaña detectado por backend es NO_HAY_FRASE_DE_CAMPANIA_VALIDA, promptFinal NO debe mencionar "campaign line", "final campaign phrase", "exact campaign line", "The final phrase must be", "[frase exacta de campaña]" ni "[campaign line]".
    - Si el estado de frase de campaña detectado por backend es NO_HAY_FRASE_DE_CAMPANIA_VALIDA, promptFinal no debe dejar instrucciones incompletas sobre frase final de campaña.
    - Si el estado de frase de campaña detectado por backend es NO_HAY_FRASE_DE_CAMPANIA_VALIDA, promptFinal debe usar una regla equivalente a: If no campaign line is provided, do not invent or force a final campaign phrase. Use only a very short natural Latin American Spanish spoken moment if it helps the scene, or rely on visual storytelling, product presentation, facial expression, and camera movement.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, no basta con decir "the campaign line"; debe escribirse la frase literal, preferentemente solo dentro de esta oración:
      The final phrase must be the campaign line exactly as written in Spanish: "Llévate el tuyo hoy".
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, no debe agregar otra línea separada como "Use the campaign line exactly as written in Spanish: "Llévate el tuyo hoy"."
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, no debe traducir la frase al inglés.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, no debe reemplazarla por una frase genérica.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, no debe omitir la frase literal del promptFinal.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, el hablante visible debe comenzar a hablar dentro de los primeros 1.5 segundos del video.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, no debe dejar el diálogo únicamente para los últimos segundos del video.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, el microdiálogo hablado debe tener exactamente 3 microfrases muy cortas en español latino.
    - El microdiálogo debe sonar natural, fluido y conversacional, no como una estructura mecánica o forzada.
    - La primera microfrase debe ser una frase breve de contexto relacionada con el producto, servicio o campaña.
    - La segunda microfrase debe ser una frase breve emocional, promocional o de reacción natural.
    - La tercera microfrase debe ser siempre la frase de campaña exacta como cierre, conservada en español y entre comillas.
    - No debe generar un diálogo donde el personaje diga solamente la frase de campaña aislada.
    - El diálogo completo debe sentirse como un microdiálogo breve y natural.
    - La frase de campaña debe integrarse dentro de un microdiálogo breve, natural y coherente en español latino.
    - La frase de campaña no debe sonar como una frase aislada, seca, robótica o forzada.
    - Las microfrases deben ser simples, naturales, fáciles de pronunciar y coherentes con el producto, servicio o emoción de la campaña.
    - No debe obligar siempre una reacción emocional previa.
    - No debe inventar palabras raras, sonidos sin sentido, frases incoherentes, muletillas extrañas ni expresiones difíciles de pronunciar.
    - Si hay una sola persona visible, esa persona puede decir un microdiálogo breve que integre la frase de campaña de forma natural.
    - Si hay dos personas visibles, pueden participar en un microdiálogo breve, pero la frase de campaña debe mantenerse exacta.
    - El diálogo total debe ser breve, natural y fácil de sincronizar en un video de 8 segundos.
    - La emoción debe comunicarse también con expresión facial, mirada, sonrisa, gestos naturales, movimiento corporal, presentación del producto y cámara.
    - El diálogo visible debe ser muy corto, natural, en español latino y fácil de sincronizar.
    - La voz debe sonar joven, amable, cálida, natural y promocional.
    - Debe evitar voz profunda, tenebrosa, robótica, exagerada o dramática.
    - La frase de campaña y cualquier frase de apoyo deben escucharse como audio, pero esta idea debe expresarse en promptFinal solo dentro del bloque final de restricciones.
    - No repitas fuera del bloque final las restricciones de texto visible, audio en inglés, subtítulos, captions, transcripción visual o palabras habladas como audio.
    - Si el estado de frase de campaña detectado por backend es FRASE_DE_CAMPANIA_VALIDA, promptFinal debe incluir una indicación fuerte similar a:
      The visible speaker must begin speaking within the first 1.5 seconds. Use exactly three very short Latin American Spanish spoken phrases with natural timing and clear lip sync. The first phrase should briefly introduce or point to the product, the second phrase should express a short emotional or promotional reaction, and the third phrase must be the exact campaign line as written in Spanish: "[frase exacta de campaña]". Do not delay the dialogue until the end of the video. Do not skip, replace, shorten, or translate the campaign line. Do not make the speaker say only the campaign line by itself.
    - Esa indicación de microdiálogo debe aparecer una sola vez en promptFinal.
    - No debe usar "No voiceover, no spoken words, no dialogue" como regla general.
    - Solo debe pedir video sin voz si el usuario lo solicita explícitamente.
    - No debe solicitar videos de 15, 20 o 30 segundos.
    - El video debe poder entenderse incluso si el audio no sale perfecto.
    - Si hay frase de campaña o diálogo, la prohibición de texto visible, subtítulos, captions y transcripción visual debe quedar clara solo en el bloque final de restricciones de promptFinal.
    - Si hay diálogo, frase de campaña hablada o palabras habladas, el modelo NO debe mostrar subtítulos, captions, karaoke text, speech bubbles, letras sincronizadas, palabras flotantes ni transcripción visual de lo que se dice.
    - El diálogo debe existir únicamente como audio natural y movimiento de boca con sincronización labial.
    - Hablar no significa subtitular.
    - La frase de campaña y cualquier frase de apoyo deben escucharse como audio, pero nunca aparecer escritas en pantalla.
    - Debe prohibir cualquier letra, palabra, número, símbolo, subtítulo, caption, overlay, cartel, logo, marca escrita o texto decorativo dentro del video.
    - No repitas fuera del bloque final las restricciones de texto visible, audio en inglés, subtítulos, captions, transcripción visual o palabras habladas como audio.
   
     Regla especial para imagen de referencia:
    - La decisión sobre imagen de referencia debe basarse únicamente en el estado de imagen de referencia detectado por backend: HAY_IMAGEN_REFERENCIA_VALIDA o NO_HAY_IMAGEN_REFERENCIA_VALIDA.
    - No decidas que hay imagen de referencia solo porque el texto del usuario mencione las palabras "imagen de referencia".
    - Si el estado de imagen de referencia detectado por backend es HAY_IMAGEN_REFERENCIA_VALIDA, entonces promptFinal debe mencionar explícitamente al inicio que Google Veo debe usar la imagen seleccionada como referencia visual principal.
    - Si el estado de imagen de referencia detectado por backend es HAY_IMAGEN_REFERENCIA_VALIDA, la imagen no debe copiarse de forma exacta, pero el resultado debe conservar rasgos reconocibles del personaje o producto: colores principales, forma general, proporciones, detalles visuales importantes, textura y apariencia general.
    - Si el estado de imagen de referencia detectado por backend es HAY_IMAGEN_REFERENCIA_VALIDA, el resultado debe sentirse claramente inspirado en la imagen seleccionada y adaptado de forma natural al video promocional.
    - Si el estado de imagen de referencia detectado por backend es HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe incluir una frase similar a:
      Use the selected reference image as the main visual guide. The video should not copy the image exactly, but it must clearly preserve the recognizable features of the character or product, including its main colors, general shape, proportions, important visual details, texture, and overall appearance.
    - Si el estado de imagen de referencia detectado por backend es HAY_IMAGEN_REFERENCIA_VALIDA, promptFinalEspanol también debe explicar que se usará la imagen de referencia como guía visual principal.
    - Si el estado de imagen de referencia detectado por backend es HAY_IMAGEN_REFERENCIA_VALIDA, resumenContexto debe mencionar que hay una imagen de referencia seleccionada.
    - Si el estado de imagen de referencia detectado por backend es HAY_IMAGEN_REFERENCIA_VALIDA, mantén el enfoque principal en el producto, personaje o elemento referenciado, no fuerces grupo grande ni diálogo grupal, y prefiere una persona principal hablando si corresponde.
    - Si el estado de imagen de referencia detectado por backend es HAY_IMAGEN_REFERENCIA_VALIDA y aparecen otras personas, solo deben acompañar con movimientos sutiles y naturales sin quitar protagonismo al producto, personaje o elemento referenciado.
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal NO debe mencionar "selected reference image", "reference image", "visual guide" ni "preserve recognizable features from the image".
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal NO debe decir "Use the selected reference image as the main visual guide."
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe iniciar con una frase similar a: Use the product, campaign, and scene description as the creative basis.
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe basarse únicamente en la descripción del producto, campaña, escena, recursos de texto o idea inicial.
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA, no debe fingir que existe una imagen seleccionada.
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal puede permitir un grupo pequeño de 2 o 3 personas jóvenes visibles.
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA y aparece un grupo pequeño, una persona principal debe liderar el diálogo y como máximo una persona secundaria puede decir una frase muy corta.
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA y aparecen personas que no hablan, deben mantenerse activas con sonrisas sutiles, movimiento de mirada, pequeños gestos de manos, cambios suaves de postura y reacciones breves al producto.
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA, no debe mostrar personas congeladas, rígidas, inmóviles, como maniquíes ni con mirada vacía.
    - Si el estado de imagen de referencia detectado por backend es NO_HAY_IMAGEN_REFERENCIA_VALIDA, promptFinal debe incluir una instrucción en inglés similar a:
      If no reference image is provided, a small group of two or three young people may appear. One main visible speaker should lead the dialogue, and at most one secondary person may speak one very short phrase. Non-speaking people must remain naturally active with subtle smiles, eye movement, small hand gestures, posture shifts, and brief reactions to the product. Do not show frozen, static, mannequin-like, motionless, or empty-eyed background people.

    Evita que el promptFinal use expresiones como:
    - luxury commercial
    - hyper cinematic
    - premium branding
    - dramatic trailer
    - futuristic dashboard
    - complex interface
    - exaggerated commercial look

    Prefiere expresiones como:
    - realistic everyday lifestyle scene
    - natural daylight
    - believable environment
    - authentic human movement
    - clean and well-composed background
    - smooth controlled camera movement
    - natural colors
    - casual modern style
    - polished but realistic look

    Estado de frase de campaña detectado por backend:
    %s

    Estado de imagen de referencia detectado por backend:
    %s

    Contexto:
    %s

    Devuelve únicamente JSON válido con los cuatro campos obligatorios:
    {
      "resumenContexto": "...",
      "guionGenerado": "...",
      "promptFinalEspanol": "...",
      "promptFinal": "..."
    }
    """.formatted(estadoFraseCampania, estadoImagenReferencia, contexto);
    }

    private String extraerTextoRespuesta(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);

        JsonNode outputText = root.get("output_text");
        if (outputText != null && outputText.isTextual()) {
            return outputText.asText();
        }

        JsonNode output = root.get("output");
        if (output != null && output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.get("content");

                if (content != null && content.isArray()) {
                    for (JsonNode contentItem : content) {
                        JsonNode textNode = contentItem.get("text");

                        if (textNode != null && textNode.isTextual()) {
                            return textNode.asText();
                        }
                    }
                }
            }
        }

        throw new RuntimeException("No se pudo extraer el texto de la respuesta de OpenAI.");
    }

    private String obtenerTexto(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull()) {
            return "";
        }

        return value.asText();
    }
}
