package proyecto.barberos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.barberos.entity.Review;
import java.util.List;
import java.util.Optional; // <--- Importante

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByBarberId(Long barberId);

    // NUEVO: Buscar si existe una reseña específica de un cliente para un barbero
    Optional<Review> findByClientIdAndBarberId(Long clientId, Long barberId);
}