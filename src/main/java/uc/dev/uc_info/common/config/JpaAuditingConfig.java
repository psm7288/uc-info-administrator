package uc.dev.uc_info.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화 설정.
 *
 * <p>{@code @CreatedDate}, {@code @LastModifiedDate} 가 동작하도록 한다.
 * BaseTimeEntity / BaseUpdatableEntity 를 상속한 엔티티의 시각 필드가
 * 영속/갱신 시점에 자동 주입된다.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}