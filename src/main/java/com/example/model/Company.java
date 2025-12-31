package com.example.model;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "companies")
public class Company {

    @Id
    private String id;

    private String companyName;



    private String establishedOn; //"yyyy-MM-dd"

    private String registrationNumber; // Unique Identifier

    private String website;

    private String address1;
    private String address2;

    private String city;
    private String state;
    private String zipCode;

    // Primary Contact Details
    private String primaryContactFirstName;
    private String primaryContactLastName;
    private String primaryContactEmail; //regex
    private String primaryContactMobile; //regex

    // Auditing (optional but professional)
    private Instant createdOn;
    private Instant modifiedOn;

    private Boolean isActive;
}
