//package com.foodorder.backend.points.entity;
//
//import com.foodorder.backend.user.entity.User;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(name = "points")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class Points {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
//    private User user;
//
//    private int amount;
//
//    public Points(int amount) {
//        this.amount = amount;
//    }
//}
