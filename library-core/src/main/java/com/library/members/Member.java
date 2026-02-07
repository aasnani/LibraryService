package com.library.members;

import com.library.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_email", columnList = "email", unique = true),
        @Index(name = "idx_member_membership_number", columnList = "membership_number", unique = true),
        @Index(name = "idx_member_last_first_name", columnList = "last_name, first_name")
})
@Getter
@Setter
public class Member extends BaseEntity {
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "membership_number", unique = true, nullable = false)
    private Long membershipNumber;
}