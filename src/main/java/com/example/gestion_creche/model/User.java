package com.example.gestion_creche.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;

    private String role; // "ROLE_PARENT" or "ROLE_ADMIN"
    
    private boolean active;

    @OneToMany(mappedBy = "parent")
    private List<Child> children;
}
