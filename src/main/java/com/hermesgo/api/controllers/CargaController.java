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

    // R04: Publicar Carga
    @PostMapping("/publicar")
    public ResponseEntity<?> publicarCarga(@Valid @RequestBody CargaDTO cargaDTO) {
        Optional<Usuario> clienteOpt = usuarioRepository.findById(cargaDTO.getClienteId());
        
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El cliente no existe");
        }

        Carga carga = cargaDTO.toEntity(clienteOpt.get());
        Carga cargaGuardada = cargaRepository.save(carga);
        
        return ResponseEntity.ok(cargaGuardada);
    }

    // R06: Aceptar Servicio (Un transportador acepta la carga)
    @PutMapping("/{cargaId}/aceptar/{transportadorId}")
    public ResponseEntity<?> aceptarCarga(
            @PathVariable Long cargaId, 
            @PathVariable Long transportadorId) {
        
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);
        Optional<Usuario> transportadorOpt = usuarioRepository.findById(transportadorId);

        if (cargaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("La carga no existe");
        }
        if (transportadorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El transportador no existe");
        }

        Carga carga = cargaOpt.get();
        Usuario transportador = transportadorOpt.get();

        // Verificamos que el transportador tenga el rol correcto
        if (transportador.getRol() != Usuario.Rol.TRANSPORTADOR) {
            return ResponseEntity.badRequest().body("El usuario no es un transportador");
        }

        // Verificamos que la carga esté pendiente
        if (carga.getEstado() != Carga.EstadoCarga.PENDIENTE) {
            return ResponseEntity.badRequest().body("La carga ya fue aceptada o está en proceso");
        }

        // Asignamos el transportador y cambiamos el estado
        carga.setTransportador(transportador);
        carga.setEstado(Carga.EstadoCarga.ACEPTADA);
        
        Carga cargaActualizada = cargaRepository.save(carga);
        return ResponseEntity.ok(cargaActualizada);
    }
}