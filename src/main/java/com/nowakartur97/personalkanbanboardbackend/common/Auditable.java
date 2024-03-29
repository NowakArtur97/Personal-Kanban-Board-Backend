package com.nowakartur97.personalkanbanboardbackend.common;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable<T> {

    @CreatedBy
    protected T createdBy;
    @CreatedDate
    @Temporal(TemporalType.DATE)
    protected LocalDate createdOn;
    @LastModifiedBy
    protected T updatedBy;
    @LastModifiedDate
    @Temporal(TemporalType.DATE)
    protected LocalDate updatedOn;
}
