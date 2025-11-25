package proyecto.barberos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "barber_id", nullable = false)
    private BarberProfile barber; // Relación: Un servicio pertenece a UN barbero

    @Column(nullable = false)
    private String name; // Ej: "Corte Degradado"

    @Column(nullable = false)
    private Double price; // Ej: 25000

    @Column(nullable = false) // Importante para la agenda futura
    private Integer durationMinutes; // Ej: 45 (minutos)

    @Column(columnDefinition = "TEXT")
    private String description; // Ej: "Incluye lavado y perfilado"

    public Service() {}

    public Service(BarberProfile barber, String name, Double price, Integer durationMinutes, String description) {
        this.barber = barber;
        this.name = name;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.description = description;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BarberProfile getBarber() { return barber; }
    public void setBarber(BarberProfile barber) { this.barber = barber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}