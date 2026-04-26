package com.example.assignment.domain.entity;

import com.example.assignment.domain.type.EnrollmentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRecord extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long saleRecordId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "enrollment_id", nullable = false)
  private Enrollment enrollment;

  private Long amount;

  private Long refundAmount;

  public static SaleRecord toEntity(Enrollment enrollment) {
    return SaleRecord.builder()
        .enrollment(enrollment)
        .amount(enrollment.getCourse().getAmount())
        .build();
  }

  public void cancel() {
    this.refundAmount = this.amount;
  }
}
