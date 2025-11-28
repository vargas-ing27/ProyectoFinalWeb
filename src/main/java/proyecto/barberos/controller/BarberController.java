package proyecto.barberos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.UserRepository;

import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Tag(name = "Barberos", description = "API para gestión de perfiles de barberos")
@Controller
@RequestMapping("/barber")
public class BarberController {

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmailOrUsername(email, email).orElse(null);
        }
        return null;
    }

    @Operation(summary = "Mostrar formulario de configuración", description = "Muestra el formulario para crear o editar perfil de barbero")
    @GetMapping("/setup")
    public String mostrarFormularioSetup(Model model) {
        User user = getAuthenticatedUser();
        
        if (user == null || !"BARBER".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Buscamos si ya tiene perfil
        Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());

        if (perfilOpt.isPresent()) {
            // SI YA TIENE: Pasamos sus datos para EDITAR
            model.addAttribute("perfil", perfilOpt.get());
        } else {
            // SI NO TIENE: Pasamos uno nuevo para CREAR
            model.addAttribute("perfil", new BarberProfile());
        }
        
        return "barber-setup"; 
    }

    @Operation(summary = "Guardar perfil de barbero", description = "Guarda o actualiza el perfil del barbero incluyendo foto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Perfil guardado exitosamente")
    })
    @PostMapping("/setup")
    public String guardarPerfil(@ModelAttribute BarberProfile perfil, 
                                @Parameter(description = "Archivo de imagen del perfil") @RequestParam("imagen") MultipartFile imagen) {
        
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        // LÓGICA PARA GUARDAR LA IMAGEN
        if (!imagen.isEmpty()) {
            // Ruta donde se guardará (src/main/resources/static/images)
            // Nota: Usamos una ruta relativa simple para empezar
            Path directorioImagenes = Paths.get("src//main//resources//static//images");
            String rutaAbsoluta = directorioImagenes.toFile().getAbsolutePath();

            try {
                // Leemos los bytes de la imagen y la escribimos en el disco
                byte[] bytesImg = imagen.getBytes();
                Path rutaCompleta = Paths.get(rutaAbsoluta + "//" + imagen.getOriginalFilename());
                Files.write(rutaCompleta, bytesImg);

                // Guardamos la URL relativa en la base de datos para que el HTML la pueda leer
                perfil.setProfileImageUrl("/images/" + imagen.getOriginalFilename());
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else {
            // Verificamos si ya existe un perfil en BD con ese ID
            if (perfil.getId() != null) {
                Optional<BarberProfile> perfilViejo = barberProfileRepository.findById(perfil.getId());
                if (perfilViejo.isPresent()) {
                    // ¡Rescatamos la URL de la foto anterior!
                    perfil.setProfileImageUrl(perfilViejo.get().getProfileImageUrl());
                }
            }
        }

        perfil.setUser(user);
        barberProfileRepository.save(perfil);

        return "redirect:/home";
    }
}