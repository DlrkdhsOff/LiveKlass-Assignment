package com.example.assignment.domain.dto.response;

import com.example.assignment.domain.entity.Settlement;
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

  private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.KOREA);

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
        .totalAmount(FORMAT.format(settlement.getTotalAmount()) + "원")
        .refundAmount(FORMAT.format(settlement.getRefundAmount()) + "원")
        .netAmount(FORMAT.format(settlement.getNetAmount()) + "원")
        .commission(FORMAT.format(settlement.getCommission()) + "원")
        .settlementAmount(FORMAT.format(settlement.getSettlementAmount()) + "원")
        .saleCount(settlement.getSaleCount() + "건")
        .cancelCount(settlement.getCancelCount() + "건")
        .settlementStatus(settlement.getSettlementStatus().getValue())
        .settlementMonth(settlement.getSettlementMonth())
        .build();
  }
}
