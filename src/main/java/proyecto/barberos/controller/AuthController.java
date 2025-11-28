package proyecto.barberos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import proyecto.barberos.service.ReviewService;
import proyecto.barberos.entity.Appointment; 
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
import proyecto.barberos.entity.Service; 
import proyecto.barberos.repository.BarberProfileRepository;
import proyecto.barberos.repository.UserRepository;
import proyecto.barberos.service.AppointmentService; 
import proyecto.barberos.service.ServiciosService; 
import proyecto.barberos.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Tag(name = "Autenticación", description = "API para registro, login y gestión de usuarios")
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
    private AppointmentService appointmentService; 

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

    @Operation(summary = "Mostrar página de registro", description = "Retorna la página de registro de nuevos usuarios")
    @GetMapping("/register")
    public String mostrarRegister(Model model) {
        model.addAttribute("usuario", new User());
        return "register";
    }

    @Operation(summary = "Procesar registro de usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Registro exitoso, redirige a login"),
        @ApiResponse(responseCode = "302", description = "Error en registro, redirige a register")
    })
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

    @Operation(summary = "Mostrar página de login", description = "Retorna la página de inicio de sesión")
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @Operation(summary = "Procesar login de usuario", description = "Autentica al usuario y genera token JWT")
    @PostMapping("/login")
    public String procesarLogin(@Parameter(description = "Identificador del usuario (email o username)") @RequestParam String identificador, 
                                @Parameter(description = "Contraseña del usuario") @RequestParam String password, 
                                HttpSession session,
                                jakarta.servlet.http.HttpServletResponse response, 
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
    
    @Operation(summary = "Página principal", description = "Muestra el home con información de barberos y citas")
    @GetMapping("/home")
    public String home(Model model) {
        
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

        // 2. Verificar autenticación REAL del usuario
        User user = getAuthenticatedUser();
        boolean isLoggedIn = (user != null);
        
        model.addAttribute("isLoggedIn", isLoggedIn);
        
        if (isLoggedIn) {
            model.addAttribute("usuario", user);

            // --- LÓGICA DE PANEL DEL BARBERO ---
            List<Appointment> misCitasBarbero = new ArrayList<>();
            if ("BARBER".equals(user.getRole())) {
                Optional<BarberProfile> perfilOpt = barberProfileRepository.findByUserId(user.getId());
                
                if (perfilOpt.isPresent()) {
                    // Datos del panel (Servicios y Citas)
                    List<Service> misServicios = serviciosService.obtenerServiciosPorBarbero(perfilOpt.get().getId());
                    misCitasBarbero = appointmentService.obtenerCitasPorBarbero(perfilOpt.get().getId());

                    Double promedio = reviewService.calcularPromedioCalificacion(perfilOpt.get().getId());

                    model.addAttribute("cantidadServicios", misServicios.size());
                    model.addAttribute("cantidadCitas", misCitasBarbero.size());
                    model.addAttribute("promedioCalificacion", promedio);

                } else {
                    model.addAttribute("cantidadServicios", 0);
                    model.addAttribute("cantidadCitas", 0);
                }
            }
            model.addAttribute("misCitas", misCitasBarbero);
        }

        return "home";
    }

    // Endpoint especial para modo invitado que ignora completamente la autenticación
    @Operation(summary = "Home como invitado", description = "Muestra el home como invitado ignorando cualquier sesión activa")
    @GetMapping("/guest")
    public String guestHome(Model model) {
        
        // 1. Cargar lista COMPLETA de barberos
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

        // 2. Forzar modo invitado completo
        model.addAttribute("isLoggedIn", false);
        model.addAttribute("isGuestMode", true);
        // NO agregar el objeto usuario

        return "home";
    }

    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario y elimina el token JWT")
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // 1. Invalidar sesión HTTP
        session.invalidate(); 
        
        // 2. Eliminar cookie JWT
        jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie("JWT_TOKEN", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Eliminar cookie inmediatamente
        response.addCookie(jwtCookie);
        
        // 3. Limpiar contexto de seguridad
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        // 4. Prevenir caché del navegador para seguridad
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        return "redirect:/login";
    }
}