package com.example.assignment.batch.step;

import com.example.assignment.batch.listener.CancelWaitlistStepListener;
import com.example.assignment.student.entity.Enrollment;
import com.example.assignment.student.repository.EnrollmentRepository;
import com.example.assignment.student.type.EnrollmentStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CancelWaitlistStepConfig  {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final EnrollmentRepository enrollmentRepository;
  private final CancelWaitlistStepListener cancelWaitlistStepListener;

  @Bean
  public Step cancelWaitlistStep() {
    return new StepBuilder("cancelWaitlistStep", jobRepository)
        .<Enrollment, Enrollment>chunk(100, transactionManager)
        .reader(cancelWaitlistReader())
        .processor(cancelWaitlistProcessor())
        .writer(cancelWaitlistWriter())
        .listener(cancelWaitlistStepListener)
        .faultTolerant()

        // 데이터 자체의 문제 → 해당 건 skip 후 다음 건 처리
        .skip(DataIntegrityViolationException.class)    // DB 제약 조건(PK, FK, Unique 등) 위반
        .skip(InvalidDataAccessApiUsageException.class) // 잘못된 JPQL 또는 파라미터 사용
        .skipLimit(10)                                  // 최대 10건까지 skip 허용, 초과 시 Job 실패 처리

        // 일시적인 문제 → 최대 3번 재시도 후 실패 시 skip 으로 처리
        .retry(TransientDataAccessException.class)       // DB 일시적 오류 (연결 끊김, 세션 만료 등)
        .retry(QueryTimeoutException.class)              // 쿼리 실행 시간 초과
        .retry(DataAccessResourceFailureException.class) // DB 서버 연결 실패 (네트워크 오류 등)
        .retry(JpaSystemException.class)                 // JPA 내부 시스템 오류
        .retryLimit(3)                                   // 최대 3번까지 재시도

        // 재시도 횟수(3번) 모두 소진 시 → skip 으로 처리
        .skip(TransientDataAccessException.class)
        .skip(QueryTimeoutException.class)
        .skip(DataAccessResourceFailureException.class)
        .skip(JpaSystemException.class)

        .build();
  }

  /**
   * 강의 시작 3일 전까지 대기(WAITLISTED) 상태인 수강신청 건 조회
   */
  @Bean
  public ItemReader<Enrollment> cancelWaitlistReader() {
    LocalDate threeDaysLater = LocalDate.now().plusDays(3);
    List<Enrollment> list = enrollmentRepository.findAllBeforeCourseStart(
        threeDaysLater,
        EnrollmentStatus.WAITLISTED
    );

    return new ListItemReader<>(list);
  }

  /**
   * 대기열 만료 취소 처리
   * 정원을 차지하지 않으므로 정원 복구 없이 상태만 변경
   */
  @Bean
  public ItemProcessor<Enrollment, Enrollment> cancelWaitlistProcessor() {
    return enrollment -> {
      enrollment.cancel();
      return enrollment;
    };
  }

  /**
   * 처리된 수강신청 건 저장
   */
  @Bean
  public ItemWriter<Enrollment> cancelWaitlistWriter() {
    return chunk -> enrollmentRepository.saveAll(chunk.getItems());
  }
}
