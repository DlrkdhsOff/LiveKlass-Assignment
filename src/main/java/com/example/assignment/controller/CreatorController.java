package com.example.assignment.controller;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.service.CreatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "강사 API", description = "정산 집계 내역 조회")
@RestController
@RequestMapping("/api/v1/creator")
@RequiredArgsConstructor
public class CreatorController {

  private final CreatorService creatorService;

  @GetMapping("/{userId}/sale")
  @Operation(summary = "월 별 정산 집계 내역 조회", description = "기간, 상태로 필터링 가능.")
  public ResponseEntity<ResultResponse> getSaleRecord(
      @PathVariable Long userId,
      @RequestParam(required = false) String yearMonth) {

    ResultResponse response = creatorService.getSaleRecord(userId, yearMonth);
    return new ResponseEntity<>(response, response.getStatus());
  }

}
