package uc.dev.uc_info.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 생성 시각 + 수정 시각(createdAt/updatedAt)을 갖는 공통 상위 엔티티.
 *
 * <p>{@link BaseTimeEntity} 를 확장하여 updatedAt 을 추가한다. JPA Auditing 으로
 * 영속 시 createdAt, 갱신 시 updatedAt 이 자동 주입된다.</p>
 *
 * <p>생성+수정 시각이 모두 필요한 엔티티(Notice/Shuttle 등)가 상속한다.</p>
 */
@Getter
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseTimeEntity{

    /** 수정 시각. 갱신 시 자동 주입 */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}