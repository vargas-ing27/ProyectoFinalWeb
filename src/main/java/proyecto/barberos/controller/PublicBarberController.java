package proyecto.barberos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.Review; // Importante
import proyecto.barberos.entity.Service; 
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.ServiceRepository;
import proyecto.barberos.service.ReviewService; // Importamos el servicio

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/ver")
public class PublicBarberController {

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ReviewService reviewService; // <--- Inyectamos el servicio de reseñas

    // Detalle individual del barbero
    @GetMapping("/barbero/{id}")
    public String verDetalleBarbero(@PathVariable Long id, HttpSession session, Model model) {
        
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
            cargarDatosUsuario(session, model);

            return "barber-detail"; 
        }

        return "redirect:/home";
    }

    // Ver todos los barberos
    @GetMapping("/todos")
    public String verTodosLosBarberos(HttpSession session, Model model) {
        List<BarberProfile> todos = barberProfileRepository.findAllByOrderByIdAsc();
        model.addAttribute("barberos", todos);
        cargarDatosUsuario(session, model);
        return "all-barbers"; 
    }

    private void cargarDatosUsuario(HttpSession session, Model model) {
        User user = (User) session.getAttribute("usuarioSesion");
        if (user != null) {
            model.addAttribute("usuario", user);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("isAdmin", "ADMIN".equals(user.getRole()));
        } else {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("isAdmin", false);
        }
    }
}