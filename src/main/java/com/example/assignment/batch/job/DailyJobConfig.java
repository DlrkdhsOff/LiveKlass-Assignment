package com.example.assignment.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DailyJobConfig  {

  private final JobRepository jobRepository;
  private final Step cancelUnpaidStep;
  private final Step cancelWaitlistStep;
  private final Step closeCourseStep;

  /**
   * 매일 자정 실행되는 수강신청 자동 처리 Job
   * 1. closeCourseStep    : 강의 시작 3일 전 OPEN/DRAFT 상태인 강의 자동 마감
   * 2. cancelUnpaidStep   : 강의 시작 3일 전까지 미결제(PENDING) 건 자동 취소 + 대기자 승격
   * 3. cancelWaitlistStep : 강의 시작 3일 전까지 대기(WAITLISTED) 건 자동 취소
   */
  @Bean
  public Job cancelJob() {
    return new JobBuilder("dailyCancelEnrollmentJob", jobRepository)
        .start(closeCourseStep)
        .next(cancelUnpaidStep)
        .next(cancelWaitlistStep)
        .build();
  }
}
