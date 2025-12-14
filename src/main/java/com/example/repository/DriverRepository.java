package com.example.repository;

import com.example.model.Company;
import com.example.model.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class DriverRepository {
    private static final Logger log = LoggerFactory.getLogger(DriverRepository.class);
    private MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Driver saveOrUpdate(Driver driver) {
        log.debug("Saving driver to DB. id={}", driver.getId());

        Driver saved = mongoTemplate.save(driver);

        log.debug("Driver saved successfully. id={}", saved.getId());

        return saved;
    }

//    public Driver findById(String id) {
//        return mongoTemplate.findById(id, Driver.class);
//    }


    public Driver findById(String id) {
        log.debug("Finding driver by id={} and isActive=true", id);

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id).and("isActive").is(true));

        return mongoTemplate.findOne(query, Driver.class);
    }

    public Page<Driver> searchDrivers(String firstName, String lastName, String licenseNumber, Integer pageIndex, Integer itemsPerPage) {

        log.debug("Executing driver search query");

        Query query = new Query();

        if (firstName != null && !firstName.trim().isEmpty()) {
            String regex = "^" + Pattern.quote(firstName.trim()) + "$";
            query.addCriteria(Criteria.where("firstName").regex(regex, "i"));
        }

        if (lastName != null && !lastName.trim().isEmpty()) {
            String regex = "^" + Pattern.quote(lastName.trim()) + "$";
            query.addCriteria(Criteria.where("lastName").regex(regex, "i"));
        }

        if (licenseNumber != null && !licenseNumber.trim().isEmpty()) {
            String regex = "^" + Pattern.quote(licenseNumber.trim()) + "$";
            query.addCriteria(Criteria.where("licenseNumber").regex(regex, "i"));
        }

        query.addCriteria(Criteria.where("isActive").is(true));

        long total = mongoTemplate.count(query, Driver.class);

        Pageable pageable = PageRequest.of(
                pageIndex,
                itemsPerPage,
                Sort.by(Sort.Direction.ASC, "createdOn")
        );

        query.with(pageable);

        List<Driver> drivers = mongoTemplate.find(query, Driver.class);

        log.debug("Drivers found count={}", drivers.size());

        return new PageImpl<>(drivers, pageable, total);
    }

    public boolean checkDriverExistByLicenseNumber(String licenseNumber) {
        log.debug("Checking existence of Driver with licenseNumber={}", licenseNumber);
        Query query = new Query();
        query.addCriteria(Criteria.where("licenseNumber").is(licenseNumber).and("isActive").is(true));
        return mongoTemplate.exists(query, Driver.class);

    }

}
