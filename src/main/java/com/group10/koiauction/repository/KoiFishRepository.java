package com.group10.koiauction.repository;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.KoiSexEnum;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface KoiFishRepository extends JpaRepository<KoiFish, Long> {
    @Query("SELECT U FROM KoiFish U WHERE U.koi_id = ?1")
    KoiFish findByKoiId(@Param("koi_id") Long koiId);

    @Query("SELECT u FROM KoiFish u WHERE u.name LIKE CONCAT('%', :name, '%')")// CONCAT để nối chuỗi %name%
    List<KoiFish> findKoiFishByName(@Param("name") String name);

    @Query("SELECT U FROM KoiFish U WHERE U.koiStatus = :koi_status")
    List<KoiFish> findByKoiStatusEnum(@Param("koi_status") KoiStatusEnum status);

    @Query("SELECT k.varieties FROM KoiFish k WHERE k.koi_id = :koi_id ")
    Set<Variety> findVarietiesByKoiId(@Param("koi_id") Long koiId);

    @Query("SELECT U FROM KoiFish U WHERE U.account.user_id = :userId ")
    List<KoiFish> findKoiFishByBreeder(@Param("userId")Long userId);


    @Query("SELECT U FROM KoiFish U WHERE U.account.user_id = :userId AND U.koiStatus != :koiStatus ")
    List<KoiFish> findKoiFishByBreederExceptStatus(@Param("userId")Long userId , @Param("koiStatus")KoiStatusEnum status);

    @Query("SELECT U FROM KoiFish U WHERE U.account.user_id = :userId AND U.koiStatus = :status ")
    List<KoiFish> findKoiFishByBreederAndStatus(@Param("userId")Long userId ,@Param("status") KoiStatusEnum status);

    @Query("SELECT U FROM KoiFish U WHERE U.video_url = :video_url")
    KoiFish findExactKoiFishByVideoUrl(@Param("video_url") String video_url);



//    @Modifying//This tells Spring Data JPA that this is an UPDATE query and not a SELECT.
//    @Query("UPDATE KoiFish k SET k.name = :name, k.breeder = :breeder, k.sex = :sex, k.variety = :variety, k.sizeCm = :sizeCm, k.bornIn = :bornIn, k.image_url = :imageUrl, k.description = :description, k.estimatedValue = :estimatedValue WHERE k.koi_id = :koi_id")
//    void updateKoiFishByKoiId(@Param("name") String name,
//                              @Param("breeder") String breeder,
//                              @Param("sex") KoiSexEnum sex,
//                              @Param("variety") String variety,
//                              @Param("sizeCm") Double sizeCm,
//                              @Param("bornIn") Date bornIn,
//                              @Param("imageUrl") String imageUrl,
//                              @Param("description") String description,
//                              @Param("estimatedValue") Double estimatedValue,
//                              @Param("koi_id") Long koiId);


}
