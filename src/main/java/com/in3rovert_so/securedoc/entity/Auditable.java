package com.in3rovert_so.securedoc.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.in3rovert_so.securedoc.domain.RequestContext;
import com.in3rovert_so.securedoc.exception.ApiException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.AlternativeJdkIdGenerator;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) //Todo: Insert EnableJpaAuduting annotation to Application class
@JsonIgnoreProperties(value = { "createdAt", "updatedAt" }, allowGetters = true) //Because we need to ignore this fields
public abstract class Auditable {
    @Id
    @SequenceGenerator(name = "primary_key_seq", sequenceName = "primary_key_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_key_seq")
    @Column(name = "id", updatable = false)
    private Long id; //This is going to serve all the subclasses so don't need to define an id from the subclasses.
    private String referenceId =  new AlternativeJdkIdGenerator().generateId().toString(); //Used to identity a specific resource in the database. Anytime we save an entity they get a refID

    @NotNull
    private Long createdBy; // WHO CREATED IT.

    @NotNull
    private Long updatedBy; //WHO UPDATED IT.

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt; //WHO UPDATED IT at what time

    @Column(name = "updated_at", nullable = false)
    @CreatedDate
    private LocalDateTime updatedAt; //WHO UPDATED IT at what time.
    //.....


    //.....
    @PrePersist
    public void beforePersist() {
        //var userId = RequestContext.getUserId();
        var userId = 0L;
        //if (userId == null) { throw new ApiException("Cannot persist entity without user ID in Request  Context for this Thread ");}
        setCreatedAt(now());
        setCreatedBy(userId);
        setUpdatedBy(userId);
        setUpdatedAt(now());
    }
    @PreUpdate
    public void beforeUpdate() {
        //var userId =  RequestContext.getUserId();
        var userId =  0L;
        //if (userId == null) { throw new ApiException("Cannot update entity without user ID in Request  Context for this Thread");}
        setUpdatedAt(now());
        setUpdatedBy(userId);
    }


}
