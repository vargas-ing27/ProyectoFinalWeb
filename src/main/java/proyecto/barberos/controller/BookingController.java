package proyecto.barberos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.Service; 
import proyecto.barberos.entity.User;
// Importamos tu entidad Appointment
import proyecto.barberos.entity.Appointment; 

import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.ServiceRepository;
import proyecto.barberos.repository.UserRepository;
import proyecto.barberos.service.AppointmentService;
import proyecto.barberos.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Tag(name = "Reservas", description = "API para gestión de citas y reservas")
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
    private UserService userService; 

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Buscar usuario por email desde el contexto de Spring Security
            String email = authentication.getName();
            return userRepository.findByEmailOrUsername(email, email).orElse(null);
        }
        return null;
    }

    @Operation(summary = "Mostrar calendario de disponibilidad", description = "Muestra las fechas y horas disponibles para reservar con un barbero")
    @GetMapping("/{barberId}/{serviceId}")
    public String mostrarCalendario(@Parameter(description = "ID del barbero") @PathVariable Long barberId, 
                                    @Parameter(description = "ID del servicio") @PathVariable Long serviceId,
                                    @Parameter(description = "Fecha seleccionada (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                    Model model) {
        
        User cliente = getAuthenticatedUser();
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


            return "booking-date";
        }

        return "redirect:/home";
    }

    @Operation(summary = "Confirmar reserva", description = "Confirma y agenda una cita para el cliente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Cita agendada exitosamente"),
        @ApiResponse(responseCode = "302", description = "Error al agendar cita")
    })
    @PostMapping("/confirm")
    public String confirmarReserva(@Parameter(description = "ID del barbero") @RequestParam Long barberId,
                                   @Parameter(description = "ID del servicio") @RequestParam Long serviceId,
                                   @Parameter(description = "Fecha de la cita") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                   @Parameter(description = "Hora de la cita") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
                                   @Parameter(description = "Teléfono del cliente (opcional)") @RequestParam(required = false) String clientePhone,
                                   RedirectAttributes redirectAttributes) {
        
        User cliente = getAuthenticatedUser();
        if (cliente == null) return "redirect:/login";

        // LÓGICA DE TELÉFONO: Si el usuario no tenía y nos lo envió ahora, lo guardamos
        if ((cliente.getPhone() == null || cliente.getPhone().isEmpty()) && clientePhone != null && !clientePhone.isEmpty()) {
            try {
                // Actualizamos el teléfono del cliente
                cliente.setPhone(clientePhone);
                userService.actualizarUsuario(cliente); 
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

    @Operation(summary = "Cancelar cita", description = "Cancela una cita existente con un motivo")
    @PostMapping("/cancel")
    public String cancelarCita(@Parameter(description = "ID de la cita") @RequestParam Long citaId, 
                               @Parameter(description = "Motivo de cancelación") @RequestParam String motivo,
                               @Parameter(description = "URL de redirección (opcional)") @RequestParam(required = false) String redirectUrl,
                               RedirectAttributes redirectAttributes) {
        
        User usuario = getAuthenticatedUser();
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

    @Operation(summary = "Ocultar cita", description = "Oculta una cita de la vista del usuario")
    @PostMapping("/hide")
    public String ocultarCita(@Parameter(description = "ID de la cita") @RequestParam Long citaId,
                              @Parameter(description = "URL de redirección (opcional)") @RequestParam(required = false) String redirectUrl) {
        
        User usuario = getAuthenticatedUser();
        if (usuario == null) return "redirect:/login";

        appointmentService.ocultarCita(citaId, usuario);
        
        if (redirectUrl != null && !redirectUrl.isEmpty()) return "redirect:" + redirectUrl;
        return "redirect:/home";
    }

    @Operation(summary = "Mis reservas", description = "Muestra todas las citas del cliente logueado")
    @GetMapping("/mis-reservas")
    public String mostrarMisReservas(Model model) {
        
        User cliente = getAuthenticatedUser();
        if (cliente == null) return "redirect:/login";

        // Obtener las citas del cliente
        List<Appointment> misCitas = appointmentService.obtenerCitasPorCliente(cliente.getId());
        
        // Pasar datos a la vista
        model.addAttribute("misCitas", misCitas);
        
        // Datos para la Navbar
        model.addAttribute("usuario", cliente);
        model.addAttribute("isLoggedIn", true);

        return "client-reservations"; // Nombre de la nueva plantilla HTML
    }
}