package proyecto.barberos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import proyecto.barberos.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 1. Buscar citas de un barbero en un rango de fecha/hora específico
    // (Vital para calcular huecos libres y evitar cruces)
    @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId AND a.appointmentDate BETWEEN :start AND :end")
    List<Appointment> findByBarberIdAndDateRange(@Param("barberId") Long barberId, 
                                                 @Param("start") LocalDateTime start, 
                                                 @Param("end") LocalDateTime end);

    // 2. Ver historial de citas de un CLIENTE (Para la sección "Mis Citas")
    List<Appointment> findByClientIdOrderByAppointmentDateDesc(Long clientId);
    
    // 3. Ver agenda de un BARBERO (Para su panel de control)
    List<Appointment> findByBarberIdOrderByAppointmentDateDesc(Long barberId);
}