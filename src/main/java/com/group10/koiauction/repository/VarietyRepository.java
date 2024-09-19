package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Variety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface VarietyRepository extends JpaRepository<Variety, Long> {
    @Query("SELECT U FROM Variety U ")
    public Set<Variety> getAllVarieties();

}
