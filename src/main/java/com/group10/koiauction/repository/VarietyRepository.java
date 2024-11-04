package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.VarietyStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Set;

public interface VarietyRepository extends JpaRepository<Variety, Long> {
    @Query("SELECT U FROM Variety U ")
    Set<Variety> getAllVarieties();

    @Query(value = "SELECT U FROM Variety  U  WHERE U.status = ?1")
    Set<Variety> getAllVarietiesByStatus(@Param("status") VarietyStatusEnum status);

   @Query(value = "SELECT U FROM Variety U where U.name = :name AND U.status = :status ")
    Variety findVarietyByNameAndStatus(@Param("name") String name , @Param("status") VarietyStatusEnum status);

}
