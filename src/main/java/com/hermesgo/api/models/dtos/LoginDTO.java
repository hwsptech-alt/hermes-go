package com.hermesgo.api.models.dtos;

import lombok.Data;

@Data
public class LoginDTO {
    private String correoElectronico;
    private String contrasena;
}