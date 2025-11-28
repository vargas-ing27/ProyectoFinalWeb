package proyecto.barberos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import proyecto.barberos.entity.Appointment;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.UserRepository;
import proyecto.barberos.service.AppointmentService;

import java.util.List;
import java.util.Optional;

@Tag(name = "Calendario", description = "API para visualización del calendario de barberos")
@Controller
@RequestMapping("/barber")
public class BarberCalendarController {

    @Autowired
    private AppointmentService appointmentService;

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

    @Operation(summary = "Ver calendario", description = "Muestra el calendario con todas las citas del barbero")
    @GetMapping("/calendar")
    public String verCalendario(Model model) {
        User user = getAuthenticatedUser();
        
        // Seguridad
        if (user == null || !"BARBER".equals(user.getRole())) {
            return "redirect:/login";
        }

        Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
        
        if (perfilOpt.isPresent()) {
            // Obtener citas del barbero
            List<Appointment> misCitas = appointmentService.obtenerCitasPorBarbero(perfilOpt.get().getId());
            model.addAttribute("citas", misCitas);
            
            // Datos Navbar
            model.addAttribute("usuario", user);
            model.addAttribute("isLoggedIn", true);
            
            return "barber-calendar";
        }
        
        return "redirect:/barber/setup";
    }
}