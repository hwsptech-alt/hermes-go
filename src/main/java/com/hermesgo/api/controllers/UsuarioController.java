package com.hermesgo.api.controllers;

import com.hermesgo.api.models.dtos.UsuarioDTO;
import com.hermesgo.api.models.dtos.LoginDTO;
import com.hermesgo.api.models.Usuario;
import com.hermesgo.api.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // R01: Registro de usuario
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        if (usuarioRepository.findByCorreoElectronico(usuarioDTO.getCorreoElectronico()).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }
        Usuario usuario = usuarioDTO.toEntity();
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(usuarioGuardado);
    }

    // R02: Inicio de sesión
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoElectronico(loginDTO.getCorreoElectronico());
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOpt.get();
        
        // Validación temporal de contraseña (luego la encriptaremos)
        if (!usuario.getContrasena().equals(loginDTO.getContrasena())) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        return ResponseEntity.ok(usuario);
    }
}