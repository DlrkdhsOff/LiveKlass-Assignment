package com.example.assignment.admin.dto;

import java.text.NumberFormat;
import java.util.Locale;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminSettlementRes {

  private Long creatorId;
  private String creatorName;
  private String totalAmount;
  private String commission;
  private String settlementAmount;

  // 합계 계산용 원본 값
  private Long rawTotalAmount;
  private Long rawCommission;
  private Long rawSettlementAmount;

  public AdminSettlementRes(Long creatorId, String creatorName,
      Long totalAmount, Long refundAmount) {
    long total  = totalAmount == null ? 0L : totalAmount;
    long refund = refundAmount == null ? 0L : refundAmount;
    long net    = total - refund;
    long comm   = (long) (net * 0.2);
    long settle = net - comm;

    this.creatorId           = creatorId;
    this.creatorName         = creatorName;
    this.rawTotalAmount      = net;
    this.rawCommission       = comm;
    this.rawSettlementAmount = settle;
    this.totalAmount         = String.format("%,d원", net);
    this.commission          = String.format("%,d원", comm);
    this.settlementAmount    = String.format("%,d원", settle);
  }
}
