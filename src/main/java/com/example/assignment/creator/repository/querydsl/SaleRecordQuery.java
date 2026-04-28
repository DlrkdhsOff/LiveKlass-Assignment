package com.example.assignment.creator.repository.querydsl;

import com.example.assignment.admin.dto.AdminSettlementRes;
import com.example.assignment.common.entity.User;
import com.example.assignment.creator.dto.response.SettlementSummary;
import com.example.assignment.creator.entity.QSaleRecord;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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


  public Page<AdminSettlementRes> getAdminSettlement(LocalDate startAt, LocalDate endAt, Pageable pageable) {
    QSaleRecord saleRecord = QSaleRecord.saleRecord;

    LocalDateTime start = startAt.atStartOfDay();
    LocalDateTime end = endAt.atTime(23, 59, 59);

    List<AdminSettlementRes> content = queryFactory
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
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory
        .select(saleRecord.user.userId.countDistinct())
        .from(saleRecord)
        .where(
            saleRecord.createdAt.between(start, end)
        )
        .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
  }

}
