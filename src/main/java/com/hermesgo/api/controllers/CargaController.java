package com.hermesgo.api.controllers;

import com.hermesgo.api.models.dtos.CargaDTO;
import com.hermesgo.api.models.Carga;
import com.hermesgo.api.models.Usuario;
import com.hermesgo.api.repositories.CargaRepository;
import com.hermesgo.api.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/cargas")
public class CargaController {

    @Autowired
    private CargaRepository cargaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/publicar")
    public ResponseEntity<?> publicarCarga(@Valid @RequestBody CargaDTO cargaDTO) {
        // Verificamos que el cliente exista
        Optional<Usuario> clienteOpt = usuarioRepository.findById(cargaDTO.getClienteId());
        
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El cliente no existe");
        }

        // Convertimos el DTO a Entidad y lo guardamos
        Carga carga = cargaDTO.toEntity(clienteOpt.get());
        Carga cargaGuardada = cargaRepository.save(carga);
        
        return ResponseEntity.ok(cargaGuardada);
    }
}