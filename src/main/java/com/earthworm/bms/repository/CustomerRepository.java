package com.earthworm.bms.repository;

import com.earthworm.bms.model.CustomerRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<CustomerRecord, Long> {
   // void saveCustomer(CustomerRecord cust);
    Optional<CustomerRecord> findByEmail(String email);
    Optional<CustomerRecord> findByUserNameOrEmail(String username, String email);
    Optional<CustomerRecord> findByUserName(String username);
    Boolean existsByUserName(String username);
    Boolean existsByEmail(String email);
}
