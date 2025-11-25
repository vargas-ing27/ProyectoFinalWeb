package proyecto.barberos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.barberos.entity.Availability;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    // Buscar horarios por el ID del Barbero, ordenados por día de la semana (Lunes a Domingo)
    List<Availability> findByBarberIdOrderByDayOfWeekAsc(Long barberId);
}