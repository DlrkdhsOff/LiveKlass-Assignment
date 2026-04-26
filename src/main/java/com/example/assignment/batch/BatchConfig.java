package com.example.assignment.batch;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchConfig {

  private final JobLauncher jobLauncher;
  private final Job cancelJob;

  /**
   * 매일 자정 실행되는 수강신청 자동 처리 배치
   * 1. 강의 시작 3일 전 OPEN/DRAFT 상태인 강의 자동 마감
   * 2. 강의 시작 3일 전까지 미결제(PENDING) 건 자동 취소 + 대기자 승격
   * 3. 강의 시작 3일 전까지 대기(WAITLISTED) 건 자동 취소
   */
  @Scheduled(cron = "0 0 0 * * *")
  public void cancelUnpaidBatch() {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLocalDateTime("executeTime", LocalDateTime.now())
          .toJobParameters();

      jobLauncher.run(cancelJob, jobParameters);
      log.info("[배치] 배치 실행 완료");

    } catch (Exception e) {
      log.error("[배치] 배치 실행 실패", e);
    }
  }

}
