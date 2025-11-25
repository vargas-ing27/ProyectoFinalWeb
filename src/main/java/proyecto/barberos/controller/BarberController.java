package proyecto.barberos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Importante para subir archivos
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.BarberProfileRepository;

import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/barber")
public class BarberController {

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    // 1. Mostrar el formulario (Crear o Editar)
    @GetMapping("/setup")
    public String mostrarFormularioSetup(HttpSession session, Model model) {
        User user = (User) session.getAttribute("usuarioSesion");
        
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

    // 2. Guardar perfil CON FOTO
    @PostMapping("/setup")
    public String guardarPerfil(@ModelAttribute BarberProfile perfil, 
                                @RequestParam("imagen") MultipartFile imagen, // <-- Recibimos el archivo
                                HttpSession session) {
        
        User user = (User) session.getAttribute("usuarioSesion");
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

        perfil.setUser(user);
        barberProfileRepository.save(perfil);

        return "redirect:/home";
    }
}