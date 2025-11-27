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

    // Buscar citas para calcular huecos (Se mantiene igual)
    @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId AND a.appointmentDate BETWEEN :start AND :end")
    List<Appointment> findByBarberIdAndDateRange(@Param("barberId") Long barberId, 
                                                 @Param("start") LocalDateTime start, 
                                                 @Param("end") LocalDateTime end);

    // --- MÉTODOS ACTUALIZADOS CON FILTRO DE TIEMPO ---

    // 2. Para el CLIENTE: Visible + Futuras (o recientes de hace 5 min)
    List<Appointment> findByClientIdAndVisibleToClientTrueAndAppointmentDateAfterOrderByAppointmentDateDesc(Long clientId, LocalDateTime fechaCorte);
    
    // 3. Para el BARBERO: Visible + Futuras (o recientes de hace 5 min)
    List<Appointment> findByBarberIdAndVisibleToBarberTrueAndAppointmentDateAfterOrderByAppointmentDateDesc(Long barberId, LocalDateTime fechaCorte);
}