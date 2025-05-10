package org.example.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    private Long id;

    private String userName;
    private String firstName;
    private String lastName;
    private Integer numberActions;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
