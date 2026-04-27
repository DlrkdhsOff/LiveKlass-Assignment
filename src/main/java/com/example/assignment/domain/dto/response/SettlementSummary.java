package com.example.assignment.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementSummary {

  private Long totalAmount;

  private Long refundAmount;

  private Long saleCount;

  private Long cancelCount;

}
