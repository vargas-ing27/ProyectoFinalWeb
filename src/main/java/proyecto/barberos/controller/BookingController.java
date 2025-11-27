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

    @Autowired
    private proyecto.barberos.service.UserService userService; // Necesitamos esto para guardar el usuario

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
                                   @RequestParam(required = false) String clientePhone, // <--- NUEVO CAMPO
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        
        User cliente = (User) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/login";

        // LÓGICA DE TELÉFONO: Si el usuario no tenía y nos lo envió ahora, lo guardamos
        if ((cliente.getPhone() == null || cliente.getPhone().isEmpty()) && clientePhone != null && !clientePhone.isEmpty()) {
            try {
                // Actualizamos el teléfono del cliente
                cliente.setPhone(clientePhone);
                userService.actualizarUsuario(cliente); // Necesitarás crear este método simple en UserService
                session.setAttribute("usuarioSesion", cliente); // Actualizamos la sesión
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "El teléfono ya está en uso por otro cliente.");
                return "redirect:/booking/" + barberId + "/" + serviceId;
            }
        }
        // Validación extra: Si sigue sin teléfono, no dejamos agendar
        else if (cliente.getPhone() == null || cliente.getPhone().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El teléfono es obligatorio para reservar.");
            return "redirect:/booking/" + barberId + "/" + serviceId;
        }

        // ... El resto de tu código de agendar cita sigue igual ...
        Optional<BarberProfile> barberOpt = barberProfileRepository.findById(barberId);
        Optional<Service> serviceOpt = serviceRepository.findById(serviceId);

        if (barberOpt.isPresent() && serviceOpt.isPresent()) {
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
            appointmentService.agendarCita(cliente, barberOpt.get(), serviceOpt.get(), fechaHora);
            
            redirectAttributes.addFlashAttribute("exito", "¡Cita agendada con éxito!");
            return "redirect:/booking/mis-reservas"; 
        }

        return "redirect:/home";
    }

  // 3. Cancelar Cita (Actualizado)
    @PostMapping("/cancel")
    public String cancelarCita(@RequestParam Long citaId, 
                               @RequestParam String motivo,
                               @RequestParam(required = false) String redirectUrl,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        User usuario = (User) session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/login";

        try {
            // Pasamos 'usuario' para saber a quién ocultársela
            appointmentService.cancelarCita(citaId, motivo, usuario);
            redirectAttributes.addFlashAttribute("exito", "Cancelación exitosa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        if (redirectUrl != null && !redirectUrl.isEmpty()) return "redirect:" + redirectUrl;
        return "redirect:/home"; 
    }

    // 4. NUEVO: Endpoint para Ocultar (La "X")
    @PostMapping("/hide")
    public String ocultarCita(@RequestParam Long citaId,
                              @RequestParam(required = false) String redirectUrl,
                              HttpSession session) {
        
        User usuario = (User) session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/login";

        appointmentService.ocultarCita(citaId, usuario);
        
        if (redirectUrl != null && !redirectUrl.isEmpty()) return "redirect:" + redirectUrl;
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