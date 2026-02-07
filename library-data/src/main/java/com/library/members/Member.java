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
    @Index(name = "idx_member_membership_number", columnList = "membershipNumber", unique = true),
    @Index(name = "idx_member_last_name", columnList = "lastName")
})
@Getter
@Setter
public class Member extends BaseEntity {
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private Long membershipNumber;
}