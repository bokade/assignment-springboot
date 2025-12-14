package com.example.repository;

import com.example.model.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class CompanyRepository {
    private static final Logger log = LoggerFactory.getLogger(CompanyRepository.class);
    private MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Company saveOrUpdate(Company company) {
        log.debug("Saving company to database. id: {}", company.getId());

        Company saved = mongoTemplate.save(company);

        log.debug("Company saved successfully. id: {}", saved.getId());

        return saved;
    }

//    public Company findById(String id) {
//        return mongoTemplate.findById(id, Company.class);
//    }

    public Company findById(String id) {
        log.debug("Fetching company from DB for id={}", id);

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id).and("isActive").is(true));
        return mongoTemplate.findOne(query, Company.class);
    }

    public Page<Company> getAllCompanyBySearch(
            String companyName,
            String registrationNumber,
            Integer pageIndex,
            Integer itemsPerPage) {

        log.debug("Searching companies. companyName={}, registrationNumber={}, pageIndex={}, itemsPerPage={}",
                companyName, registrationNumber, pageIndex, itemsPerPage);

        Query query = new Query();

        if (companyName != null && !companyName.trim().isEmpty()) {
            String regex = "^" + Pattern.quote(companyName.trim()) + "$";
            query.addCriteria(
                    Criteria.where("companyName").regex(regex, "i")
            );
        }

        if (registrationNumber != null && !registrationNumber.trim().isEmpty()) {
            String regex = "^" + Pattern.quote(registrationNumber.trim()) + "$";
            query.addCriteria(
                    Criteria.where("registrationNumber").regex(regex, "i")
            );
        }

        query.addCriteria(Criteria.where("isActive").is(true));

        long total = mongoTemplate.count(query, Company.class);

        Pageable pageable = PageRequest.of(
                pageIndex,
                itemsPerPage,
                Sort.by(Sort.Direction.ASC, "createdOn")
        );

        query.with(pageable);

        List<Company> companies = mongoTemplate.find(query, Company.class);

        log.debug("Companies found: {} out of total {}", companies.size(), total);

        return new PageImpl<>(companies, pageable, total);
    }


    public boolean checkCompanyExistByRegistractionNumber(String registrationNumber) {
        log.debug("Checking existence of company with registrationNumber={}", registrationNumber);
        Query query = new Query();
        query.addCriteria(Criteria.where("registrationNumber").is(registrationNumber).and("isActive").is(true));
        return mongoTemplate.exists(query, Company.class);

    }
}
