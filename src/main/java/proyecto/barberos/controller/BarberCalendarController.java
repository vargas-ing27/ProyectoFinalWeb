package proyecto.barberos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import proyecto.barberos.entity.Appointment;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.service.AppointmentService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/barber")
public class BarberCalendarController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @GetMapping("/calendar")
    public String verCalendario(HttpSession session, Model model) {
        User user = (User) session.getAttribute("usuarioSesion");
        
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
            model.addAttribute("isAdmin", "ADMIN".equals(user.getRole()));
            
            return "barber-calendar";
        }
        
        return "redirect:/barber/setup";
    }
}