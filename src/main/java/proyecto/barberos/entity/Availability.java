package proyecto.barberos.entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalTime;

@Entity
@Table(name = "availabilities")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "barber_id", nullable = false)
    private BarberProfile barber;

    // Usaremos números para los días: 1=Lunes, 2=Martes... 7=Domingo
    @Column(nullable = false)
    private Integer dayOfWeek; 

    @Column(nullable = false)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime; // Hora inicio (Ej: 08:00)

    @Column(nullable = false)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;   // Hora fin (Ej: 18:00)

    // Para saber si ese horario está activo o si decidió no trabajar ese día temporalmente
    private boolean isActive = true; 

    public Availability() {}

    public Availability(BarberProfile barber, Integer dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.barber = barber;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BarberProfile getBarber() { return barber; }
    public void setBarber(BarberProfile barber) { this.barber = barber; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    // Método auxiliar para mostrar el nombre del día en el HTML
    public String getDayName() {
        switch(dayOfWeek) {
            case 1: return "Lunes";
            case 2: return "Martes";
            case 3: return "Miércoles";
            case 4: return "Jueves";
            case 5: return "Viernes";
            case 6: return "Sábado";
            case 7: return "Domingo";
            default: return "Desconocido";
        }
    }
}