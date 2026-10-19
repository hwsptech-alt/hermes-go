package com.hermesgo.api.controllers;

import com.hermesgo.api.models.dtos.UsuarioDTO;
import com.hermesgo.api.models.dtos.LoginDTO;
import com.hermesgo.api.models.Usuario;
import com.hermesgo.api.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // R01: Registro
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        if (usuarioRepository.findByCorreoElectronico(usuarioDTO.getCorreoElectronico()).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }
        Usuario usuario = usuarioDTO.toEntity();
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(usuarioGuardado);
    }

    // R02: Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoElectronico(loginDTO.getCorreoElectronico());
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.getContrasena().equals(loginDTO.getContrasena())) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        return ResponseEntity.ok(usuario);
    }

    // R03: Perfil
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPerfil(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        
        return ResponseEntity.ok(usuarioOpt.get());
    }

    // R05: Listar transportadores disponibles
    @GetMapping("/transportadores")
    public ResponseEntity<?> listarTransportadores() {
        List<Usuario> transportadores = usuarioRepository.findByRol(Usuario.Rol.TRANSPORTADOR);
        return ResponseEntity.ok(transportadores);
    }
}