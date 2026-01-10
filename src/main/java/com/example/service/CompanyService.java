package com.example.service;

import com.example.exception.BadRequestException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.Company;
import com.example.repository.CompanyRepository;
import com.example.utils.i11Utils;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    @Autowired
    private CompanyRepository companyRepository;

    public void setCompanyRepository(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(Company company) {

        log.debug("Validating mandatory fields for company");

        validateMandatoryFields(company);

        if(companyRepository.checkCompanyExistByRegistractionNumber(company.getRegistrationNumber())) {
            throw new BadRequestException("Company with the same Registration Number already exists");
        }

        log.debug("Validating established date");


        if (company.getEstablishedOn() != null && !company.getEstablishedOn().trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(company.getEstablishedOn().trim(), i11Utils.DATE_FORMATTER);

                // Validate future date
                if (date.isAfter(LocalDate.now())) {
                    throw new BadRequestException("Please provide a valid registration date. Future date is not allowed.");
                }

                // Validate too old date (optional)
                if (date.isBefore(LocalDate.of(1800, 1, 1))) {
                    throw new BadRequestException("Please provide a valid registration date.");
                }

                company.setEstablishedOn(date.format(i11Utils.DATE_FORMATTER));
            } catch (DateTimeParseException ex) {
                log.error("Invalid establishedOn date format: {}", company.getEstablishedOn());
                throw new BadRequestException("Invalid establishedOn date. Expected format is yyyy-MM-dd");
            }
        }

        if (company.getPrimaryContactMobile() != null && !company.getPrimaryContactMobile().isEmpty()) {
            Pattern pattern = Pattern.compile(i11Utils.MOBILE_PATTERN);
            String mobile = company.getPrimaryContactMobile().trim(); // remove spaces
            log.info("Mobile after trim: '{}'", mobile);
            if (!pattern.matcher(mobile).matches()) {
                throw new BadRequestException("Invalid mobile number");
            }
        }

        if (company.getPrimaryContactEmail() != null && !company.getPrimaryContactEmail().isEmpty()) {

            Pattern pattern = Pattern.compile(i11Utils.EMAIL_PATTERN);

            if (!pattern.matcher(company.getPrimaryContactEmail()).matches()) {
                throw new BadRequestException("Invalid email");
            }

        }

        company.setCreatedOn(Instant.now());
        company.setModifiedOn(Instant.now());
        company.setIsActive(true);

        Company saved = companyRepository.saveOrUpdate(company);

        log.info("Company saved successfully with id={}", saved.getId());

        return saved;
    }


    public Company updateCompany(String id, Company company) {
        log.info("Updating company with id={}", id);
        Company existing = companyRepository.findById(id);

        if (existing == null) {
            log.warn("Company not found with id: {}", id);
           // throw new RuntimeException("Company not found");
            throw new ResourceNotFoundException("Company not found with id: " + id);
        }

        log.debug("Applying updates for company id={}", id);

        // companyName
        if (StringUtils.hasText(company.getCompanyName())) {
            existing.setCompanyName(company.getCompanyName());
        }

        // establishedOn (date as String, safe parse + validate)
        if (company.getEstablishedOn() != null && !company.getEstablishedOn().trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(company.getEstablishedOn().trim(), i11Utils.DATE_FORMATTER);

                // Validate future date
                if (date.isAfter(LocalDate.now())) {
                    throw new BadRequestException("Please provide a valid registration date. Future date is not allowed.");
                }

                // Validate too old date (optional)
                if (date.isBefore(LocalDate.of(1800, 1, 1))) {
                    throw new BadRequestException("Please provide a valid registration date.");
                }

                existing.setEstablishedOn(date.format(i11Utils.DATE_FORMATTER));

            } catch (DateTimeParseException ex) {
                log.error("Invalid establishedOn date format: {}", company.getEstablishedOn());
                throw new BadRequestException("Invalid establishedOn date. Expected format is yyyy-MM-dd");
            }
        }

        // primaryContactMobile validation
        if (company.getPrimaryContactMobile() != null && !company.getPrimaryContactMobile().isEmpty()) {
            if (!Pattern.matches(i11Utils.MOBILE_PATTERN, company.getPrimaryContactMobile())) {
                throw new BadRequestException("Invalid mobile number");
            }
            existing.setPrimaryContactMobile(company.getPrimaryContactMobile());
        }




        // registrationNumber
        if (StringUtils.hasText(company.getRegistrationNumber())) {
            if(existing.getRegistrationNumber().equalsIgnoreCase(company.getRegistrationNumber())){
                existing.setRegistrationNumber(company.getRegistrationNumber());
            }else if (companyRepository.checkCompanyExistByRegistractionNumber(company.getRegistrationNumber())) {
                throw new BadRequestException("Company with the same Registration Number already exists");
            }else {
                existing.setRegistrationNumber(company.getRegistrationNumber());
            }
        }

        // website
        if (StringUtils.hasText(company.getWebsite())) {
            existing.setWebsite(company.getWebsite());
        }

        // address1
        if (StringUtils.hasText(company.getAddress1())) {
            existing.setAddress1(company.getAddress1());
        }

        // address2
        if (StringUtils.hasText(company.getAddress2())) {
            existing.setAddress2(company.getAddress2());
        }

        // city
        if (StringUtils.hasText(company.getCity())) {
            existing.setCity(company.getCity());
        }

        // state
        if (StringUtils.hasText(company.getState())) {
            existing.setState(company.getState());
        }

        // zipCode
        if (StringUtils.hasText(company.getZipCode())) {
            existing.setZipCode(company.getZipCode());
        }

        // primaryContactFirstName
        if (StringUtils.hasText(company.getPrimaryContactFirstName())) {
            existing.setPrimaryContactFirstName(company.getPrimaryContactFirstName());
        }

        // primaryContactLastName
        if (StringUtils.hasText(company.getPrimaryContactLastName())) {
            existing.setPrimaryContactLastName(company.getPrimaryContactLastName());
        }

        // primaryContactEmail
        if (StringUtils.hasText(company.getPrimaryContactEmail())) {
            if (!Pattern.matches(i11Utils.EMAIL_PATTERN, company.getPrimaryContactEmail())) {
                throw new BadRequestException("Invalid email");
            }
            existing.setPrimaryContactEmail(company.getPrimaryContactEmail());
        }

        // primaryContactMobile
