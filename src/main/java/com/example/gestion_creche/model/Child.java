package com.example.gestion_creche.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "children")
@Data
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    private String allergies;
    private String medicalNotes;
    private String address;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "additional_info")
    private String additionalInfo;
    
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;
}