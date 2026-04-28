package com.example.assignment.admin.service.impl;

import com.example.assignment.admin.dto.AdminSettlementPageRes;
import com.example.assignment.admin.dto.AdminSettlementRes;
import com.example.assignment.admin.service.AdminService;
import com.example.assignment.common.dto.PageResponse;
import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.common.entity.User;
import com.example.assignment.common.exception.GlobalException;
import com.example.assignment.common.respotiroy.UserRepository;
import com.example.assignment.common.type.FailedType;
import com.example.assignment.common.type.SuccessType;
import com.example.assignment.creator.repository.querydsl.SaleRecordQuery;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final UserRepository userRepository;
  private final SaleRecordQuery saleRecordQuery;

  @Override
  public ResultResponse getSaleRecord(Long userId, LocalDate startAt, LocalDate endAt, int page) {

    validateAdmin(userId);

    validateDate(startAt, endAt);

    Pageable pageable = PageRequest.of(page - 1, 10);
    Page<AdminSettlementRes> settlements = saleRecordQuery.getAdminSettlement(startAt, endAt, pageable);

    AdminSettlementPageRes response = AdminSettlementPageRes.toResponse(settlements);

    return new ResultResponse(SuccessType.SUCCESS_GET_ADMIN_SETTLEMENT, response);
  }

  private void validateAdmin(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));

    if (!user.isAdmin()) throw new GlobalException(FailedType.ACCESS_DENIED);
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
