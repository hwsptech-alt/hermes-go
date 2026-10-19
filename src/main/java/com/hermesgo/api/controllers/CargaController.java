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

    // R06: Aceptar Servicio
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

        if (transportador.getRol() != Usuario.Rol.TRANSPORTADOR) {
            return ResponseEntity.badRequest().body("El usuario no es un transportador");
        }

        if (carga.getEstado() != Carga.EstadoCarga.PENDIENTE) {
            return ResponseEntity.badRequest().body("La carga ya fue aceptada o está en proceso");
        }

        carga.setTransportador(transportador);
        carga.setEstado(Carga.EstadoCarga.ACEPTADA);
        
        Carga cargaActualizada = cargaRepository.save(carga);
        return ResponseEntity.ok(cargaActualizada);
    }

    // R07: Pagar Servicio (Simulado)
    @PutMapping("/{cargaId}/pagar")
    public ResponseEntity<?> pagarCarga(
            @PathVariable Long cargaId, 
            @RequestParam String metodoPago) {
        
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);

        if (cargaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("La carga no existe");
        }

        Carga carga = cargaOpt.get();

        // Validamos que la carga ya haya sido aceptada por un transportador
        if (carga.getEstado() == Carga.EstadoCarga.PENDIENTE) {
            return ResponseEntity.badRequest().body("La carga debe ser aceptada antes de pagar");
        }

        if (carga.getPagado()) {
            return ResponseEntity.badRequest().body("La carga ya fue pagada");
        }

        // Simulamos el pago exitoso
        carga.setMetodoPago(metodoPago.toUpperCase());
        carga.setPagado(true);
        
        Carga cargaActualizada = cargaRepository.save(carga);
        return ResponseEntity.ok(cargaActualizada);
    }
}