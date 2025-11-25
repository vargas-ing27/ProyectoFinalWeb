package proyecto.barberos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Importamos la configuración de seguridad para poder excluirla
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// EL CAMBIO CLAVE ESTÁ AQUÍ ABAJO:
// Le decimos a Spring Boot que NO active la pantalla de login por defecto.
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class BarberosApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarberosApplication.class, args);
    }

}