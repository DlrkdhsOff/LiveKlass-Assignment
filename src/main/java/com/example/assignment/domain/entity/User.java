package com.example.assignment.domain.entity;

import com.example.assignment.domain.type.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  private String name;

  @Enumerated(EnumType.STRING)
  private UserRole userRole;

  public boolean isStudent() {
    return userRole == UserRole.STUDENT;
  }

  public boolean isCreator() {
    return userRole == UserRole.CREATORS;
  }

  public boolean isAdmin() {
    return userRole == UserRole.ADMIN;
  }
}