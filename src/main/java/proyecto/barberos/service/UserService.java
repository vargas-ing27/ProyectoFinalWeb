package proyecto.barberos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // <--- Importante
import org.springframework.stereotype.Service;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.UserRepository;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // <--- Inyectamos la herramienta de encriptación

    public void actualizarUsuario(User user) {
    userRepository.save(user);
}

    // Lógica para registrar
    public User registrarUsuario(User user) throws Exception {
        if (userRepository.existsByEmail(user.getEmail())) {
             throw new Exception("El correo ya está registrado");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
             throw new Exception("El nombre de usuario ya está en uso");
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("CLIENT");
        }

        // --- CAMBIO AQUÍ: ENCRIPTAMOS ANTES DE GUARDAR ---
        String passwordEncriptada = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEncriptada);
        // -------------------------------------------------

        return userRepository.save(user);
    }

    // Lógica para Login
    public User autenticarUsuario(String identificador, String password) {
        Optional<User> usuario = userRepository.findByEmailOrUsername(identificador, identificador);

        if (usuario.isPresent()) {
            User userReal = usuario.get();
            
            // --- CAMBIO AQUÍ: USAMOS MATCHES ---
            // password: es la contraseña "12345" que escribió el usuario
            // userReal.getPassword(): es el hash "$2a$10$..." que está en la BD
            if (passwordEncoder.matches(password, userReal.getPassword())) {
                return userReal;
            }
            // -----------------------------------
        }
        return null;
    }
}