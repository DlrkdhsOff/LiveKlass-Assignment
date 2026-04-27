package com.example.assignment.domain.dto.response;

import com.example.assignment.domain.dto.PageResponse;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettlementPageRes {

  private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.KOREA);

  private String totalAmount;
  private String totalCommission;
  private String totalSettlementAmount;
  private PageResponse<AdminSettlementRes> pageResponse;

  public static AdminSettlementPageRes toResponse(List<AdminSettlementRes> settlements, int page) {
    long totalAmount      = settlements.stream().mapToLong(AdminSettlementRes::getRawTotalAmount).sum();
    long totalCommission  = settlements.stream().mapToLong(AdminSettlementRes::getRawCommission).sum();
    long totalSettlement  = settlements.stream().mapToLong(AdminSettlementRes::getRawSettlementAmount).sum();

    return AdminSettlementPageRes.builder()
        .totalAmount(FORMAT.format(totalAmount) + "원")
        .totalCommission(FORMAT.format(totalCommission) + "원")
        .totalSettlementAmount(FORMAT.format(totalSettlement) + "원")
        .pageResponse(PageResponse.pagination(settlements, page))
        .build();
  }
}
