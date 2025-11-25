package proyecto.barberos.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import proyecto.barberos.service.ReviewService;
import proyecto.barberos.entity.Appointment; // <--- NUEVO IMPORT
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.entity.Service; 
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.service.AppointmentService; // <--- NUEVO IMPORT
import proyecto.barberos.service.ServiciosService; 
import proyecto.barberos.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
private proyecto.barberos.security.JwtUtil jwtUtil;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @Autowired
    private ServiciosService serviciosService; 
    
    @Autowired
    private AppointmentService appointmentService; // <--- Inyectamos el servicio de citas

    // ================== REGISTRO ==================
    @GetMapping("/register")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new User());
        return "register";
    }

    @PostMapping("/register")
    public String procesarRegistro(@ModelAttribute("usuario") User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registrarUsuario(user);
            redirectAttributes.addFlashAttribute("exito", "¡Registro exitoso! Por favor inicia sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // ================== LOGIN ==================
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

   @PostMapping("/login")
    public String procesarLogin(@RequestParam String identificador, 
                                @RequestParam String password, 
                                HttpSession session,
                                jakarta.servlet.http.HttpServletResponse response, // <--- NUEVO PARÁMETRO
                                RedirectAttributes redirectAttributes) {
        
        User usuarioLogueado = userService.autenticarUsuario(identificador, password);

        if (usuarioLogueado != null) {
            // 1. Mantener la sesión tradicional (por si acaso, para compatibilidad con lo que ya tienes)
            session.setAttribute("usuarioSesion", usuarioLogueado);

            // 2. GENERAR EL JWT
            String token = jwtUtil.generateToken(usuarioLogueado);

            // 3. GUARDARLO EN UNA COOKIE
            jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(true); // Seguridad: JavaScript no puede leerla
            jwtCookie.setPath("/"); // Disponible en toda la página
            jwtCookie.setMaxAge(60 * 60 * 10); // 10 horas
            response.addCookie(jwtCookie);

            // --- Lógica de Redirección (IGUAL QUE ANTES) ---
            if ("BARBER".equals(usuarioLogueado.getRole())) {
                if (barberProfileRepository.findByUserId(usuarioLogueado.getId()).isPresent()) {
                    return "redirect:/home"; 
                } else {
                    return "redirect:/barber/setup"; 
                }
            } else {
                return "redirect:/home";
            }

        } else {
            redirectAttributes.addFlashAttribute("error", "Credenciales incorrectas.");
            return "redirect:/login";
        }
    }
    
    // ================== HOME (CON CITAS) ==================
    
@GetMapping("/home")
    public String home(HttpSession session, Model model) {
        // 1. Cargar lista COMPLETA de barberos (Para la sección de ver barberos)
        List<BarberProfile> todosLosBarberos = barberProfileRepository.findAllByOrderByIdAsc();
        
        // Lógica para limitar a 4 en el home
        List<BarberProfile> barberosParaMostrar;
        boolean hayMasBarberos = false;

        if (todosLosBarberos.size() > 4) {
            barberosParaMostrar = todosLosBarberos.subList(0, 4);
            hayMasBarberos = true; 
        } else {
            barberosParaMostrar = todosLosBarberos;
        }

        model.addAttribute("barberos", barberosParaMostrar);
        model.addAttribute("mostrarBotonVerTodos", hayMasBarberos); 

        // 2. Verificar usuario logueado
        User user = (User) session.getAttribute("usuarioSesion");

        if (user != null) {
            model.addAttribute("usuario", user);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("isAdmin", "ADMIN".equals(user.getRole()));

            // --- LÓGICA DE PANEL DEL BARBERO (SE MANTIENE) ---
            List<Appointment> misCitasBarbero = new ArrayList<>();
            if ("BARBER".equals(user.getRole())) {
                Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
                
                if (perfilOpt.isPresent()) {
                    // Datos del panel (Servicios y Citas)
                    List<Service> misServicios = serviciosService.obtenerServiciosPorBarbero(perfilOpt.get().getId());
                    misCitasBarbero = appointmentService.obtenerCitasPorBarbero(perfilOpt.get().getId());

                    Double promedio = reviewService.calcularPromedioCalificacion(perfilOpt.get().getId());

                    model.addAttribute("cantidadServicios", misServicios.size());
                    model.addAttribute("cantidadCitas", misCitasBarbero.size()); // Para el contador del panel

                    model.addAttribute("promedioCalificacion", promedio);

                } else {
                    model.addAttribute("cantidadServicios", 0);
                    model.addAttribute("cantidadCitas", 0);

                }
            }
            // Solo enviamos las citas si es barbero, para su panel
            model.addAttribute("misCitas", misCitasBarbero);
            // -----------------------------------

        } else {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("isAdmin", false);
        }

        return "home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/login";
    }
}