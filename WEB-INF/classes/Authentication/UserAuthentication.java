package Authentication;

import DBUtil.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserAuthentication {
    private static final String SECRET_KEY = "EmployeeManagementSystemByShivram2024SecretKeyUsedForEncryption11112222333344445555666677778888999910"; // Use a strong secret key in production
    private static final long EXPIRATION_TIME = 3600000; // in milliseconds

    public static String generateToken(String employeeId, String organizationId) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withClaim("employeeId", employeeId) // Include EmployeeID in token
                .withClaim("organizationId", organizationId) // Include OrganizationID in token
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public static boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token.substring(7,token.length()));
            return jwt.getExpiresAt().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public static Map<String, String> decodeToken(String token) {
        Map<String, String> claims = new HashMap<>();
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token.substring(7,token.length()));

            // Extracting employeeId and organizationId from claims
            claims.put("employeeId", jwt.getClaim("employeeId").asString());
            claims.put("organizationId", jwt.getClaim("organizationId").asString());
        } catch (Exception e) {
            // Handle the exception (e.g., log it)
            e.printStackTrace();
        }
        return claims;
    }
}
