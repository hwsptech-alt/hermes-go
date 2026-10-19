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

    // Relación con el usuario que publica la carga
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;
}