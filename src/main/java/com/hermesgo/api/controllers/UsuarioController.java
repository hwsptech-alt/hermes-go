package com.hermesgo.api.controllers;

import com.hermesgo.api.models.dtos.UsuarioDTO;
import com.hermesgo.api.models.dtos.LoginDTO;
import com.hermesgo.api.models.Usuario;
import com.hermesgo.api.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
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

    // R03: Subir foto de perfil
    @PostMapping("/{id}/foto")
    public ResponseEntity<?> subirFoto(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            // Crear carpeta uploads si no existe
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Guardar archivo con nombre único
            String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
            Path ruta = uploadDir.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), ruta);

            // Actualizar la URL en la base de datos
            Usuario usuario = usuarioOpt.get();
            usuario.setFotoUrl("http://localhost:8080/uploads/" + nombreArchivo);
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(usuario);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al subir la foto: " + e.getMessage());
        }
    }
}