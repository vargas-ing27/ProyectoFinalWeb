package proyecto.barberos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import proyecto.barberos.dto.AvailabilityWrapper;
import proyecto.barberos.entity.Availability;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.AvailabilityRepository; 
import proyecto.barberos.service.AvailabilityService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/barber/schedule")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private BarberProfileRepository barberProfileRepository;
    
    @Autowired
    private AvailabilityRepository availabilityRepository;

    // 1. Ver la Agenda
    @GetMapping
    public String verHorarios(HttpSession session, Model model) {
        User user = (User) session.getAttribute("usuarioSesion");
        
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
            model.addAttribute("isAdmin", "ADMIN".equals(user.getRole()));
            // ----------------------------
            
            return "barber-schedule"; 
        }
        
        return "redirect:/barber/setup";
    }

    // 2. Guardar TODO la semana de una vez (Lógica Segura)
    @PostMapping("/saveAll")
    public String guardarTodo(@ModelAttribute AvailabilityWrapper wrapper, HttpSession session) {
        
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

    // 3. Botón de Pánico: Restablecer Horarios por Defecto
    @PostMapping("/reset")
    public String reiniciarHorarios(HttpSession session) {
        User user = (User) session.getAttribute("usuarioSesion");
        
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