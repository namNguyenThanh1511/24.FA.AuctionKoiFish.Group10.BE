package com.group10.koiauction.repository;

import com.group10.koiauction.entity.KoiFish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KoiFishRepository extends JpaRepository<KoiFish, Long> {
    @Query("SELECT U FROM KoiFish U WHERE U.koi_id = ?1")
    KoiFish findByKoiId(@Param("koi_id") Long koiId);

    @Query("SELECT u FROM KoiFish u WHERE u.name LIKE CONCAT('%', :name, '%')")// CONCAT để nối chuỗi %name%
    List<KoiFish> findKoiFishByName(@Param("name") String name);



}
