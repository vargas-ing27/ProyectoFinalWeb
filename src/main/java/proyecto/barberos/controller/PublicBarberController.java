package proyecto.barberos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.Review; // Importante
import proyecto.barberos.entity.Service; 
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.ServiceRepository;
import proyecto.barberos.repository.UserRepository;
import proyecto.barberos.service.ReviewService; // Importamos el servicio

import java.util.List;
import java.util.Optional;

@Tag(name = "Barberos Públicos", description = "API para visualización pública de perfiles de barberos")
@Controller
@RequestMapping("/ver")
public class PublicBarberController {

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewService reviewService; // <--- Inyectamos el servicio de reseñas

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmailOrUsername(email, email).orElse(null);
        }
        return null;
    }

    @Operation(summary = "Ver detalle de barbero", description = "Muestra el perfil completo de un barbero con sus servicios y reseñas")
    @GetMapping("/barbero/{id}")
    public String verDetalleBarbero(@Parameter(description = "ID del barbero") @PathVariable Long id, Model model) {
        
        Optional<BarberProfile> perfilOpt = barberProfileRepository.findById(id);

        if (perfilOpt.isPresent()) {
            BarberProfile barbero = perfilOpt.get();
            
            // 1. Cargar Servicios
            List<Service> servicios = serviceRepository.findByBarberId(barbero.getId());

            // 2. Cargar Reseñas y Promedio
            List<Review> resenas = reviewService.obtenerResenasPorBarbero(barbero.getId());
            Double promedio = reviewService.calcularPromedioCalificacion(barbero.getId());

            // 3. Enviar todo a la vista
            model.addAttribute("barbero", barbero);
            model.addAttribute("servicios", servicios);
            model.addAttribute("resenas", resenas);   // Lista de comentarios
            model.addAttribute("promedio", promedio); // Número (ej: 4.5)

            // Lógica de Navbar (Usuario logueado)
            cargarDatosUsuario(model);

            return "barber-detail"; 
        }

        return "redirect:/home";
    }

    @Operation(summary = "Ver todos los barberos", description = "Muestra una lista con todos los barberos registrados")
    @GetMapping("/todos")
    public String verTodosLosBarberos(Model model) {
        List<BarberProfile> todos = barberProfileRepository.findAllByOrderByIdAsc();
        model.addAttribute("barberos", todos);
        cargarDatosUsuario(model);
        return "all-barbers"; 
    }

    private void cargarDatosUsuario(Model model) {
        User user = getAuthenticatedUser();
        if (user != null) {
            model.addAttribute("usuario", user);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}