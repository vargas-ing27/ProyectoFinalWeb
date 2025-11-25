package proyecto.barberos.service;

import org.springframework.beans.factory.annotation.Autowired;
// NO IMPORTAMOS org.springframework.stereotype.Service AQUÍ PARA EVITAR CONFLICTOS

import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.Service; // <--- Esta es TU Entidad (la tabla)
import proyecto.barberos.repository.ServiceRepository;
import java.util.List; // <--- Faltaba este import para las Listas

// Usamos el nombre largo aquí para que Java no se confunda
@org.springframework.stereotype.Service 
public class ServiciosService {

    @Autowired
    private ServiceRepository serviceRepository;

    // Guardar un nuevo servicio
    public void guardarServicio(Service servicio, BarberProfile barbero) {
        servicio.setBarber(barbero); 
        serviceRepository.save(servicio);
    }

    // Listar servicios de un barbero
    public List<Service> obtenerServiciosPorBarbero(Long barberId) {
        return serviceRepository.findByBarberId(barberId);
    }

    // Eliminar servicio
    public void eliminarServicio(Long id) {
        serviceRepository.deleteById(id);
    }
}