package com.nowakartur97.personalkanbanboardbackend.common;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class Auditable<T> {

    @CreatedBy
    protected T createdBy;
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    protected Instant createdOn;
    @LastModifiedBy
    protected T updatedBy;
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    protected Instant updatedOn;
}
