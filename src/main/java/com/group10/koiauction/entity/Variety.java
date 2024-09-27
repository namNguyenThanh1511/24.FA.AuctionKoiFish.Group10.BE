package com.group10.koiauction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.group10.koiauction.entity.enums.VarietyStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "variety")
public class Variety {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @NotBlank(message = "name is required")
    @Column(name = "name" , unique = true)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private VarietyStatusEnum status;


    @JsonIgnore
    @ManyToMany(mappedBy = "varieties") // mappedBy = "trỏ đến tên của set Varieties bên KoiFish" để ánh xạ qua
    Set<KoiFish> koiFishSet = new HashSet<>();


}
