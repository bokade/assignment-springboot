package com.example.service;

import com.example.exception.BadRequestException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.Driver;
import com.example.repository.DriverRepository;
import com.example.utils.i11Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Service
public class DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverService.class);

    private DriverRepository driverRepository;

    @Autowired
    public void setDriverRepository(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$");

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");

    public Driver createDriver(Driver driver) {

        log.info("Creating driver. Email={}, Mobile={}", driver.getEmail(), driver.getMobile());

        validateMandatoryFields(driver);
        validateEmail(driver.getEmail());
        validateMobile(driver.getMobile());

        // Format DOB as yyyy-MM-dd string
        if (driver.getDateOfBirth() == null || driver.getDateOfBirth().trim().isEmpty()) {
            throw new BadRequestException("dateOfBirth is mandatory");
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(driver.getDateOfBirth().trim(), i11Utils.DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            log.error("Invalid DOB format: {}", driver.getDateOfBirth());
            throw new BadRequestException("Invalid dateOfBirth. Expected format is yyyy-MM-dd");
        }

         // Age validation
        validateDateOfBirth(dob);

         // normalize format
        driver.setDateOfBirth(dob.format(i11Utils.DATE_FORMATTER));

        if(driverRepository.checkDriverExistByLicenseNumber(driver.getLicenseNumber())){
            throw new BadRequestException("Driver with the same License Number already exists");
        }

        driver.setCreatedOn(Instant.now());
        driver.setModifiedOn(Instant.now());
        driver.setIsActive(true);

        Driver saved = driverRepository.saveOrUpdate(driver);

        log.info("Driver creation completed. id={}", saved.getId());

        return saved;
    }


    public Driver updateDriver(String id, Driver driver) {
        log.info("Updating driver with id={}", id);
        Driver existing = driverRepository.findById(id);

        if (existing == null) {
            log.warn("Driver not found with id={}", id);
            throw new ResourceNotFoundException("Driver not found with id: " + id);
        }

        if (StringUtils.hasText(driver.getFirstName())) {
            existing.setFirstName(driver.getFirstName());
        }

        if (StringUtils.hasText(driver.getLastName())) {
            existing.setLastName(driver.getLastName());
        }

        if (StringUtils.hasText(driver.getEmail())) {
            validateEmail(driver.getEmail());
            existing.setEmail(driver.getEmail());
        }

        if (StringUtils.hasText(driver.getMobile())) {
            validateMobile(driver.getMobile());
            existing.setMobile(driver.getMobile());
        }

        if (driver.getDateOfBirth() != null) {
            LocalDate dob;
            try {
                dob = LocalDate.parse(
                        driver.getDateOfBirth().trim(),
                        i11Utils.DATE_FORMATTER
                );
            } catch (DateTimeParseException ex) {
                log.error("Invalid DOB format: {}", driver.getDateOfBirth());
                throw new BadRequestException("Invalid dateOfBirth. Expected format is yyyy-MM-dd");
            }
            validateDateOfBirth(dob);
            existing.setDateOfBirth(dob.format(i11Utils.DATE_FORMATTER));
        }

        if (StringUtils.hasText(driver.getLicenseNumber())) {
//            if(driverRepository.checkDriverExistByLicenseNumber(driver.getLicenseNumber())){
//                throw new BadRequestException("Driver with the same License Number already exists");
//            }
            existing.setLicenseNumber(driver.getLicenseNumber());
        }

        if (driver.getExperienceYears() != null) {
            existing.setExperienceYears(driver.getExperienceYears());
        }


        if (StringUtils.hasText(driver.getAddress1())) {
            existing.setAddress1(driver.getAddress1());
        }

        if (StringUtils.hasText(driver.getAddress2())) {
            existing.setAddress2(driver.getAddress2());
        }

        if (StringUtils.hasText(driver.getCity())) {
            existing.setCity(driver.getCity());
        }

        if (StringUtils.hasText(driver.getState())) {
            existing.setState(driver.getState());
        }

        if (StringUtils.hasText(driver.getZipCode())) {
            existing.setZipCode(driver.getZipCode());
        }

        existing.setModifiedOn(Instant.now());
        Driver saved = driverRepository.saveOrUpdate(existing);

        log.info("Driver updated successfully with id={}", id);

        return saved;
    }

    public Driver getDriverById(String id) {
        log.info("Fetching driver by id={}", id);
        Driver driver = driverRepository.findById(id);
        if (driver == null) {
            log.warn("Driver not found with id={}", id);
            throw new ResourceNotFoundException("Driver not found with id: " + id);
        }
        return driver;
    }

    public Page<Driver> searchDrivers(String firstName, String lastName,
            String licenseNumber, Integer pageIndex, Integer itemsPerPage) {
        log.debug("Searching drivers in DB");
        return driverRepository.searchDrivers(firstName, lastName,  licenseNumber, pageIndex, itemsPerPage);
    }

    /* ================= VALIDATIONS ================= */

    private void validateMandatoryFields(Driver driver) {
        if (!StringUtils.hasText(driver.getFirstName())
                || !StringUtils.hasText(driver.getLastName())
                || !StringUtils.hasText(driver.getEmail())
                || !StringUtils.hasText(driver.getMobile())
                || driver.getDateOfBirth() ==  null
                || !StringUtils.hasText(driver.getLicenseNumber())) {

            throw new BadRequestException("First Name, Last Name, Email, Mobile , DOB, and License Number are mandatory");
        }
    }

    private void validateEmail(String email) {
        if(email != null && !StringUtils.isEmpty(email)){
            Pattern pattern = Pattern.compile(i11Utils.EMAIL_PATTERN);
            if(!pattern.matcher(email).matches()){
                throw new BadRequestException("Please provide a valid email address");
            }
        }else{
            throw new BadRequestException("please provide a  email address");
        }
    }

    private void validateMobile(String mobile) {
        if(mobile != null && !StringUtils.isEmpty(mobile)){
             Pattern pattern = Pattern.compile(i11Utils.MOBILE_PATTERN);
             if(!pattern.matcher(mobile).matches()){
                 throw new BadRequestException("Please provide a valid mobile number");
         }
        }else{
            throw new BadRequestException("please provide a  mobile number");
        }
    }

    private void validateDateOfBirth(LocalDate dob) {

        if (dob.isAfter(LocalDate.now())) {
            throw new BadRequestException("Date of birth cannot be a future date");
        }

        if (dob.isAfter(LocalDate.now().minusYears(18))) {
            throw new BadRequestException("Driver must be at least 18 years old");
        }
    }

    public void deleteDriver(String id) {
        log.info("Deleting driver with id={}", id);
        Driver driver = driverRepository.findById(id);

        if (driver == null) {
            throw new ResourceNotFoundException("Driver not found with id: " + id);
        }

        driver.setIsActive(false);
        driver.setModifiedOn(Instant.now());

        driverRepository.saveOrUpdate(driver);
        log.info("Driver soft deleted with id={}", id);
    }
}
