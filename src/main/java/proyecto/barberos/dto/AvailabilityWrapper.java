package proyecto.barberos.dto;

import proyecto.barberos.entity.Availability;
import java.util.List;

public class AvailabilityWrapper {
    
    private List<Availability> horarios;

    // Constructor vacío
    public AvailabilityWrapper() {}

    // Getters y Setters
    public List<Availability> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<Availability> horarios) {
        this.horarios = horarios;
    }
}