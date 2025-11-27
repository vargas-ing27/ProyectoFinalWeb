package proyecto.barberos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.Review;
import proyecto.barberos.entity.User;
import proyecto.barberos.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // MÉTODO INTELIGENTE: GUARDAR O ACTUALIZAR
    public void gestionarResena(User cliente, BarberProfile barbero, Integer rating, String comment) {
        
        // 1. Buscamos si ya existe una reseña de este usuario para este barbero
        Optional<Review> reseñaExistente = reviewRepository.findByClientIdAndBarberId(cliente.getId(), barbero.getId());

        if (reseñaExistente.isPresent()) {
            // --- SI YA EXISTE, ACTUALIZAMOS ---
            Review review = reseñaExistente.get();
            
            // Si mandó rating, lo actualizamos
            if (rating != null) {
                review.setRating(rating);
            }
            // Si mandó comentario, lo actualizamos
            if (comment != null && !comment.isEmpty()) {
                review.setComment(comment);
            }
            reviewRepository.save(review);

        } else {
            // --- SI NO EXISTE, CREAMOS UNA NUEVA ---
            Review nuevaReview = new Review();
            nuevaReview.setClient(cliente);
            nuevaReview.setBarber(barbero);
            
            // Si rating es null, ponemos 0 (o null, según prefieras, pero 0 es más fácil de manejar en la vista)
            nuevaReview.setRating(rating != null ? rating : 0);
            
            // Si mandó comentario
            nuevaReview.setComment(comment != null ? comment : "");
            
            reviewRepository.save(nuevaReview);
        }
    }

    public List<Review> obtenerResenasPorBarbero(Long barberId) {
        return reviewRepository.findByBarberId(barberId);
    }

    public Double calcularPromedioCalificacion(Long barberId) {
        List<Review> reviews = reviewRepository.findByBarberId(barberId);
        if (reviews.isEmpty()) return 0.0;
        
        double suma = 0;
        // Solo sumamos reseñas que tengan calificación válida (> 0)
        int contadorValido = 0;
        
        for(Review r : reviews) {
            if(r.getRating() != null && r.getRating() > 0) {
                suma += r.getRating();
                contadorValido++;
            }
        }
        
        if (contadorValido == 0) return 0.0;

        double promedio = suma / contadorValido;
        return Math.round(promedio * 10.0) / 10.0;
    }

    public boolean eliminarResena(Long reviewId, Long clienteId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            // Verificar que el cliente logueado sea el dueño de la reseña
            if (review.getClient().getId().equals(clienteId)) {
                reviewRepository.deleteById(reviewId);
                return true; // Eliminado con éxito
            }
        }
        return false; // No se encontró o no es el dueño
    }
}