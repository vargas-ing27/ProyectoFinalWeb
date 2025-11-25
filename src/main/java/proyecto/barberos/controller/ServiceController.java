package proyecto.barberos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.entity.Service; // Tu entidad Service
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.ServiceRepository;
import proyecto.barberos.service.ServiciosService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/barber/services")
public class ServiceController {

    @Autowired
    private ServiciosService serviciosService;

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    // 1. Mostrar la página (Modo Agregar)
    @GetMapping
    public String gestionarServicios(HttpSession session, Model model) {
        return cargarVistaServicios(session, model, new Service());
    }

    // 2. Mostrar la página (Modo Editar)
    @GetMapping("/edit/{id}")
    public String editarServicio(@PathVariable Long id, HttpSession session, Model model) {
        Optional<Service> servicioOpt = serviceRepository.findById(id);
        
        if (servicioOpt.isPresent()) {
            return cargarVistaServicios(session, model, servicioOpt.get());
        }
        return "redirect:/barber/services";
    }

    // MÉTODO AUXILIAR PARA NO REPETIR CÓDIGO
    private String cargarVistaServicios(HttpSession session, Model model, Service servicioFormulario) {
        User user = (User) session.getAttribute("usuarioSesion");
        
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
            model.addAttribute("isAdmin", "ADMIN".equals(user.getRole()));
            // -------------------------------------------
            
            return "barber-services"; 
        }
        
        return "redirect:/barber/setup";
    }

    // 3. Guardar (Sirve para CREAR y ACTUALIZAR)
    @PostMapping("/add")
    public String agregarServicio(@ModelAttribute Service servicio, HttpSession session) {
        User user = (User) session.getAttribute("usuarioSesion");
        
        if (user != null && "BARBER".equals(user.getRole())) {
            Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
            if (perfilOpt.isPresent()) {
                serviciosService.guardarServicio(servicio, perfilOpt.get());
            }
        }
        return "redirect:/barber/services";
    }

    // 4. Eliminar
    @GetMapping("/delete/{id}")
    public String eliminarServicio(@PathVariable Long id) {
        serviciosService.eliminarServicio(id);
        return "redirect:/barber/services";
    }
}