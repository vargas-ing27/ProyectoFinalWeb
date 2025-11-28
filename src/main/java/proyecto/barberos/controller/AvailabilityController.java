package proyecto.barberos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import proyecto.barberos.dto.AvailabilityWrapper;
import proyecto.barberos.entity.Availability;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.AvailabilityRepository;
import proyecto.barberos.repository.UserRepository;
import proyecto.barberos.service.AvailabilityService;

import java.util.List;
import java.util.Optional;

@Tag(name = "Disponibilidad", description = "API para gestión de horarios y disponibilidad de barberos")
@Controller
@RequestMapping("/barber/schedule")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private BarberProfileRepository barberProfileRepository;
    
    @Autowired
    private AvailabilityRepository availabilityRepository;

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

    @Operation(summary = "Ver horarios", description = "Muestra la página con los horarios de trabajo del barbero")
    @GetMapping
    public String verHorarios(Model model) {
        User user = getAuthenticatedUser();
        
        // Seguridad: Solo barberos logueados
        if (user == null || !"BARBER".equals(user.getRole())) {
            return "redirect:/login";
        }

        Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
        
        if (perfilOpt.isPresent()) {
            // Obtenemos los horarios desde el servicio
            List<Availability> horarios = availabilityService.obtenerHorarios(perfilOpt.get());
            
            // Metemos la lista en el envoltorio (Wrapper) para el formulario
            AvailabilityWrapper wrapper = new AvailabilityWrapper();
            wrapper.setHorarios(horarios);
            
            model.addAttribute("wrapper", wrapper);
            
            // --- DATOS PARA LA NAVBAR ---
            model.addAttribute("usuario", user); 
            model.addAttribute("isLoggedIn", true);
            // ----------------------------
            
            return "barber-schedule"; 
        }
        
        return "redirect:/barber/setup";
    }

    @Operation(summary = "Guardar horarios", description = "Guarda todos los horarios de la semana de una vez")
    @PostMapping("/saveAll")
    public String guardarTodo(@ModelAttribute AvailabilityWrapper wrapper) {
        
        if (wrapper.getHorarios() != null) {
            for (Availability formDay : wrapper.getHorarios()) {
                // Verificamos que el día tenga ID para saber cuál actualizar
                if (formDay.getId() != null) {
                    
                    // 1. Buscamos el registro ORIGINAL en la BD
                    // Esto es vital para no perder la referencia al Barbero (barber_id)
                    Optional<Availability> dbDayOpt = availabilityRepository.findById(formDay.getId());
                    
                    if (dbDayOpt.isPresent()) {
                        Availability dbDay = dbDayOpt.get();
                        
                        // 2. Actualizamos SOLO los campos editables (Hora y Estado)
                        dbDay.setStartTime(formDay.getStartTime());
                        dbDay.setEndTime(formDay.getEndTime());
                        dbDay.setActive(formDay.isActive());
                        
                        // 3. Guardamos el objeto actualizado
                        availabilityService.guardarHorario(dbDay);
                    }
                }
            }
        }
        return "redirect:/barber/schedule";
    }

    @Operation(summary = "Reiniciar horarios", description = "Restablece los horarios a los valores por defecto")
    @PostMapping("/reset")
    public String reiniciarHorarios() {
        User user = getAuthenticatedUser();
        
        if (user != null && "BARBER".equals(user.getRole())) {
            Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
            
            // Si existe el perfil, llamamos al servicio para reiniciar
            if (perfilOpt.isPresent()) {
                availabilityService.reiniciarHorarios(perfilOpt.get());
            }
        }
        return "redirect:/barber/schedule"; // Recargamos la página limpia
    }
}