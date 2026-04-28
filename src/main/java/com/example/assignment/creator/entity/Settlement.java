package com.example.assignment.creator.entity;

import com.example.assignment.common.entity.User;
import com.example.assignment.creator.dto.response.SettlementSummary;
import com.example.assignment.creator.type.SettlementStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long settlementId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  private Long totalAmount;       // 총 판매 금액
  private Long refundAmount;      // 환불 금액
  private Long netAmount;         // 순 판매 금액
  private Long commission;        // 플랫폼 수수료
  private Long settlementAmount;  // 정산 예정 금액
  private Long saleCount;         // 판매 건수
  private Long cancelCount;       // 취소 건수

  @Enumerated(EnumType.STRING)
  private SettlementStatus settlementStatus;

  private String settlementMonth;

  // =================== 정적 팩토리 메서드 ===================

  /**
   * Settlement 생성
   * 집계 데이터를 기반으로 정산 금액을 계산하여 PENDING 상태로 생성
   */
  public static Settlement toEntity(User user, YearMonth yearMonth, SettlementSummary summary) {
    long[] calculated = calculate(summary);

    return Settlement.builder()
        .user(user)
        .totalAmount(calculated[0])
        .refundAmount(calculated[1])
        .netAmount(calculated[2])
        .commission(calculated[3])
        .settlementAmount(calculated[4])
        .saleCount(summary.getSaleCount() == null ? 0L : summary.getSaleCount())
        .cancelCount(summary.getCancelCount() == null ? 0L : summary.getCancelCount())
        .settlementStatus(SettlementStatus.PENDING)
        .settlementMonth(yearMonth.toString())
        .build();
  }

  // =================== 정산 갱신 ===================

  /**
   * 정산 데이터 갱신
   * PENDING 상태일 때 취소 발생 시 최신 집계 데이터로 업데이트
   * 호출 전 isPending() 확인은 서비스 레이어에서 담당
   */
  public void update(SettlementSummary summary) {
    long[] calculated = calculate(summary);

    this.totalAmount      = calculated[0];
    this.refundAmount     = calculated[1];
    this.netAmount        = calculated[2];
    this.commission       = calculated[3];
    this.settlementAmount = calculated[4];
    this.saleCount        = summary.getSaleCount() == null ? 0L : summary.getSaleCount();
    this.cancelCount      = summary.getCancelCount() == null ? 0L : summary.getCancelCount();
  }

  // =================== 내부 계산 ===================

  /**
   * 정산 금액 계산
   * 순 판매 = 총 판매 - 환불
   * 수수료 = 순 판매 × 20%
   * 정산 예정 = 순 판매 - 수수료
   * null 인 경우 0 으로 처리
   */
  private static long[] calculate(SettlementSummary summary) {
    long total  = summary.getTotalAmount() == null ? 0L : summary.getTotalAmount();
    long refund = summary.getRefundAmount() == null ? 0L : summary.getRefundAmount();
    long net    = total - refund;
    long comm   = (long) (net * 0.2);
    long settle = net - comm;

    return new long[]{total, refund, net, comm, settle};
  }

  // =================== 상태 판별 ===================

  /**
   * PENDING 상태 여부 확인
   * 정산 갱신 가능 여부 판단에 사용
   */
  public boolean isPending() {
    return this.settlementStatus == SettlementStatus.PENDING;
  }
}
