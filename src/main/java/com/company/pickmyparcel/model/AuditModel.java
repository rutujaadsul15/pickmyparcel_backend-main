package com.company.pickmyparcel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"CREATED_DATE", "UPDATED_DATE"})
@Data
public abstract class AuditModel  implements Serializable {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    @CreatedDate
    private Date createdDate;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_DATE", nullable = false)
    @LastModifiedDate
    private Date updatedDate;


}
