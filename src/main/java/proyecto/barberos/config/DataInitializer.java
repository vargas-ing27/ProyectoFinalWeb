package proyecto.barberos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Verificamos si existe el admin por su username
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setFullName("Administrador Principal");
            admin.setUsername("admin");
            admin.setEmail("admin@mibarbero.com");
            admin.setPassword("admin123"); // Encriptaremos en el futuro
            admin.setRole("ADMIN");
            
            userRepository.save(admin);
            System.out.println("--- USUARIO ADMIN CREADO EXITOSAMENTE ---");
        }
    }
}