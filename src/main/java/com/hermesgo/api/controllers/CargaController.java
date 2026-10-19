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

import java.time.LocalDateTime;
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
        return ResponseEntity.ok(cargaRepository.save(carga));
    }

    // R06: Aceptar Servicio
    @PutMapping("/{cargaId}/aceptar/{transportadorId}")
    public ResponseEntity<?> aceptarCarga(@PathVariable Long cargaId, @PathVariable Long transportadorId) {
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);
        Optional<Usuario> transportadorOpt = usuarioRepository.findById(transportadorId);

        if (cargaOpt.isEmpty()) return ResponseEntity.badRequest().body("La carga no existe");
        if (transportadorOpt.isEmpty()) return ResponseEntity.badRequest().body("El transportador no existe");

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
        return ResponseEntity.ok(cargaRepository.save(carga));
    }

    // R07: Pagar Servicio
    @PutMapping("/{cargaId}/pagar")
    public ResponseEntity<?> pagarCarga(@PathVariable Long cargaId, @RequestParam String metodoPago) {
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);
        if (cargaOpt.isEmpty()) return ResponseEntity.badRequest().body("La carga no existe");

        Carga carga = cargaOpt.get();
        if (carga.getEstado() == Carga.EstadoCarga.PENDIENTE) {
            return ResponseEntity.badRequest().body("La carga debe ser aceptada antes de pagar");
        }
        if (carga.getPagado()) return ResponseEntity.badRequest().body("La carga ya fue pagada");

        carga.setMetodoPago(metodoPago.toUpperCase());
        carga.setPagado(true);
        return ResponseEntity.ok(cargaRepository.save(carga));
    }

    // R08: Actualizar ubicación
    @PutMapping("/{cargaId}/ubicacion")
    public ResponseEntity<?> actualizarUbicacion(@PathVariable Long cargaId, @RequestParam Double latitud, @RequestParam Double longitud) {
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);
        if (cargaOpt.isEmpty()) return ResponseEntity.badRequest().body("La carga no existe");

        Carga carga = cargaOpt.get();
        carga.setLatitudActual(latitud);
        carga.setLongitudActual(longitud);
        carga.setUltimaActualizacionUbicacion(LocalDateTime.now());
        if (carga.getEstado() == Carga.EstadoCarga.ACEPTADA) {
            carga.setEstado(Carga.EstadoCarga.EN_RUTA);
        }
        return ResponseEntity.ok(cargaRepository.save(carga));
    }

    // R08: Consultar seguimiento
    @GetMapping("/{cargaId}/seguimiento")
    public ResponseEntity<?> consultarSeguimiento(@PathVariable Long cargaId) {
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);
        if (cargaOpt.isEmpty()) return ResponseEntity.badRequest().body("La carga no existe");
        return ResponseEntity.ok(cargaOpt.get());
    }

    // R09: Confirmar entrega y calificar
    @PutMapping("/{cargaId}/confirmar-entrega")
    public ResponseEntity<?> confirmarEntregaYCalificar(
            @PathVariable Long cargaId,
            @RequestParam Integer calificacion,
            @RequestParam(required = false) String comentario) {
        
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);
        if (cargaOpt.isEmpty()) return ResponseEntity.badRequest().body("La carga no existe");

        Carga carga = cargaOpt.get();
        if (carga.getEstado() == Carga.EstadoCarga.ENTREGADA) {
            return ResponseEntity.badRequest().body("La carga ya fue entregada");
        }
        if (calificacion < 1 || calificacion > 5) {
            return ResponseEntity.badRequest().body("La calificación debe ser entre 1 y 5");
        }

        carga.setEstado(Carga.EstadoCarga.ENTREGADA);
        carga.setCalificacion(calificacion);
        carga.setComentarioCalificacion(comentario);
        
        return ResponseEntity.ok(cargaRepository.save(carga));
    }
    // R10: Historial de cargas del cliente
    @GetMapping("/historial/{clienteId}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable Long clienteId) {
        return ResponseEntity.ok(cargaRepository.findByClienteId(clienteId));
    }
    // R12: Calificar el servicio (Endpoint separado)
    @PutMapping("/{cargaId}/calificar")
    public ResponseEntity<?> calificarServicio(
            @PathVariable Long cargaId,
            @RequestParam Integer calificacion,
            @RequestParam(required = false) String comentario) {
        
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);
        if (cargaOpt.isEmpty()) return ResponseEntity.badRequest().body("La carga no existe");

        Carga carga = cargaOpt.get();
        if (carga.getEstado() != Carga.EstadoCarga.ENTREGADA) {
            return ResponseEntity.badRequest().body("La carga debe estar entregada para poder calificar");
        }
        if (calificacion < 1 || calificacion > 5) {
            return ResponseEntity.badRequest().body("La calificación debe ser entre 1 y 5");
        }

        carga.setCalificacion(calificacion);
        carga.setComentarioCalificacion(comentario);
        
        return ResponseEntity.ok(cargaRepository.save(carga));
    }
}