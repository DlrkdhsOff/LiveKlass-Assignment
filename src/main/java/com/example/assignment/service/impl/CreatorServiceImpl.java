package com.example.assignment.service.impl;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.response.SettlementRes;
import com.example.assignment.domain.dto.response.SettlementSummary;
import com.example.assignment.domain.entity.Settlement;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.repository.SettlementRepository;
import com.example.assignment.domain.repository.UserRepository;
import com.example.assignment.domain.repository.querydsl.SaleRecordQuery;
import com.example.assignment.domain.type.FailedType;
import com.example.assignment.domain.type.SuccessType;
import com.example.assignment.exception.GlobalException;
import com.example.assignment.service.CreatorService;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatorServiceImpl implements CreatorService {

  private final UserRepository userRepository;
  private final SettlementRepository settlementRepository;
  private final SaleRecordQuery saleRecordQuery;

  /**
   * 크리에이터 월별 정산 조회
   * 이미 저장된 정산 데이터가 있으면 반환, 없으면 집계 후 저장
   * PENDING 상태인 경우 취소 발생 여부를 반영하기 위해 재집계
   */
  @Override
  @Transactional
  public ResultResponse getSaleRecord(Long userId, String yearMonth) {

    User user = getUser(userId);

    if (user.isStudent()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    Settlement settlement = getSettlement(user, yearMonth);
    SettlementRes response = SettlementRes.toResponse(settlement);

    return new ResultResponse(SuccessType.SUCCESS_GET_SETTLEMENT, response);
  }

  // =================== 내부 메서드 ===================

  /**
   * 유저 조회
   * userId 에 해당하는 유저가 없으면 예외 발생
   */
  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));
  }

  /**
   * 정산 데이터 조회
   * 1. 저장된 정산 데이터가 있는 경우
   *    - PENDING 상태: 취소 발생 가능성이 있으므로 재집계 후 업데이트
   *    - CONFIRMED / PAID 상태: 확정된 금액이므로 그대로 반환
   * 2. 저장된 정산 데이터가 없는 경우
   *    - 집계 쿼리 실행 후 PENDING 상태로 저장
   */
  private Settlement getSettlement(User user, String yearMonth) {
    YearMonth ym = getYearMonth(yearMonth);
    LocalDateTime startAt = ym.atDay(1).atStartOfDay();
    LocalDateTime endAt = ym.atEndOfMonth().atTime(23, 59, 59);

    return settlementRepository
        .findByUserAndSettlementMonth(user, ym.toString())
        .map(s -> {
          if (s.isPending()) {
            SettlementSummary summary = saleRecordQuery.getSummary(user, startAt, endAt);
            s.update(summary);
          }
          return s;
        })
        .orElseGet(() -> {
          SettlementSummary summary = saleRecordQuery.getSummary(user, startAt, endAt);
          Settlement settlement = Settlement.toEntity(user, ym, summary);
          return settlementRepository.save(settlement);
        });
  }

  /**
   * 연월 파싱 및 검증
   * - null 이면 현재 연월 사용
   * - 미래 날짜면 예외 발생
   * - 형식이 올바르지 않으면 예외 발생 (올바른 형식: 2025-03)
   */
  private YearMonth getYearMonth(String yearMonth) {
    try {
      YearMonth ym = (yearMonth == null) ? YearMonth.now() : YearMonth.parse(yearMonth);
      if (ym.isAfter(YearMonth.now())) {
        throw new GlobalException(FailedType.INVALID_YEAR_MONTH);
      }
      return ym;
    } catch (DateTimeParseException e) {
      throw new GlobalException(FailedType.INVALID_DATE_FORMAT);
    }
  }
}