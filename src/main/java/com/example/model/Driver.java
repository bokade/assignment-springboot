package com.example.model;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    private String email;
    private String mobile;

    private String dateOfBirth;

    private String licenseNumber;

    private Integer experienceYears;

    private String address1;
    private String address2;

    private String city;
    private String state;
    private String zipCode;

    // Auditing
    private Instant createdOn;
    private Instant modifiedOn;

    private Boolean isActive;
}
