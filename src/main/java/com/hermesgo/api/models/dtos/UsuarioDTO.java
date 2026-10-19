package com.hermesgo.api.models.dtos;

import com.hermesgo.api.models.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo válido")
    private String correoElectronico;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    private String telefono;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    // --- NUEVOS CAMPOS PARA EL TRANSPORTADOR (R05) ---
    private String tipoVehiculo;
    private String placa;
    private Double capacidad;
    private Double calificacion;
    private Double precioBase;

    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(this.nombreCompleto);
        usuario.setCorreoElectronico(this.correoElectronico);
        usuario.setContrasena(this.contrasena);
        usuario.setTelefono(this.telefono);
        usuario.setRol(Usuario.Rol.valueOf(this.rol.toUpperCase()));
        
        // Asignamos los nuevos campos
        usuario.setTipoVehiculo(this.tipoVehiculo);
        usuario.setPlaca(this.placa);
        usuario.setCapacidad(this.capacidad);
        usuario.setCalificacion(this.calificacion);
        usuario.setPrecioBase(this.precioBase);
        
        return usuario;
    }
}