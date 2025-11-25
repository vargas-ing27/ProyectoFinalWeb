package proyecto.barberos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.barberos.entity.Service;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    // Buscar todos los servicios de un barbero específico por su ID de Perfil
    List<Service> findByBarberId(Long barberId);
}