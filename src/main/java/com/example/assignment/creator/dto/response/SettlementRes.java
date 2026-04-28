package com.example.assignment.creator.dto.response;

import com.example.assignment.creator.entity.Settlement;
import java.text.NumberFormat;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRes {

  private Long settlementId;

  private Long userId;

  private String totalAmount;       // 총 판매 금액

  private String refundAmount;      // 환불 금액

  private String netAmount;         // 순 판매 금액

  private String commission;        // 플랫폼 수수료

  private String settlementAmount;  // 정산 예정 금액

  private String saleCount;           // 판매 건수

  private String cancelCount;         // 취소 건수

  private String settlementStatus;

  private String settlementMonth;


  public static SettlementRes toResponse(Settlement settlement) {
    return SettlementRes.builder()
        .settlementId(settlement.getSettlementId())
        .userId(settlement.getUser().getUserId())
        .totalAmount(String.format("%,d원", settlement.getTotalAmount()))
        .refundAmount(String.format("%,d원", settlement.getRefundAmount()))
        .netAmount(String.format("%,d원", settlement.getNetAmount()))
        .commission(String.format("%,d원", settlement.getCommission()))
        .settlementAmount(String.format("%,d원", settlement.getSettlementAmount()))
        .saleCount(settlement.getSaleCount() + "건")
        .cancelCount(settlement.getCancelCount() + "건")
        .settlementStatus(settlement.getSettlementStatus().getValue())
        .settlementMonth(settlement.getSettlementMonth())
        .build();
  }
}
