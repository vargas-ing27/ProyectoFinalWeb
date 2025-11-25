package proyecto.barberos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.barberos.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
   // Esto permite buscar si coincide el email O si coincide el username
    Optional<User> findByEmailOrUsername(String email, String username);
    
    boolean existsByEmail(String email);
    
    // Nuevo: para evitar que dos personas se pongan el mismo usuario
    boolean existsByUsername(String username);
}