package proyecto.barberos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import proyecto.barberos.entity.Availability;
import java.util.List;

@Schema(description = "Contenedor para múltiples horarios de disponibilidad")
public class AvailabilityWrapper {
    
    @Schema(description = "Lista de horarios de disponibilidad del barbero")
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