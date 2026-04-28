package com.example.assignment.batch.step;

import com.example.assignment.batch.listener.CancelUnpaidStepListener;
import com.example.assignment.creator.entity.Course;
import com.example.assignment.creator.repository.CourseRepository;
import com.example.assignment.student.entity.Enrollment;
import com.example.assignment.student.repository.EnrollmentRepository;
import com.example.assignment.student.type.EnrollmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CancelUnpaidStepConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;
  private final CancelUnpaidStepListener cancelUnpaidStepListener;

  @Bean
  public Step cancelUnpaidStep() {
    return new StepBuilder("cancelUnpaidStep", jobRepository)
        .<Enrollment, Enrollment>chunk(100, transactionManager)
        .reader(cancelUnpaidReader())
        .processor(cancelUnpaidProcessor())
        .writer(cancelUnpaidWriter())
        .listener(cancelUnpaidStepListener)
        .faultTolerant()

        // 데이터 자체의 문제 → 해당 건 skip 후 다음 건 처리
        .skip(NoSuchElementException.class)           // 강의 또는 수강신청 데이터가 존재하지 않는 경우
        .skip(DataIntegrityViolationException.class)  // DB 제약 조건(PK, FK, Unique 등) 위반
        .skip(InvalidDataAccessApiUsageException.class) // 잘못된 JPQL 또는 파라미터 사용
        .skipLimit(10)                                // 최대 10건까지 skip 허용, 초과 시 Job 실패 처리

        // 일시적인 문제 → 최대 3번 재시도 후 실패 시 skip 으로 처리
        .retry(TransientDataAccessException.class)      // DB 일시적 오류 (연결 끊김, 세션 만료 등)
        .retry(CannotAcquireLockException.class)        // 비관적 락 획득 실패 (동시 요청으로 인한 경합)
        .retry(QueryTimeoutException.class)             // 쿼리 실행 시간 초과
        .retry(DataAccessResourceFailureException.class) // DB 서버 연결 실패 (네트워크 오류 등)
        .retry(JpaSystemException.class)                // JPA 내부 시스템 오류
        .retryLimit(3)                                  // 최대 3번까지 재시도

        // 재시도 횟수(3번) 모두 소진 시 → skip 으로 처리
        .skip(TransientDataAccessException.class)
        .skip(CannotAcquireLockException.class)
        .skip(QueryTimeoutException.class)
        .skip(DataAccessResourceFailureException.class)
        .skip(JpaSystemException.class)

        .build();
  }

  /**
   * 강의 시작 3일 전까지 미결제(PENDING) 상태인 수강신청 건 조회
   */
  @Bean
  public ItemReader<Enrollment> cancelUnpaidReader() {
    LocalDate threeDaysLater = LocalDate.now().plusDays(3);
    List<Enrollment> list = enrollmentRepository.findAllBeforeCourseStart(
        threeDaysLater,
        EnrollmentStatus.PENDING
        );

    return new ListItemReader<>(list);
  }

  /**
   * 미결제 취소 처리
   * - 정원 복구
   * - 대기자 자동 승격 (CLOSED → OPEN 복귀된 경우에만)
   * - 상태 CANCELLED 로 변경
   */
  @Bean
  public ItemProcessor<Enrollment, Enrollment> cancelUnpaidProcessor() {
    return enrollment -> {

      Course course = courseRepository.findByIdWithLock(enrollment.getCourse().getCourseId())
          .orElseThrow(NoSuchElementException::new);

      course.decreaseEnrollmentCnt();
      enrollment.cancel();

      // CLOSED → OPEN 으로 복귀된 경우에만 대기자 승격
      if (course.isOpen()) {
        enrollmentRepository.findFirstWaitlistedByCourse(course)
            .ifPresent(waitlisted -> {
              waitlisted.promote();
              course.increaseEnrollmentCnt();
            });
      }

      return enrollment;
    };
  }

  /**
   * 처리된 수강신청 건 저장
   */
  @Bean
  public ItemWriter<Enrollment> cancelUnpaidWriter() {
    return chunk -> enrollmentRepository.saveAll(chunk.getItems());
  }
}
