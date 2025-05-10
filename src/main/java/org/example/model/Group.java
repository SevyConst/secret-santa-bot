package org.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "groups")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Group {
    @Id
    private String id;

    private String name;
    private Long admin;
    private Boolean isDistributed;

    @CreatedDate
    private LocalDate createdAt;

    private LocalDate distributedAt;

}
