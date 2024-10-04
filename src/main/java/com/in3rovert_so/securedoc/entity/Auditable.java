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
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = { "createdAt", "updatedAt" }, allowGetters = true)
public abstract class Auditable {
    @Id
    @SequenceGenerator(name = "primary_key_seq", sequenceName = "primary_key_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_key_seq")
    @Column(name = "id", updatable = false)
    private Long id;
    private String referenceId =  new AlternativeJdkIdGenerator().generateId().toString();

    @NotNull
    private Long createdBy;

    @NotNull
    private Long updatedBy;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt; //WHO UPDATED IT at what time

    @Column(name = "updated_at", nullable = false)
    @CreatedDate

    private LocalDateTime updatedAt; //WHO UPDATED IT at what time.
    /**
     * ======================
     * This method is called before an entity is persisted in the database.
     * It sets the createdAt, createdBy, updatedBy, and updatedAt fields of the entity.
     * It also checks if a userId is provided and throws an exception if it is not.
     * ======================
     */
    @PrePersist
    public void beforePersist() {
        var userId = RequestContext.getUserId();
       // var userId = 0L;
        if (userId == null) { throw new ApiException("Cannot persist entity without user ID in Request  Context for this Thread ");}
        setCreatedAt(now());
        setCreatedBy(userId);
        setUpdatedBy(userId);
        setUpdatedAt(now());
    }
    /**
     * ======================
     * Updates the entity before it is persisted to the database.
     * This method is annotated with @PreUpdate, which means it will be called by the persistence provider
     * immediately before the entity is updated in the database.
     * The method retrieves the user ID from the RequestContext and throws an ApiException if the user ID is null.
     * It then sets the updatedAt field to the current time and the updatedBy field to the user ID.
     * @throws ApiException if the user ID is null
     * ======================
     */
    @PreUpdate
    public void beforeUpdate() {
        var userId =  RequestContext.getUserId();
       // var userId =  0L;
        if (userId == null) { throw new ApiException("Cannot update entity without user ID in Request  Context for this Thread");}
        setUpdatedAt(now());
        setUpdatedBy(userId);
    }
}
