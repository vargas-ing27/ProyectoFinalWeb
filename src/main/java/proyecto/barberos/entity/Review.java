package proyecto.barberos.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quién hace la reseña (El Cliente)
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // A quién califican (El Barbero)
    @ManyToOne
    @JoinColumn(name = "barber_id", nullable = false)
    private BarberProfile barber;

    @Column(nullable = false)
    private Integer rating; // 1 a 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    public Review() {}

    // Constructor auxiliar
    public Review(User client, BarberProfile barber, Integer rating, String comment) {
        this.client = client;
        this.barber = barber;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = new Date();
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }

    public BarberProfile getBarber() { return barber; }
    public void setBarber(BarberProfile barber) { this.barber = barber; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}