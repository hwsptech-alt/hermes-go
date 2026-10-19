package com.hermesgo.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ayuda")
public class AyudaController {

    @GetMapping
    public ResponseEntity<?> obtenerCentroAyuda() {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Bienvenido al Centro de Ayuda de Hermes GO");
        respuesta.put("soporte", "soporte@hermesgo.com");
        respuesta.put("telefono", "+57 300 123 4567");
        respuesta.put("whatsapp", "+57 300 123 4567");
        respuesta.put("preguntasFrecuentes", new String[]{
            "¿Cómo publicar una carga?",
            "¿Cómo rastrear mi envío?",
            "¿Qué métodos de pago aceptan?",
            "¿Cómo califico al transportador?"
        });
        return ResponseEntity.ok(respuesta);
    }
}