package uc.dev.uc_info.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 생성 시각(createdAt)만 갖는 공통 상위 엔티티.
 *
 * <p>JPA Auditing 으로 영속 시점에 createdAt 이 자동 주입된다.
 * {@code @PrePersist} 수동 구현이 필요 없다.</p>
 *
 * <p>적용 전제: 설정 클래스에 {@code @EnableJpaAuditing} 활성화.
 * 생성 시각만 필요한 엔티티(Department/Banner/Schedule/Scholarship 등)가 상속한다.
 * 생성+수정 시각이 모두 필요하면 {@link BaseUpdatableEntity} 를 상속한다.</p>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}