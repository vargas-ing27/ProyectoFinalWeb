package proyecto.barberos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.Service; 
import proyecto.barberos.entity.User;
// Importamos tu entidad Appointment
import proyecto.barberos.entity.Appointment; 

import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.ServiceRepository;
import proyecto.barberos.service.AppointmentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    // 1. Mostrar Calendario y Huecos Libres
    @GetMapping("/{barberId}/{serviceId}")
    public String mostrarCalendario(@PathVariable Long barberId, 
                                    @PathVariable Long serviceId,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                    HttpSession session, 
                                    Model model) {
        
        User cliente = (User) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/login";

        Optional<BarberProfile> barberOpt = barberProfileRepository.findById(barberId);
        Optional<Service> serviceOpt = serviceRepository.findById(serviceId);

        if (barberOpt.isPresent() && serviceOpt.isPresent()) {
            BarberProfile barbero = barberOpt.get();
            Service servicio = serviceOpt.get();

            if (fecha == null) {
                fecha = LocalDate.now();
            }

            List<LocalTime> huecos = appointmentService.obtenerHorasDisponibles(barbero, fecha, servicio.getDurationMinutes());

            model.addAttribute("barbero", barbero);
            model.addAttribute("servicio", servicio);
            model.addAttribute("fechaSeleccionada", fecha);
            model.addAttribute("huecos", huecos);
            
            model.addAttribute("usuario", cliente);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("isAdmin", "ADMIN".equals(cliente.getRole()));

            return "booking-date";
        }

        return "redirect:/home";
    }

    // 2. Confirmar Reserva
    @PostMapping("/confirm")
    public String confirmarReserva(@RequestParam Long barberId,
                                   @RequestParam Long serviceId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        
        User cliente = (User) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/login";

        Optional<BarberProfile> barberOpt = barberProfileRepository.findById(barberId);
        Optional<Service> serviceOpt = serviceRepository.findById(serviceId);

        if (barberOpt.isPresent() && serviceOpt.isPresent()) {
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
            appointmentService.agendarCita(cliente, barberOpt.get(), serviceOpt.get(), fechaHora);
            
            redirectAttributes.addFlashAttribute("exito", "¡Cita agendada con éxito!");
            // CAMBIO: Redirigimos a la nueva página de "Mis Reservas"
            return "redirect:/booking/mis-reservas"; 
        }

        return "redirect:/home";
    }

    // 3. Cancelar Cita
    @PostMapping("/cancel")
    public String cancelarCita(@RequestParam Long citaId, 
                               @RequestParam String motivo,
                               @RequestParam(required = false) String redirectUrl, // Para saber a dónde volver
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        User usuario = (User) session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/login";

        try {
            appointmentService.cancelarCita(citaId, motivo);
            redirectAttributes.addFlashAttribute("exito", "La cita ha sido cancelada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
        }

        // Si nos dicen a dónde volver (ej: /booking/mis-reservas), volvemos ahí.
        // Si no, por defecto al home (para el barbero).
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/home"; 
    }

    // --- 4. NUEVO: PÁGINA DE MIS RESERVAS (CLIENTE) ---
    @GetMapping("/mis-reservas")
    public String mostrarMisReservas(HttpSession session, Model model) {
        
        User cliente = (User) session.getAttribute("usuarioSesion");
        
        // Validar que esté logueado
        if (cliente == null) {
            return "redirect:/login"; 
        }

        // Obtener las citas del cliente
        List<Appointment> misCitas = appointmentService.obtenerCitasPorCliente(cliente.getId());
        
        // Pasar datos a la vista
        model.addAttribute("misCitas", misCitas);
        
        // Datos para la Navbar
        model.addAttribute("usuario", cliente);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("isAdmin", "ADMIN".equals(cliente.getRole()));

        return "client-reservations"; // Nombre de la nueva plantilla HTML
    }
}