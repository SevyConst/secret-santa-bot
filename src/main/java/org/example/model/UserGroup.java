package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users_groups")
public class UserGroup {

    @Id
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "group_id")
    Group group;
}