//        if (StringUtils.hasText(company.getPrimaryContactMobile())) {
//            existing.setPrimaryContactMobile(company.getPrimaryContactMobile());
//        }
        existing.setModifiedOn(Instant.now());
        Company updated = companyRepository.saveOrUpdate(existing);

        log.info("Company updated successfully for id={}", id);

        return updated;

    }

//    private void validateEstablishedDate(LocalDate establishedOn) {
//
//        // future date not allowed
//        if (establishedOn.isAfter(LocalDate.now())) {
//            throw new BadRequestException("Please provide a valid registration date. Future date is not allowed.");
//        }
//
//        // too old date check (optional but professional)
//        if (establishedOn.isBefore(LocalDate.of(1800, 1, 1))) {
//            throw new BadRequestException("Please provide a valid registration date.");
//        }
//    }


    public Company getCompanyById(String id) {
        log.info("getting company with id={}", id);
        Company company =  companyRepository.findById(id);
        if (company == null) {
            log.warn("Company not found with id: {}", id);
            // throw new RuntimeException("Company not found");
            throw new ResourceNotFoundException("Company not found with id: " + id);
        }
        return company;
    }


    public Page<Company> getAllCompanyBySearch(String companyName, String registrationNumber, Integer pageIndex, Integer itemsPerPage) {
        log.info("getting all companies with companyName={} , registrationNumber={}, pageIndex={}, itemsPerPage={}", companyName, registrationNumber,pageIndex,itemsPerPage);
        return companyRepository.getAllCompanyBySearch(companyName, registrationNumber, pageIndex, itemsPerPage);
    }

    private void validateMandatoryFields(Company company) {
        if (!StringUtils.hasText(company.getCompanyName()) || !StringUtils.hasText(company.getRegistrationNumber())) {

            throw new BadRequestException("Company Name, Registration Number are mandatory");
        }
    }

    public void deleteCompany(String id) {
        log.info("Soft delete initiated for company id={}", id);
        Company company = companyRepository.findById(id);

        if (company == null) {
            throw new ResourceNotFoundException("Company not found with id: " + id);
        }

        company.setIsActive(false);
        company.setModifiedOn(Instant.now());

        companyRepository.saveOrUpdate(company);

        log.info("Company soft deleted successfully for id={}", id);
    }

}
