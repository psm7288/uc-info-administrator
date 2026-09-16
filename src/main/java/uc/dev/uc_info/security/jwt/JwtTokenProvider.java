package uc.dev.uc_info.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * 학생 앱(Flutter) 인증용 JWT 발급·검증 컴포넌트.
 *
 * <p>토큰의 subject(sub) 클레임에 studentId(학번)를 담는다.</p>
 *
 * <p>ES256(ECDSA P-256) 비대칭 서명을 쓴다 — 개인키({@code JWT_PRIVATE_KEY})로
 * 서명하고 공개키({@code JWT_PUBLIC_KEY})로 검증한다. 둘 다 PKCS8/X.509 PEM
 * 문자열(헤더·개행 포함해도 되고 Base64만 있어도 됨)을 환경변수로 받는다.</p>
 */
@Component
public class JwtTokenProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long expirationMillis;

    /**
     * JWT 서명·검증에 쓸 EC 키쌍과 만료 시간을 초기화한다.
     *
     * @param privateKeyValue 서명용 EC 개인키(PKCS8, 환경변수 JWT_PRIVATE_KEY)
     * @param publicKeyValue  검증용 EC 공개키(X.509, 환경변수 JWT_PUBLIC_KEY)
     * @param expirationHours 토큰 만료 시간(시간 단위, 예: 720 = 30일)
     * @throws IllegalStateException 키 형식이 잘못되어 파싱에 실패한 경우
     */
    public JwtTokenProvider(
            @Value("${jwt.private-key}") String privateKeyValue,
            @Value("${jwt.public-key}") String publicKeyValue,
            @Value("${jwt.expiration-hours}") long expirationHours
    ) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            this.privateKey = keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(decode(privateKeyValue)));
            this.publicKey = keyFactory.generatePublic(
                    new X509EncodedKeySpec(decode(publicKeyValue)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(
                    "JWT_PRIVATE_KEY/JWT_PUBLIC_KEY 형식이 올바르지 않습니다 (PKCS8/X.509 EC 키여야 함).", e);
        }
        this.expirationMillis = expirationHours * 60 * 60 * 1000;
    }

    /**
     * PEM 헤더/개행이 섞여 있어도 순수 Base64 DER 바이트로 디코딩한다.
     *
     * @param pemOrBase64 "-----BEGIN ...-----" 헤더가 있는 PEM 문자열이거나 순수 Base64 문자열
     * @return 디코딩된 DER 바이트
     */
    private static byte[] decode(String pemOrBase64) {
        String cleaned = pemOrBase64
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(cleaned);
    }

    /**
     * studentId를 담은 토큰을 발급한다.
     *
     * @param studentId 토큰에 담을 학번
     * @return ES256으로 서명된 JWT 문자열
     */
    public String createToken(String studentId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(studentId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.ES256)
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
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}