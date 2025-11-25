package proyecto.barberos.service;

import jakarta.transaction.Transactional; // Importante para transacciones seguras
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyecto.barberos.entity.Availability;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.repository.AvailabilityRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Transactional
    public List<Availability> obtenerHorarios(BarberProfile barbero) {
        // Buscamos los horarios existentes del barbero
        List<Availability> horarios = availabilityRepository.findByBarberIdOrderByDayOfWeekAsc(barbero.getId());

        // Si el barbero es nuevo y no tiene horarios creados, se los creamos por defecto
        if (horarios.isEmpty()) {
            horarios = inicializarHorariosPorDefecto(barbero);
        }
        return horarios;
    }

    public void guardarHorario(Availability horario) {
        availabilityRepository.save(horario);
    }

    // Crea Lunes a Domingo de 8:00 AM a 6:00 PM por defecto
    @Transactional
    private List<Availability> inicializarHorariosPorDefecto(BarberProfile barbero) {
        List<Availability> nuevosHorarios = new ArrayList<>();
        for (int i = 1; i <= 7; i++) { // 1=Lunes ... 7=Domingo
            Availability dia = new Availability();
            dia.setBarber(barbero);
            dia.setDayOfWeek(i);
            
            // CONFIGURACIÓN POR DEFECTO: 8:00 AM - 6:00 PM
            dia.setStartTime(LocalTime.of(8, 0)); 
            dia.setEndTime(LocalTime.of(18, 0));  
            dia.setActive(true); // Abierto por defecto
            
            availabilityRepository.save(dia); 
            nuevosHorarios.add(dia);
        }
        return nuevosHorarios;
    }

    // NUEVO MÉTODO: Restablecer los horarios existentes a los valores por defecto
    // Esto es lo que llama el botón "Restablecer Horario"
    @Transactional
    public void reiniciarHorarios(BarberProfile barbero) {
        List<Availability> horarios = availabilityRepository.findByBarberIdOrderByDayOfWeekAsc(barbero.getId());
        
        for (Availability dia : horarios) {
            dia.setStartTime(LocalTime.of(8, 0)); // Volver a 8:00 AM
            dia.setEndTime(LocalTime.of(18, 0));  // Volver a 6:00 PM
            dia.setActive(true);                  // Volver a Abierto
            availabilityRepository.save(dia);
        }
    }
}