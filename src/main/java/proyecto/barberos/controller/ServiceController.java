package proyecto.barberos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.entity.Service;
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.ServiceRepository;
import proyecto.barberos.repository.UserRepository;
import proyecto.barberos.service.ServiciosService;

import java.util.List;
import java.util.Optional;

@Tag(name = "Servicios", description = "API para gestión de servicios de barbería")
@Controller
@RequestMapping("/barber/services")
public class ServiceController {

    @Autowired
    private ServiciosService serviciosService;

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

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

    @Operation(summary = "Gestionar servicios", description = "Muestra la página para agregar y listar servicios del barbero")
    @GetMapping
    public String gestionarServicios(Model model) {
        return cargarVistaServicios(model, new Service());
    }

    @Operation(summary = "Editar servicio", description = "Muestra el formulario para editar un servicio existente")
    @GetMapping("/edit/{id}")
    public String editarServicio(@Parameter(description = "ID del servicio") @PathVariable Long id, Model model) {
        Optional<Service> servicioOpt = serviceRepository.findById(id);
        
        if (servicioOpt.isPresent()) {
            return cargarVistaServicios(model, servicioOpt.get());
        }
        return "redirect:/barber/services";
    }

    // MÉTODO AUXILIAR PARA NO REPETIR CÓDIGO
    private String cargarVistaServicios(Model model, Service servicioFormulario) {
        User user = getAuthenticatedUser();
        
        // Seguridad: Solo barberos logueados
        if (user == null || !"BARBER".equals(user.getRole())) {
            return "redirect:/login";
        }

        Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
        
        if (perfilOpt.isPresent()) {
            BarberProfile perfil = perfilOpt.get();
            
            // Cargamos la lista de servicios
            List<Service> misServicios = serviciosService.obtenerServiciosPorBarbero(perfil.getId());
            
            model.addAttribute("servicios", misServicios);
            model.addAttribute("nuevoServicio", servicioFormulario);
            
            // --- AGREGA ESTO PARA QUE LA NAV FUNCIONE ---
            // Sin esto, el header dará error al intentar mostrar el nombre del usuario
            model.addAttribute("usuario", user);
            model.addAttribute("isLoggedIn", true);
            // -------------------------------------------
            
            return "barber-services"; 
        }
        
        return "redirect:/barber/setup";
    }

    @Operation(summary = "Agregar servicio", description = "Crea o actualiza un servicio para el barbero")
    @PostMapping("/add")
    public String agregarServicio(@ModelAttribute Service servicio) {
        User user = getAuthenticatedUser();
        
        if (user != null && "BARBER".equals(user.getRole())) {
            Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
            if (perfilOpt.isPresent()) {
                serviciosService.guardarServicio(servicio, perfilOpt.get());
            }
        }
        return "redirect:/barber/services";
    }

    @Operation(summary = "Eliminar servicio", description = "Elimina un servicio si no tiene citas agendadas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Servicio eliminado exitosamente"),
        @ApiResponse(responseCode = "302", description = "No se puede eliminar, tiene citas agendadas")
    })
    @GetMapping("/delete/{id}")
    public String eliminarServicio(@Parameter(description = "ID del servicio a eliminar") @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Intentamos borrar
            serviciosService.eliminarServicio(id);
            redirectAttributes.addFlashAttribute("exito", "Servicio eliminado correctamente.");
        } catch (Exception e) {
            // Si falla (por ejemplo, porque tiene citas agendadas), mostramos TU mensaje
            redirectAttributes.addFlashAttribute("error", "No puedes borrar el servicio, tienes citas agendadas.");
        }
        
        // Volvemos a la lista
        return "redirect:/barber/services";
    }
}