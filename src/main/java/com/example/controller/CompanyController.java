package com.example.controller;

import com.example.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.model.Company;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/companies")
@CrossOrigin
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);

    private CompanyService companyService;

    private ObjectMapper objectMapper;

    @Autowired
    public  void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Data Entry Screen
    @PostMapping("/create-company")
    public ResponseEntity<ObjectNode> createCompany(@RequestBody Company company) {

        log.info("Create Company API called with companyName={}", company.getCompanyName());

        Company saved = companyService.createCompany(company);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("message", "Company created successfully");
        response.put("id", saved.getId());
        response.put("companyName", saved.getCompanyName());

        log.info("Company created successfully with id={}", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update Company
    @PutMapping("/update-company/{id}")
    public ResponseEntity<ObjectNode> updateCompany(@PathVariable String id, @RequestBody Company company) {

        log.info("Request received to update company with id: {}", id);

        Company updated = companyService.updateCompany(id, company);

        log.info("Company updated successfully with id: {}", id);

        ObjectNode node = objectMapper.valueToTree(updated);
        node.put("message", "Company updated successfully");

        return ResponseEntity.ok(node);
    }

    //Get Company by ID
    @GetMapping("/get-company/{id}")
    public ResponseEntity<ObjectNode> getCompanyById(@PathVariable String id) {

        log.info("Get Company API called with id={}", id);

        Company company = companyService.getCompanyById(id);

        ObjectNode node = objectMapper.valueToTree(company);
        node.put("status", "SUCCESS");

        log.info("Company fetched successfully for id={}", id);

        return ResponseEntity.ok(node);
    }


    @GetMapping("/get-companies")
    public ResponseEntity<ObjectNode> getCompanies(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String registrationNumber,
            @RequestParam(defaultValue = "0") Integer pageIndex,
            @RequestParam(defaultValue = "10") Integer itemsPerPage) {

        log.info("Search Companies API called | companyName={}, registrationNumber={}, pageIndex={}, itemsPerPage={}",
                companyName, registrationNumber, pageIndex, itemsPerPage);

        Page<Company> page = companyService.getAllCompanyBySearch(
                companyName, registrationNumber, pageIndex, itemsPerPage);

        ArrayNode companiesArray = objectMapper.createArrayNode();
        page.getContent().forEach(company ->
                companiesArray.add(objectMapper.valueToTree(company))
        );

        ObjectNode response = objectMapper.createObjectNode();
        response.put("pageIndex", page.getNumber());
        response.put("itemsPerPage", page.getSize());
        response.put("totalRecords", page.getTotalElements());
        response.set("companies", companiesArray);

        log.info("Search completed. Total records={}", page.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-company/{id}")
    public ResponseEntity<ObjectNode> deleteCompany(@PathVariable String id) {

        log.info("Delete Company API called for id={}", id);

        companyService.deleteCompany(id);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("message", "Company deleted successfully");

        log.info("Company soft-deleted successfully for id={}", id);

        return ResponseEntity.ok(response);
    }
}