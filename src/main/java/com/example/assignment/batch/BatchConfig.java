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
   * 미결제 자동 취소 배치
   * 매일 자정에 실행
   * 강의 시작 하루 전까지 결제하지 않은 PENDING 건을 자동 취소
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
