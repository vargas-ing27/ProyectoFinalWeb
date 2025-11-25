package proyecto.barberos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.barberos.entity.BarberProfile;
import java.util.List; // Importante
import java.util.Optional;

@Repository
public interface BarberProfileRepository extends JpaRepository<BarberProfile, Long> {
    
    Optional<BarberProfile> findByUserId(Long userId);
    
    // --- NUEVO MÉTODO: Buscar TODOS ordenados por ID ---
    // Esto garantiza que el orden nunca cambie al editar
    List<BarberProfile> findAllByOrderByIdAsc();
}