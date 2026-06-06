package uz.pdp.smartinventory.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final String secretKeyString = "9a2f34f24302c11438a203f1947b31273948231a31248a39c13847a391c491a2";

    private final long accessExpiration = 15 * 60 * 1000;  // 15 daqiqa
    private final long refreshExpiration = 7 * 24 * 60 * 60 * 1000;  // 7 kun

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token yaratish
    public String generateAccessToken(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    // Refresh Token yaratish
    public String generateRefreshToken(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey())
                .compact();
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Token ichidan username ni sug'urib olish
    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    // Token umrini tekshirish
    private boolean isTokenExpired(String token){
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, String username){
        final String extractedUsername = extractUsername(token);
        // 1. Tokendan chiqqan username DB dagi user bilan mosmi?
        // 2. Va token hali o'lib ulgurmaganmi?
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }


}
