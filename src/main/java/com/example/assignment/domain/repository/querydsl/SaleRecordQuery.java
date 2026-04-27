package com.example.assignment.domain.repository.querydsl;

import com.example.assignment.domain.dto.response.AdminSettlementRes;
import com.example.assignment.domain.dto.response.SettlementSummary;
import com.example.assignment.domain.entity.QSaleRecord;
import com.example.assignment.domain.entity.User;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SaleRecordQuery {

  private final JPAQueryFactory queryFactory;

  /**
   * 크리에이터별 월별 정산 집계 조회
   * 판매 건수, 취소 건수, 총 판매 금액, 환불 금액을 집계하여 SettlementRes 로 반환
   */
  public SettlementSummary getSummary(User user, LocalDateTime start, LocalDateTime end) {
    QSaleRecord saleRecord = QSaleRecord.saleRecord;

    return queryFactory
        .select(Projections.constructor(SettlementSummary.class,
            saleRecord.amount.sum(),
            saleRecord.refundAmount.sum(),
            saleRecord.count(),
            saleRecord.refundAmount.count()
        ))
        .from(saleRecord)
        .where(
            saleRecord.user.eq(user),
            saleRecord.createdAt.between(start, end)
        )
        .fetchOne();
  }


  public List<AdminSettlementRes> getAdminSettlement(LocalDate startAt, LocalDate endAt) {
    QSaleRecord saleRecord = QSaleRecord.saleRecord;

    LocalDateTime start = startAt.atStartOfDay();
    LocalDateTime end = endAt.atTime(23, 59, 59);

    return queryFactory
        .select(Projections.constructor(AdminSettlementRes.class,
            saleRecord.user.userId,
            saleRecord.user.name,
            saleRecord.amount.sum(),
            saleRecord.refundAmount.sum()
        ))
        .from(saleRecord)
        .join(saleRecord.user)
        .where(
            saleRecord.createdAt.between(start, end)
        )
        .groupBy(saleRecord.user.userId, saleRecord.user.name)
        .orderBy(saleRecord.user.userId.asc())
        .fetch();
  }

}
