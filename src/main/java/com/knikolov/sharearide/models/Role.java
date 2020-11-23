package com.knikolov.sharearide.models;

import com.knikolov.sharearide.enums.ERole;
import javax.persistence.*;
import java.util.UUID;

/**
 * Entity that holds all user roles
 */
@Entity
public class Role {

    @Id
    @Column(name = "role_id")
    private String id;

    @Enumerated(EnumType.STRING)
    private ERole name;

    public Role() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ERole getName() {
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }
}
