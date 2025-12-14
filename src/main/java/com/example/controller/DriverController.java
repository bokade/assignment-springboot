package com.example.controller;

import com.example.model.Driver;
import com.example.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/drivers")
@CrossOrigin
public class DriverController {

    private static final Logger log = LoggerFactory.getLogger(DriverController.class);

    @Autowired
    private DriverService driverService;

    @Autowired
    private ObjectMapper objectMapper;


    // CREATE DRIVER
    @PostMapping("/create-driver")
    public ResponseEntity<ObjectNode> createDriver(@RequestBody Driver driver) {

        log.info("Request received to create driver. Email={}", driver.getEmail());

        Driver saved = driverService.createDriver(driver);

        log.info("Driver created successfully with id={}", saved.getId());

        ObjectNode response = objectMapper.createObjectNode();
        response.put("message", "Driver created successfully");
        response.put("driverId", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UPDATE DRIVER
    @PutMapping("/update-driver/{id}")
    public ResponseEntity<ObjectNode> updateDriver(@PathVariable String id, @RequestBody Driver driver) {

        log.info("Request received to update driver with id={}", id);

        Driver updated = driverService.updateDriver(id, driver);

        log.info("Driver updated successfully with id={}", id);

        ObjectNode response = objectMapper.valueToTree(updated);
        response.put("message", "Driver updated successfully");

        return ResponseEntity.ok(response);
    }

    // GET BY ID
    @GetMapping("/get-driver/{id}")
    public ResponseEntity<ObjectNode> getDriverById(@PathVariable String id) {

        log.info("Fetching driver details for id={}", id);

        Driver driver = driverService.getDriverById(id);

        ObjectNode response = objectMapper.valueToTree(driver);
        response.put("status", "SUCCESS");

        return ResponseEntity.ok(response);
    }

    // SEARCH + PAGINATION
    @GetMapping("/get-drivers")
    public ResponseEntity<ObjectNode> getDrivers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(defaultValue = "0") Integer pageIndex,
            @RequestParam(defaultValue = "10") Integer itemsPerPage) {

        log.info("Searching drivers. firstName={}, lastName={}, licenseNumber={}, pageIndex={}, itemsPerPage={}",
                firstName, lastName, licenseNumber, pageIndex, itemsPerPage);

        Page<Driver> page = driverService.searchDrivers(
                firstName, lastName, licenseNumber, pageIndex, itemsPerPage);

        ArrayNode arrayNode = objectMapper.createArrayNode();
        page.getContent().forEach(d -> arrayNode.add(objectMapper.valueToTree(d)));

        ObjectNode response = objectMapper.createObjectNode();
        response.put("pageIndex", page.getNumber());
        response.put("itemsPerPage", page.getSize());
        response.put("totalRecords", page.getTotalElements());
        response.set("drivers", arrayNode);

        return ResponseEntity.ok(response);
    }

    // DELETE DRIVER
    @DeleteMapping("/delete-driver/{id}")
    public ResponseEntity<ObjectNode> deleteDriver(@PathVariable String id) {

        log.info("Request received to delete driver with id={}", id);

        driverService.deleteDriver(id);

        log.info("Driver deleted successfully with id={}", id);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("message", "Driver deleted successfully");

        return ResponseEntity.ok(response);
    }
}
