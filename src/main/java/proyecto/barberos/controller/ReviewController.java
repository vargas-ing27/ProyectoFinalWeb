package proyecto.barberos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.entity.Review;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.UserRepository;
import proyecto.barberos.service.ReviewService;

import java.util.List;
import java.util.Optional;

@Tag(name = "Reseñas", description = "API para gestión de reseñas y calificaciones")
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

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

    @Operation(summary = "Agregar reseña", description = "Agrega una calificación y/o comentario para un barbero")
    @PostMapping("/add")
    public String agregarResena(@Parameter(description = "ID del barbero") @RequestParam Long barberId,
                                @Parameter(description = "Calificación (1-5)") @RequestParam(required = false) Integer rating,
                                @Parameter(description = "Comentario del cliente") @RequestParam(required = false) String comment,
                                RedirectAttributes redirectAttributes) {
        
        User cliente = getAuthenticatedUser();
        if (cliente == null) return "redirect:/login";

        Optional<BarberProfile> barberOpt = barberProfileRepository.findById(barberId);
        
        if (barberOpt.isPresent()) {
            // Llamamos a nuestro método inteligente
            reviewService.gestionarResena(cliente, barberOpt.get(), rating, comment);
            
            if (rating != null) redirectAttributes.addFlashAttribute("exito", "¡Calificación enviada!");
            else redirectAttributes.addFlashAttribute("exito", "¡Opinión publicada!");
        }
        
        return "redirect:/ver/barbero/" + barberId;
    }

    @Operation(summary = "Eliminar reseña", description = "Elimina una reseña existente del cliente")
    @PostMapping("/delete/{reviewId}")
    public String eliminarResena(@Parameter(description = "ID de la reseña") @PathVariable Long reviewId, 
                                 @Parameter(description = "ID del barbero") @RequestParam Long barberId,
                                 RedirectAttributes redirectAttributes) {
        
        User cliente = getAuthenticatedUser();
        
        // 1. Seguridad: Debe estar logueado
        if (cliente == null) {
            return "redirect:/login";
        }

        // 2. Llamar al servicio para eliminar
        boolean eliminado = reviewService.eliminarResena(reviewId, cliente.getId());

        if (eliminado) {
            redirectAttributes.addFlashAttribute("exito", "Tu calificación/opinión ha sido eliminada.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la reseña.");
        }

        // 3. Redirigir de vuelta al perfil del barbero
        return "redirect:/ver/barbero/" + barberId;
    }

    @Operation(summary = "Ver mis reseñas", description = "Muestra todas las reseñas del barbero logueado")
    @GetMapping("/mis-resenas")
    public String verMisResenas(Model model) {
        User user = getAuthenticatedUser();
        
        // Seguridad: Solo barberos
        if (user == null || !"BARBER".equals(user.getRole())) {
            return "redirect:/login";
        }

        Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
        
        if (perfilOpt.isPresent()) {
            BarberProfile barbero = perfilOpt.get();
            
            // Obtenemos todas las reseñas
            List<Review> resenas = reviewService.obtenerResenasPorBarbero(barbero.getId());
            Double promedio = reviewService.calcularPromedioCalificacion(barbero.getId());

            model.addAttribute("resenas", resenas);
            model.addAttribute("promedio", promedio);
            
            // Datos Navbar
            model.addAttribute("usuario", user);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("isAdmin", false);
            
            return "barber-reviews"; // Nueva página
        }
        
        return "redirect:/home";
    }

}