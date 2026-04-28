package com.example.assignment.admin.dto;

import com.example.assignment.common.dto.PageResponse;
import com.example.assignment.creator.dto.response.CourseRes;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

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

  public static AdminSettlementPageRes toResponse(Page<AdminSettlementRes> settlementPage) {
    List<AdminSettlementRes> settlements = settlementPage.getContent();

    long totalAmount      = settlements.stream().mapToLong(AdminSettlementRes::getRawTotalAmount).sum();
    long totalCommission  = settlements.stream().mapToLong(AdminSettlementRes::getRawCommission).sum();
    long totalSettlement  = settlements.stream().mapToLong(AdminSettlementRes::getRawSettlementAmount).sum();

    PageResponse<AdminSettlementRes> response = new PageResponse<>(
        settlementPage.getNumber() + 1,
        settlementPage.getTotalPages(),
        settlementPage.getTotalElements(),
        settlements
    );

    return AdminSettlementPageRes.builder()
        .totalAmount(FORMAT.format(totalAmount) + "원")
        .totalCommission(FORMAT.format(totalCommission) + "원")
        .totalSettlementAmount(FORMAT.format(totalSettlement) + "원")
        .pageResponse(response)
        .build();
  }
}