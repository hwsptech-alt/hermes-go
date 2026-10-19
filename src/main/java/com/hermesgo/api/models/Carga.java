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

    // Relación con el cliente que publica la carga
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    // --- NUEVOS CAMPOS PARA EL R06 ---
    @ManyToOne
    @JoinColumn(name = "transportador_id")
    private Usuario transportador; // Puede ser nulo hasta que alguien la acepte

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCarga estado = EstadoCarga.PENDIENTE;

    public enum EstadoCarga {
        PENDIENTE,
        ACEPTADA,
        EN_RUTA,
        ENTREGADA
    }
}