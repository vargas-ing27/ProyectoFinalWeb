package proyecto.barberos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import proyecto.barberos.entity.User;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Llave secreta para firmar (¡En producción esto va en variables de entorno!)
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Tiempo de vida del token (ej: 10 horas)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; 

    // 1. GENERAR TOKEN
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // Guardamos el email
                .claim("userId", user.getId()) // Guardamos el ID
                .claim("role", user.getRole()) // Guardamos el Rol
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 2. VALIDAR TOKEN Y OBTENER EMAIL
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 3. VALIDAR SI EL TOKEN ESTÁ VENCIDO
    public boolean isTokenValid(String token) {
        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}