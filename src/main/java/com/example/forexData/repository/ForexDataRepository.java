package com.example.forexData.repository;

import com.example.forexData.model.ForexData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForexDataRepository extends JpaRepository<ForexData, Long> {
}
