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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cargas")
public class CargaController {

    @Autowired
    private CargaRepository cargaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // R04: Publicar Carga (Normal)
    @PostMapping("/publicar")
    public ResponseEntity<?> publicarCarga(@Valid @RequestBody CargaDTO cargaDTO) {
        Optional<Usuario> clienteOpt = usuarioRepository.findById(cargaDTO.getClienteId());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El cliente no existe");
        }

        if (cargaDTO.getFechaHora().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("La fecha y hora no pueden ser anteriores a hoy.");
        }

        Carga carga = cargaDTO.toEntity(clienteOpt.get());
        
        // GENERAMOS EL CÓDIGO DE ENTREGA ALEATORIO (4 DÍGITOS)
        String codigo = String.valueOf((int)(Math.random() * 9000) + 1000);
        carga.setCodigoEntrega(codigo);

        return ResponseEntity.ok(cargaRepository.save(carga));
    }

    // R04: Publicar Carga con Reliquidación Proporcional (Compartida)
    @PostMapping("/publicar-compartida")
    public ResponseEntity<?> publicarCargaCompartida(@Valid @RequestBody CargaDTO cargaDTO) {
        Optional<Usuario> clienteOpt = usuarioRepository.findById(cargaDTO.getClienteId());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El cliente no existe");
        }

        if (cargaDTO.getFechaHora().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("La fecha y hora no pueden ser anteriores a hoy.");
        }

        Carga nuevaCarga = cargaDTO.toEntity(clienteOpt.get());
        nuevaCarga.setEstaReliquidada(false);
        
        // GENERAMOS EL CÓDIGO DE ENTREGA ALEATORIO
        String codigo = String.valueOf((int)(Math.random() * 9000) + 1000);
        nuevaCarga.setCodigoEntrega(codigo);

        // Buscar cargas compatibles
        List<Carga> cargasCompatibles = cargaRepository.findAll().stream()
                .filter(c -> c.getOrigen().equalsIgnoreCase(nuevaCarga.getOrigen())
                        && c.getDestino().equalsIgnoreCase(nuevaCarga.getDestino())
                        && c.getEsCompartida()
                        && (c.getEstaReliquidada() == null || !c.getEstaReliquidada())
                        && c.getEstado() == Carga.EstadoCarga.PENDIENTE)
                .toList();

        if (!cargasCompatibles.isEmpty()) {
            Carga cargaExistente = cargasCompatibles.get(0);
            
            double pesoTotal = cargaExistente.getPeso() + nuevaCarga.getPeso();
            double precioTotalViaje = cargaExistente.getPrecioBase() != null ? cargaExistente.getPrecioBase() : 1000000;

            double precioExistente = (cargaExistente.getPeso() / pesoTotal) * precioTotalViaje;
            double precioNueva = (nuevaCarga.getPeso() / pesoTotal) * precioTotalViaje;

            cargaExistente.setPrecioFinal(precioExistente);
            cargaExistente.setEstaReliquidada(true);
            cargaRepository.save(cargaExistente);

            nuevaCarga.setPrecioBase(precioTotalViaje); 
            nuevaCarga.setPrecioFinal(precioNueva);
            nuevaCarga.setEstaReliquidada(true);
            cargaRepository.save(nuevaCarga);

            return ResponseEntity.ok(nuevaCarga);
        }

        return ResponseEntity.ok(cargaRepository.save(nuevaCarga));
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

    // R09: Confirmar entrega con Código y Observaciones
    @PutMapping("/{cargaId}/confirmar-entrega")
    public ResponseEntity<?> confirmarEntrega(
            @PathVariable Long cargaId,
            @RequestParam String codigo,
            @RequestParam(required = false) String observaciones) {
        
        Optional<Carga> cargaOpt = cargaRepository.findById(cargaId);
        if (cargaOpt.isEmpty()) return ResponseEntity.badRequest().body("La carga no existe");

        Carga carga = cargaOpt.get();
        
        // VALIDACIÓN DEL CÓDIGO
        if (!carga.getCodigoEntrega().equals(codigo)) {
            return ResponseEntity.badRequest().body("El código de entrega es incorrecto");
        }

        carga.setEstado(Carga.EstadoCarga.ENTREGADA);
        carga.setObservacionesEntrega(observaciones); // Guardamos las observaciones
        
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

    // Obtener cargas disponibles para transportadores
    @GetMapping("/disponibles")
    public ResponseEntity<?> obtenerCargasDisponibles() {
        List<Carga> cargas = cargaRepository.findAll().stream()
                .filter(c -> c.getEstado() == Carga.EstadoCarga.PENDIENTE)
                .toList();
        return ResponseEntity.ok(cargas);
    }
}