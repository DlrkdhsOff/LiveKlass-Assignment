package com.example.assignment.service.impl;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.response.AdminSettlementPageRes;
import com.example.assignment.domain.dto.response.AdminSettlementRes;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.repository.UserRepository;
import com.example.assignment.domain.repository.querydsl.SaleRecordQuery;
import com.example.assignment.domain.type.FailedType;
import com.example.assignment.domain.type.SuccessType;
import com.example.assignment.exception.GlobalException;
import com.example.assignment.service.AdminService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final UserRepository userRepository;
  private final SaleRecordQuery saleRecordQuery;

  @Override
  public ResultResponse getSaleRecord(Long userId, LocalDate startAt, LocalDate endAt, int page) {

    User user = getUser(userId);

    if(!user.isAdmin()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    validateDate(startAt, endAt);
    List<AdminSettlementRes> settlements = saleRecordQuery.getAdminSettlement(startAt, endAt);
    AdminSettlementPageRes response = AdminSettlementPageRes.toResponse(settlements, page);

    return new ResultResponse(SuccessType.SUCCESS_GET_ADMIN_SETTLEMENT, response);
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));
  }

  /**
   * 기간 유효성 검증
   * - 시작일이 종료일보다 늦으면 예외 발생
   * - 미래 날짜면 예외 발생
   */
  private void validateDate(LocalDate startAt, LocalDate endAt) {
    if (startAt.isAfter(endAt)) {
      throw new GlobalException(FailedType.INVALID_DATE_RANGE);
    }
    if (startAt.isAfter(LocalDate.now())) {
      throw new GlobalException(FailedType.INVALID_YEAR_MONTH);
    }
  }

}
