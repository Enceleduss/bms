package com.earthworm.bms.repository;

import com.earthworm.bms.model.EducationLoan;
import com.earthworm.bms.model.LoanRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository<T extends LoanRecord> extends CrudRepository<T, Integer> {
    // void saveCustomer(CustomerRecord cust);
    List<T> findAllByUserId(String userId);
}
