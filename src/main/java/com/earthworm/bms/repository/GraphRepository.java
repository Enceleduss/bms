package com.earthworm.bms.repository;

import com.earthworm.bms.model.GraphNode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GraphRepository<T extends GraphNode> extends CrudRepository<T, Long> {
    // void saveCustomer(CustomerRecord cust);
    //List<T> findAllByUserId(String userId);
}