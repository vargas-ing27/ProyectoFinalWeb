package proyecto.barberos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // <--- Este incluye @GetMapping, @PostMapping, @PathVariable
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable; // Importante

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.entity.Review;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.service.ReviewService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @PostMapping("/add")
    public String agregarResena(@RequestParam Long barberId,
                                @RequestParam(required = false) Integer rating, // Opcional
                                @RequestParam(required = false) String comment, // Opcional
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        User cliente = (User) session.getAttribute("usuarioSesion");
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

@PostMapping("/delete/{reviewId}")
    public String eliminarResena(@PathVariable Long reviewId, 
                                 @RequestParam Long barberId,
                                 HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        
        User cliente = (User) session.getAttribute("usuarioSesion");
        
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

    @GetMapping("/mis-resenas")
    public String verMisResenas(HttpSession session, Model model) {
        User user = (User) session.getAttribute("usuarioSesion");
        
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