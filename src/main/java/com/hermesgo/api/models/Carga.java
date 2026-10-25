package com.hermesgo.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cargas")
public class Carga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    private String direccionOrigen;
    private String direccionDestino;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String tipoCarga;

    @Column(nullable = false)
    private Double peso;

    private Double volumen;

    @Column(nullable = false)
    private Boolean esPerecedera;

    @Column(nullable = false)
    private Boolean esCompartida;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "transportador_id")
    private Usuario transportador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCarga estado = EstadoCarga.PENDIENTE;

    private String metodoPago;

    @Column(nullable = false)
    private Boolean pagado = false;

    // --- CAMPOS PARA EL R08 (SEGUIMIENTO) ---
    private Double latitudActual;
    private Double longitudActual;
    private LocalDateTime ultimaActualizacionUbicacion;

    // --- CAMPOS PARA EL R09 (CALIFICACIÓN) ---
    private Integer calificacion;
    private String comentarioCalificacion;

    // --- CAMPOS PARA EL R04 (RELIQUIDACIÓN) ---
    private Double precioBase;
    private Double precioFinal;
    private Boolean estaReliquidada = false;

    // --- CAMPOS PARA EL ACTA DE ENTREGA (R09) ---
    private String codigoEntrega;
    private String codigoRecogida; // <-- ESTE ES EL QUE FALTABA
    private String observacionesEntrega;

    public enum EstadoCarga {
        PENDIENTE,
        ACEPTADA,
        EN_RUTA,
        ENTREGADA
    }
}