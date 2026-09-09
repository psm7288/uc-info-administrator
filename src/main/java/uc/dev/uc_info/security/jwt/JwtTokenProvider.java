package uc.dev.uc_info.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 학생 앱(Flutter) 인증용 JWT 발급·검증 컴포넌트.
 *
 * <p>토큰의 subject(sub) 클레임에 studentId(학번)를 담는다.</p>
 *
 * <p>시크릿 키는 {@code jwt.secret}(환경변수 {@code JWT_SECRET})에서 읽는다.
 * HS256 서명이라 최소 32바이트(256비트) 이상이어야 한다</p>
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMillis;

    /**
     * JWT 서명에 쓸 키와 만료 시간을 초기화한다.
     *
     * @param secret          서명용 시크릿 문자열(환경변수 JWT_SECRET, 최소 32바이트/256비트)
     * @param expirationHours 토큰 만료 시간(시간 단위, 예: 720 = 30일)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-hours}") long expirationHours
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationHours * 60 * 60 * 1000;
    }

    /**
     * studentId를 담은 토큰을 발급한다.
     *
     * @param studentId 토큰에 담을 학번
     * @return 서명된 JWT 문자열
     */
    public String createToken(String studentId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(studentId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰에서 studentId를 꺼낸다. 호출 전에 반드시
     * {@link #validateToken(String)}으로 유효성을 먼저 확인해야 한다 —
     * 이 메서드 자체는 서명/만료 오류 시 예외를 던진다.
     *
     * @param token 검증할 토큰
     * @return 토큰에 담긴 studentId
     */
    public String getStudentId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰이 서명·만료 검증을 통과하는지 확인한다.
     *
     * @param token 검증할 토큰
     * @return 유효하면 true, 위조/만료/형식오류 등 어떤 이유로든 무효면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰을 파싱해 서명 검증까지 마친 클레임을 꺼낸다. 서명이 위조됐거나
     * 만료됐거나 형식이 잘못됐으면 {@link JwtException} 또는
     * {@link IllegalArgumentException}을 던진다 — 호출하는 쪽
     * ({@link #getStudentId}/{@link #validateToken})이 각자 상황에 맞게
     * 처리한다.
     *
     * @param token 파싱할 토큰
     * @return 검증을 통과한 클레임
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}