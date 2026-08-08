package com.earthworm.bms.repository;

import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.model.Folder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolderRepository extends CrudRepository<Folder, Long> {
    Optional<Folder> findByName(String username);
}
