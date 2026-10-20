package com.hermesgo.api.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, unique = true)
    private String correoElectronico;

    @Column(nullable = false)
    private String contrasena;

    private String telefono;

    // NUEVO CAMPO PARA LA FOTO DE PERFIL
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    // Campos específicos para TRANSPORTADORES (nulos para clientes)
    private String tipoVehiculo;
    private String placa;
    private Double capacidad;
    private Double calificacion;
    private Double precioBase;

    public enum Rol {
        CLIENTE,
        TRANSPORTADOR,
        ADMINISTRADOR
    }
}