# ☘️ 프로젝트 개요

크리에이터(강사)가 강의를 개설하고, 클래스메이트(수강생)가 원하는 강의에 수강 신청할 수 있는 수강 신청 시스템입니다.

### 주요 기능
- 강사가 강의를 등록하고 상태(DRAFT → OPEN → CLOSED)를 관리
- 수강생이 강의를 검색하고 수강 신청
- 결제 확정 처리 및 수강 취소 (결제 후 7일 이내)
- 정원 초과 시 대기열(Waitlist) 자동 등록 및 취소 발생 시 순서대로 자동 승격
- 동시에 여러 사람이 마지막 자리에 신청하는 경합 상황을 비관적 락으로 처리

# ☘️ 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 |
| ORM | Spring Data JPA, QueryDSL 5.1.0 |
| Database | MySQL |
| Build | Gradle |
| API 문서 | Swagger (springdoc-openapi 2.8.0) |
| 기타 | Lombok, Spring Validation |

--- 

# ☘️ 실행 방법

### 환경 설정

`src/main/resources` 경로에 `secret.yml` 파일을 생성하고 아래 내용을 환경에 맞게 수정합니다.

```yaml
DEV:
  DATABASE:
    URL: jdbc:mysql://localhost:3306/{데이터베이스명}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true
    USERNAME: {MySQL 사용자명}
    PASSWORD: {MySQL 비밀번호}

PROD:
  DATABASE:
    URL: jdbc:mysql://mysql:3306/{데이터베이스명}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true
    USERNAME: {MySQL 사용자명}
    PASSWORD: {MySQL 비밀번호}
```

### 프로파일 설정

| 프로파일 | 설명 |
|---|---|
| `dev` | 로컬 개발 환경 |
| `prod` | 운영 환경 |

### API 문서 확인

서버 실행 후 아래 URL 에서 Swagger UI 를 통해 API 를 확인하고 테스트할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```
---

# ☘️ 데이터 모델 설명

### ERD
<img width="1125" height="694" alt="image" src="https://github.com/user-attachments/assets/eea2cf82-9378-4163-a8db-bbb6874de6e2" />

### Enum 상태값

**CourseStatus (강의 상태)**

| 값 | 설명 |
|---|---|
| DRAFT | 초안 — 수강 신청 불가 |
| OPEN | 모집 중 — 수강 신청 가능 |
| CLOSED | 모집 마감 — 수강 신청 불가, 대기 신청 가능 |

**EnrollmentStatus (수강신청 상태)**

| 값 | 설명 |
|---|---|
| PENDING | 신청 완료, 결제 대기 |
| CONFIRMED | 결제 완료, 수강 확정 |
| CANCELLED | 취소됨 |
| WAITLISTED | 정원 초과로 인한 대기 중 |

**UserRole (사용자 역할)**

| 값 | 설명 |
|---|---|
| CREATORS | 강사 — 강의 등록 및 관리 가능 |
| STUDENT | 수강생 — 강의 수강 신청 가능 |

---

# ☘️ API 목록 및 예시

### 강의 API

| 메서드 | URL | 설명 | 권한 |
|---|---|---|---|
| POST | /api/v1/course | 강의 등록 | 강사 |
| GET | /api/v1/course | 강의 목록 조회 | 전체 |
| GET | /api/v1/course/{courseId} | 강의 상세 조회 | 전체 |
| PATCH | /api/v1/course/{userId}/{courseId}/status | 강의 상태 변경 | 강사 |
| GET | /api/v1/course/{userId}/{courseId}/enrollments | 강의별 수강생 목록 조회 | 강사 |

### 수강신청 API

| 메서드 | URL | 설명 | 권한 |
|---|---|---|---|
| POST | /api/v1/enrollment/{userId}/{courseId} | 수강 신청 | 수강생 |
| PATCH | /api/v1/enrollment/{userId}/{enrollmentId}/pay | 결제 확정 | 수강생 |
| GET | /api/v1/enrollment/{userId} | 내 수강 신청 목록 조회 | 수강생 |
| DELETE | /api/v1/enrollment/{userId}/{enrollmentId} | 수강 취소 | 수강생 |

---

# ☘️ 요구사항 해석 및 가정

### 서비스 정책
```
### 강의 정책

- 강의 등록은 강사만 가능하며, 동일한 강의를 중복 등록할 수 없음
- 강의 상태는 초안 → 모집 중 → 모집 마감 순으로 변경 가능하며, 마감된 강의는 다시 모집 중으로 변경할 수 없음
- 초안 상태에서 바로 마감 처리하는 것은 가능
- 수강 인원이 최대 정원에 도달하면 자동으로 모집 마감 처리되며, 취소가 발생하여 자리가 생기면 자동으로 모집 중으로 복귀
- 수강 기간이 종료된 강의는 상태 변경 불가
- 강의 상태 변경은 해당 강의를 개설한 강사 본인만 가능

### 수강 신청 정책

- 수강 신청은 수강생만 가능하며, 강사는 수강 신청 불가
- 아래 경우 수강 신청 불가
  - 아직 모집이 시작되지 않은 강의 (초안 상태)
  - 강의가 이미 시작된 경우
  - 이미 신청 중이거나 수강 확정된 강의 (대기 중인 경우 포함)
- 취소한 강의는 재신청 가능
- 정원이 꽉 찬 경우 신청을 거부하지 않고 대기열에 자동 등록되며, 취소가 발생하면 신청 순서대로 자동으로 결제 대기 상태로 전환

### 결제 정책

- 결제는 결제 대기 상태인 본인 수강 신청 건만 가능
- 이미 결제가 완료되었거나 취소된 건은 결제 불가
- 결제 완료 시 결제 대기에서 수강 확정 상태로 전환

### 취소 정책

- 결제 전 상태는 언제든 취소 가능
  - 수강 신청 후 강의 시작 하루 전까지 결제하지 않으면 자동 취소 처리
- 결제 완료 후에는 아래 두 조건을 모두 만족하는 경우에만 취소 가능
  - 결제 완료 후 7일 이내
  - 강의 시작 하루 전까지
- 위 조건 중 하나라도 만족하지 않으면 취소 불가
  - 예) 결제 후 7일이 지났다면 강의 시작 전이라도 취소 불가
  - 예) 결제 후 7일 이내라도 강의 시작 하루 전이라면 취소 불가
- 취소 시 대기 중인 첫 번째 신청자가 자동으로 결제 대기 상태로 전환
- 대기 중 취소 시 정원에 영향을 주지 않음

### 대기열 정책

- 정원 초과 시 자동으로 대기열에 등록
- 대기 순서는 신청 시간 기준 선착순으로 처리
- 취소 발생 시 대기 순서대로 자동으로 결제 대기 상태로 전환
- 강의 시작 3일 전까지 대기 유지되며, 이후 대기 신청은 자동으로 취소 처리
  - 대기 중 자동 취소 시 정원에 영향을 주지 않음
  - 강의 시작 3일 전 이후에는 대기 신청 불가
- 대기 중 취소는 언제든 가능
```

### 1. 강의 등록

**요구사항** 
> 강의 등록: 제목, 설명, 가격, 정원(최대 수강 인원), 수강 기간(시작일~종료일)


**해석 및 구현**

요청 시 강사 식별을 위해 `userId` 를 함께 받도록 구현
동일 강사라도 기간이 다르면 별개의 강의로 인정될 수 있고, 정원이 다른 경우도 존재할 수 있다고 판단
제목, 설명, 가격, 정원, 기간, 상태가 모두 일치하는 경우에만 중복으로 판단하여 등록을 거부

## 

### 2. 강의 상태

**요구사항**
> 강의 상태: DRAFT → OPEN → CLOSED

**해석 및 구현**

단순 순차 흐름이 아닌 실제 운영 상황을 고려해 아래와 같이 해석

| 전환 | 허용 여부 | 이유 |
|---|---|---|
| DRAFT → OPEN | 허용 | 초안을 모집 중으로 전환 |
| DRAFT → CLOSED | 허용 | 개설 취소 등 즉시 마감이 필요한 경우를 고려 |
| OPEN → CLOSED | 허용 | 강사의 조기 마감 |
| CLOSED → OPEN | 불가 | 마감된 강의의 재모집은 허용하지 않음 |
| OPEN → DRAFT, CLOSED → DRAFT| 불가 | 역방향 전환 불가 |

정원이 꽉 차면 자동으로 `CLOSED` 로 전환하고, 수강 취소로 자리가 생기면 자동으로 `OPEN` 으로 복귀하도록 구현

## 

### 3. 강의 목록 조회

**요구사항**
> 강의 목록 조회 (상태 필터 가능)

**해석 및 구현**

상태 필터 외에도 강사명, 제목, 가격 범위, 기간으로도 필터링할 수 있도록 확장하여 구현
모든 조건은 선택값이며 조건을 지정하지 않으면 전체 조회됨. QueryDSL 동적 쿼리로 처리

## 

### 4. 강의 상세 조회

**요구사항**
> 강의 상세 조회 (현재 신청 인원 포함)

**해석 및 구현**

현재 신청 인원은 별도 집계 쿼리 없이 `Course.enrollmentCnt` 필드를 활용
수강 신청/취소 시 해당 값을 직접 증감하여 항상 최신 상태를 유지하도록 구현

## 

### 5. 수강 신청

**요구사항**
> 수강 신청: 사용자가 강의에 신청

**해석 및 구현**

아래 조건을 모두 만족해야 신청 가능하도록 구현함.

- 강사는 수강 신청 불가
- `DRAFT` 상태 강의는 신청 불가
- 강의 시작일이 지난 강의는 신청 불가 (요구사항에 명시되지 않았으나 현실적인 제약으로 판단해 추가)
- 이미 신청(`PENDING`, `CONFIRMED`, `WAITLISTED`)한 강의는 재신청 불가
- 취소(`CANCELLED`) 후 재신청은 가능

> 정원이 꽉 찬 경우 신청을 거부하지 않고 대기열(`WAITLISTED`)에 자동 등록.
> 취소가 발생하면 대기 신청 순서대로 자동으로 `PENDING` 으로 승격

## 

### 6. 수강신청 상태

**요구사항**
> 신청 상태: PENDING → CONFIRMED → CANCELLED

**해석 및 구현**

대기열 기능 구현을 위해 `WAITLISTED` 상태를 추가함.

| 상태 | 설명 |
|---|---|
| PENDING | 신청 완료, 결제 대기 |
| CONFIRMED | 결제 완료, 수강 확정 |
| CANCELLED | 취소됨 |
| WAITLISTED | 정원 초과로 인한 대기 중 (추가) |

## 

### 7. 결제 확정 처리

**요구사항**
> 결제 확정 처리 (외부 결제 시스템 연동은 불필요 — 단순 상태 변경으로 대체)

**해석 및 구현**

결제 확정 API 는 외부 PG 사 콜백을 받는 시점을 가정하고 `PENDING → CONFIRMED` 상태 전환만 처리. 
이미 결제되었거나 취소된 건은 결제 불가.

## 

### 8. 수강 취소

**요구사항**
> 수강 취소

**해석 및 구현**

- `PENDING`, `WAITLISTED` 상태는 결제 전이므로 언제든 취소 가능
- `CONFIRMED` 상태는 결제 완료 시점 기준 7일 이내만 취소 가능
- 취소 발생 시 대기열에 있는 첫 번째 사람을 자동으로 `PENDING` 으로 승격
- `WAITLISTED` 상태 취소는 정원을 차지하지 않으므로 정원 복구 없이 상태만 변경
- 결제 기한 만료 처리: 수강 신청 후 3일 이내 결제하지 않으면 자동으로 취소 처리 예정

결제 완료 시점 기준을 `updatedAt` 으로 사용
`confirm()` 호출 시 JPA Auditing 이 `updatedAt` 을 갱신하며, 현재 `Enrollment.updatedAt` 을 변경하는 지점은 `confirm()` 과 `cancel()` 뿐이므로 기준이 변질될 가능성이 없다고 판단

## 

### 9. 정원 관리

**요구사항**

> 강의별 최대 정원을 초과한 신청은 거부
> 동시에 여러 사람이 마지막 자리에 신청하는 경우를 고려

**해석 및 구현**

정원 초과 시 신청을 거부하는 대신 대기열(`WAITLISTED`)에 자동 등록하도록 구현함.

동시성 문제는 비관적 락(`SELECT ... FOR UPDATE`)으로 처리
수강 신청과 수강 취소 시 `Course` 를 락을 걸고 조회하여 동시에 여러 요청이 들어와도 순차적으로 처리되도록 함.

## 

### 10. 인증/인가

**요구사항에 명시되지 않은 부분**

Spring Security 는 구현 범위에서 제외함. 현재는 요청 파라미터로 `userId` 를 직접 받아 사용자를 식별.

---

# ☘️ 설계 결정과 이유

### 1. 동시성 제어 — 비관적 락 선택

> 수강 신청은 여러 사용자가 동시에 마지막 자리에 신청하는 경합 상황이 발생할 수 있음

이를 해결하기 위해 비관적 락과 낙관적 락 두 가지 방식을 검토
| 낙관적 락 | 충돌 발생 시 재시도하는 방식으로, 충돌이 잦은 수강 신청 환경에서는 재시도 비용이 커질 수 있음 |
|---|---|
| 비관적 락 | 조회 시점에 락을 걸어 순차 처리를 보장하는 방식으로, 데이터 정합성을 확실히 보장할 수 있음|

- 수강 신청처럼 충돌 가능성이 높은 환경에서는 재시도 비용보다 순차 처리가 더 적합하다고 판단하여 비관적 락을 선택
- 락은 `Course` 의 수강 인원(`enrollmentCnt`)을 수정하는 지점에만 적용
- 결제는 본인의 수강 신청 건만 수정하므로 경합이 발생하지 않아 락을 적용하지 않음

## 
### 2. 정원 관리 — enrollmentCnt 필드 직접 관리

> 현재 수강 인원을 별도 집계 쿼리 없이 `Course.enrollmentCnt` 필드를 직접 증감하는 방식으로 관리함.

집계 쿼리(`COUNT`) 방식과 비교했을 때 아래와 같은 이유로 필드 직접 관리 방식을 선택함.

- 강의 상세 조회 시 매번 집계 쿼리가 발생하지 않아 성능상 유리함
- 수강 신청/취소 시점에 즉시 반영되어 항상 최신 상태를 유지할 수 있음
- 비관적 락과 함께 사용하면 동시성 환경에서도 정확한 값을 보장할 수 있음

## 
### 3. 대기열 — 별도 테이블 없이 EnrollmentStatus 로 관리

> 대기열을 별도 테이블로 분리하지 않고 `Enrollment` 의 상태값(`WAITLISTED`)으로 관리함.

- 대기열도 결국 수강 신청의 한 상태이므로 같은 테이블에서 관리하는 것이 자연스러움
- 별도 테이블을 만들면 취소/승격 시 두 테이블을 동시에 수정해야 하는 복잡도가 생김
- 신청 시간(`createdAt`) 기준으로 정렬하면 선착순 처리가 간단하게 구현 가능

## 
### 4. 취소 기간 기준 — updatedAt 활용

> 결제 완료 시점을 별도 컬럼으로 관리하지 않고 JPA Auditing 의 `updatedAt` 을 활용함.

결제 확정(`confirm()`) 호출 시 JPA Auditing 이 `updatedAt` 을 자동으로 갱신하며, 현재 `Enrollment` 의 `updatedAt` 을 변경하는 지점은 결제 확정과 수강 취소뿐임. 
취소 이후에는 재접근이 불가하므로 기준이 변질될 가능성이 없다고 판단함.

다만 향후 결제 외 다른 이유로 `updatedAt` 이 갱신되는 로직이 추가된다면 결제 시점을 저장하는 별도 컬럼(`confirmedAt`)으로 분리하는 것이 더 안전함.

## 
### 5. 도메인 메서드 — 비즈니스 규칙을 엔티티에 응집

서비스 레이어에서 엔티티의 상태를 직접 조작하지 않고, 엔티티 내부에 도메인 메서드를 두어 비즈니스 규칙을 응집시킴.

```
// 서비스에서 직접 조작하는 방식
course.setCourseStatus(CourseStatus.CLOSED);

// 도메인 메서드를 활용하는 방식
course.closeCourse();
course.increaseEnrollmentCnt();
```

- 동일한 규칙이 여러 서비스에 흩어지는 것을 방지할 수 있음
- 규칙이 변경될 때 엔티티 한 곳만 수정하면 됨
- 메서드명으로 의도가 명확하게 드러남

단, 예외 처리는 엔티티에 두지 않고 서비스 레이어에서 담당하도록 분리함. 엔티티가 애플리케이션 계층의 예외 클래스에 의존하면 순수한 도메인 객체로서의 역할이 훼손되기 때문.

## 
### 6. QueryDSL — 동적 쿼리 처리

강의 목록 조회는 강사명, 제목, 가격 범위, 기간, 상태 등 다양한 조건을 선택적으로 조합할 수 있어야 함. `@Query` JPQL 로는 조건마다 분기 처리가 필요해 유지보수가 어려워지므로 QueryDSL 을 도입함.

QueryDSL 은 조건이 `null` 이면 해당 조건을 자동으로 제외하는 방식으로 동작하여, 조건이 추가되어도 메서드 하나만 추가하면 되므로 확장성이 높음.

## 
### 7. N+1 문제 방지 — fetchJoin 적용

`Enrollment` 조회 시 연관된 `Course`, `User` 를 함께 조회해야 하는데, 지연 로딩(`LAZY`) 설정으로 인해 각 엔티티마다 추가 쿼리가 발생하는 N+1 문제가 생길 수 있음.

이를 방지하기 위해 조회 쿼리에 `JOIN FETCH` 를 적용하여 한 번의 쿼리로 필요한 데이터를 모두 가져오도록 처리함.

---

# ☘️ 미구현 / 제약사항

### 미구현 항목

**인증/인가**
Spring Security 를 구현 범위에서 제외함. 로그인 및 인증/인가 기능이 없어 현재는 요청 파라미터로 `userId` 를 직접 받아 사용자를 식별하고 있음. Security 도입 후 인증 토큰 기반으로 변경 예정.

### 제약사항

현재 로그인 기능이 없어 요청 파라미터로 `userId` 를 직접 전달하는 방식으로 사용자를 식별함. 이로 인해 타인의 `userId` 를 입력하면 해당 사용자의 수강 신청 내역에 접근할 수 있는 보안 취약점이 존재함.

---

# ☘️ 테스트 실행 방법

### Swagger UI 를 통한 API 테스트

서버 실행 후 아래 URL 에서 Swagger UI 에 접속하여 API 를 테스트할 수 있음.

```
http://localhost:8080/swagger-ui/index.html
```

### 테스트 데이터 준비

아래 SQL 을 실행하여 테스트용 사용자와 강의 데이터를 삽입함.

```sql
-- 사용자 데이터 (강사 3명, 수강생 12명)
INSERT INTO user (name, user_role) VALUES
('강사1', 'CREATORS'),
('강사2', 'CREATORS'),
('강사3', 'CREATORS'),
('학생1', 'STUDENT'),
('학생2', 'STUDENT'),
('학생3', 'STUDENT'),
('학생4', 'STUDENT'),
('학생5', 'STUDENT'),
('학생6', 'STUDENT'),
('학생7', 'STUDENT'),
('학생8', 'STUDENT'),
('학생9', 'STUDENT'),
('학생10', 'STUDENT'),
('학생11', 'STUDENT'),
('학생12', 'STUDENT');

-- 강의 데이터
INSERT INTO course (
    title, description, amount, personnel,
    start_period_at, end_period_at,
    user_id, course_status, enrollment_cnt,
    created_at, updated_at
) VALUES
('Spring Boot 입문', 'Spring Boot 기초 강의', 50000, 5, '2026-05-01', '2026-07-31', 1, 'OPEN', 2, NOW(), NOW()),
('JPA 완전정복', 'JPA 심화 학습', 80000, 3, '2026-05-01', '2026-07-31', 1, 'CLOSED', 3, NOW(), NOW()),
('QueryDSL 마스터', 'QueryDSL 동적 쿼리', 70000, 2, '2026-05-01', '2026-07-31', 2, 'CLOSED', 2, NOW(), NOW()),
('React 기초', 'React 컴포넌트와 Hook', 45000, 10, '2026-01-01', '2026-04-30', 2, 'OPEN', 3, NOW(), NOW()),
('TypeScript 심화', '타입 시스템 완전 정복', 55000, 10, '2026-06-01', '2026-08-31', 3, 'DRAFT', 0, NOW(), NOW()),
('Docker 입문', '컨테이너 기초', 50000, 10, '2026-05-01', '2026-07-31', 3, 'OPEN', 1, NOW(), NOW());

-- 수강신청 데이터
INSERT INTO enrollment (course_id, user_id, enrollment_status, created_at, updated_at) VALUES
(1, 4, 'PENDING', NOW(), NOW()),
(1, 5, 'CONFIRMED', NOW(), NOW()),
(2, 6, 'CONFIRMED', NOW(), NOW()),
(2, 7, 'CONFIRMED', NOW(), NOW()),
(2, 8, 'CONFIRMED', NOW(), NOW()),
(3, 9, 'CONFIRMED', NOW(), NOW()),
(3, 10, 'PENDING', NOW(), NOW()),
(3, 11, 'WAITLISTED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(3, 12, 'WAITLISTED', NOW(), NOW()),
(4, 4, 'CONFIRMED', NOW(), NOW()),
(4, 5, 'CONFIRMED', NOW(), NOW()),
(4, 6, 'CONFIRMED', NOW(), NOW()),
(6, 4, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY));
```
### 테스트 데이터 구조

**사용자 (User)**

| userId | 이름 | 역할 |
|---|---|---|
| 1 | 강사1 | 강사 |
| 2 | 강사2 | 강사 |
| 3 | 강사3 | 강사 |
| 4 | 학생1 | 수강생 |
| 5 | 학생2 | 수강생 |
| 6 | 학생3 | 수강생 |
| 7 | 학생4 | 수강생 |
| 8 | 학생5 | 수강생 |
| 9 | 학생6 | 수강생 |
| 10 | 학생7 | 수강생 |
| 11 | 학생8 | 수강생 |
| 12 | 학생9 | 수강생 |
| 13 | 학생10 | 수강생 |
| 14 | 학생11 | 수강생 |
| 15 | 학생12 | 수강생 |

**강의 (Course)**

| courseId | 제목 | 강사 | 상태 | 정원 | 현재 인원 | 시작일 | 종료일 |
|---|---|---|---|---|---|---|---|
| 1 | Spring Boot 입문 | 강사1 | 모집 중 | 5 | 2 | 2026-05-01 | 2026-07-31 |
| 2 | JPA 완전정복 | 강사1 | 모집 마감 | 3 | 3 | 2026-05-01 | 2026-07-31 |
| 3 | QueryDSL 마스터 | 강사2 | 모집 마감 | 2 | 2 | 2026-05-01 | 2026-07-31 |
| 4 | React 기초 | 강사2 | 모집 중 | 10 | 3 | 2026-01-01 | 2026-04-30 |
| 5 | TypeScript 심화 | 강사3 | 초안 | 10 | 0 | 2026-06-01 | 2026-08-31 |
| 6 | Docker 입문 | 강사3 | 모집 중 | 10 | 1 | 2026-05-01 | 2026-07-31 |

**수강신청 (Enrollment)**

| enrollmentId | 강의 | 수강생 | 상태 | 비고 |
|---|---|---|---|---|
| 1 | Spring Boot 입문 (1) | 학생1 (4) | 결제 대기 | |
| 2 | Spring Boot 입문 (1) | 학생2 (5) | 수강 확정 | |
| 3 | JPA 완전정복 (2) | 학생3 (6) | 수강 확정 | |
| 4 | JPA 완전정복 (2) | 학생4 (7) | 수강 확정 | |
| 5 | JPA 완전정복 (2) | 학생5 (8) | 수강 확정 | |
| 6 | QueryDSL 마스터 (3) | 학생6 (9) | 수강 확정 | |
| 7 | QueryDSL 마스터 (3) | 학생7 (10) | 결제 대기 | |
| 8 | QueryDSL 마스터 (3) | 학생8 (11) | 대기 중 | 1시간 전 신청 (승격 우선순위 1순위) |
| 9 | QueryDSL 마스터 (3) | 학생9 (12) | 대기 중 | 승격 우선순위 2순위 |
| 10 | React 기초 (4) | 학생1 (4) | 수강 확정 | 이미 시작된 강의 |
| 11 | React 기초 (4) | 학생2 (5) | 수강 확정 | 이미 시작된 강의 |
| 12 | React 기초 (4) | 학생3 (6) | 수강 확정 | 이미 시작된 강의 |
| 13 | Docker 입문 (6) | 학생1 (4) | 수강 확정 | 8일 전 결제 (취소 기간 만료) |

### 주요 테스트 시나리오

---

**강의 조회**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 전체 강의 목록 조회 | `GET /api/v1/course` |
| 2 | 강사명으로 필터링 (course 1, 2 조회) | `GET /api/v1/course?creatorName=강사1` |
| 3 | 강의 상태로 필터링 (course 1, 4, 6 조회) | `GET /api/v1/course?courseStatus=OPEN` |
| 4 | 가격 범위로 필터링 | `GET /api/v1/course?minAmount=50000&maxAmount=80000` |
| 5 | 강의 상세 조회 | `GET /api/v1/course/1` |

**강의 조회 실패 케이스**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 존재하지 않는 강의 조회 → 실패 | `GET /api/v1/course/999` |

---

**강의 상태 변경**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 변경 전 강의 상태 확인 (DRAFT) | `GET /api/v1/course/5` |
| 2 | 초안 → 모집 중으로 변경 | `PATCH /api/v1/course/3/5/status` → `{ "courseStatus": "OPEN" }` |
| 3 | 변경 후 강의 상태 확인 (OPEN) | `GET /api/v1/course/5` |
| 4 | 변경 전 강의 상태 확인 (OPEN) | `GET /api/v1/course/6` |
| 5 | 모집 중 → 모집 마감으로 변경 | `PATCH /api/v1/course/3/6/status` → `{ "courseStatus": "CLOSED" }` |
| 6 | 변경 후 강의 상태 확인 (CLOSED) | `GET /api/v1/course/6` |

**강의 상태 변경 실패 케이스**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 마감된 강의 재오픈 시도 → 실패 | `PATCH /api/v1/course/1/2/status` → `{ "courseStatus": "OPEN" }` |
| 2 | 이미 모집 중인 강의를 모집 중으로 변경 시도 → 실패 | `PATCH /api/v1/course/1/1/status` → `{ "courseStatus": "OPEN" }` |
| 3 | 이미 마감된 강의를 마감 처리 시도 → 실패 | `PATCH /api/v1/course/1/2/status` → `{ "courseStatus": "CLOSED" }` |
| 4 | 타인 강의 상태 변경 시도 → 실패 | `PATCH /api/v1/course/2/1/status` → `{ "courseStatus": "CLOSED" }` |
| 5 | 수강생이 강의 상태 변경 시도 → 실패 | `PATCH /api/v1/course/4/1/status` → `{ "courseStatus": "CLOSED" }` |

---

**강의별 수강생 목록 조회**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 강사1이 본인 강의 수강생 목록 조회 | `GET /api/v1/course/1/1/enrollments` |

**강의별 수강생 목록 조회 실패 케이스**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 강사2가 강사1 강의 수강생 목록 조회 시도 → 실패 | `GET /api/v1/course/2/1/enrollments` |
| 2 | 수강생이 수강생 목록 조회 시도 → 실패 | `GET /api/v1/course/4/1/enrollments` |

---

**수강 신청**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 신청 전 강의 상세 조회 (현재 신청 인원 2명 확인) | `GET /api/v1/course/1` |
| 2 | 학생10이 정원 여유 있는 강의 신청 → PENDING | `POST /api/v1/enrollment/13/1` |
| 3 | 신청 후 강의 상세 조회 (신청 인원 3명으로 증가 확인) | `GET /api/v1/course/1` |
| 4 | 신청 후 학생10 수강 신청 목록 확인 (PENDING 확인) | `GET /api/v1/enrollment/13` |
| 5 | 신청 전 강의 상세 조회 (정원 꽉 찬 상태 확인) | `GET /api/v1/course/2` |
| 6 | 학생11이 정원 꽉 찬 강의 신청 → WAITLISTED | `POST /api/v1/enrollment/14/2` |
| 7 | 신청 후 학생11 수강 신청 목록 확인 (WAITLISTED 확인) | `GET /api/v1/enrollment/14` |

**수강 신청 실패 케이스**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 강사1이 수강 신청 시도 → 실패 | `POST /api/v1/enrollment/1/6` |
| 2 | 학생12가 초안 강의 신청 시도 → 실패 | `POST /api/v1/enrollment/15/5` |
| 3 | 학생12가 이미 시작된 강의 신청 시도 → 실패 | `POST /api/v1/enrollment/15/4` |
| 4 | 학생1이 이미 신청한 강의 재신청 시도 → 실패 | `POST /api/v1/enrollment/4/1` |

---

**결제**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 결제 전 학생1 수강 신청 목록 확인 (enrollmentId 1 PENDING 확인) | `GET /api/v1/enrollment/4` |
| 2 | 학생1이 enrollmentId 1 결제 확정 | `PATCH /api/v1/enrollment/4/1/pay` |
| 3 | 결제 후 학생1 수강 신청 목록 확인 (CONFIRMED 로 변경 확인) | `GET /api/v1/enrollment/4` |

**결제 실패 케이스**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 학생2의 이미 결제된 건 재결제 시도 → 실패 | `PATCH /api/v1/enrollment/5/2/pay` |
| 2 | 학생7의 수강 신청 목록 확인 (enrollmentId 7 PENDING 확인) | `GET /api/v1/enrollment/10` |
| 3 | 학생7이 enrollmentId 7 취소 | `DELETE /api/v1/enrollment/10/7` |
| 4 | 취소된 enrollmentId 7 결제 시도 → 실패 | `PATCH /api/v1/enrollment/10/7/pay` |

---

**수강 취소**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 취소 전 학생7 수강 신청 목록 확인 (enrollmentId 7 PENDING 확인) | `GET /api/v1/enrollment/10` |
| 2 | 취소 전 QueryDSL 마스터 수강생 목록 확인 (대기자 상태 확인) | `GET /api/v1/course/2/3/enrollments` |
| 3 | 학생7이 결제 전(PENDING) enrollmentId 7 취소 | `DELETE /api/v1/enrollment/10/7` |
| 4 | 취소 후 학생7 수강 신청 목록 확인 (CANCELLED 로 변경 확인) | `GET /api/v1/enrollment/10` |
| 5 | 취소 후 QueryDSL 마스터 수강생 목록 확인 (학생8 WAITLISTED → PENDING 승격 확인) | `GET /api/v1/course/2/3/enrollments` |
| 6 | 취소 전 QueryDSL 마스터 수강생 목록 확인 (현재 상태 확인) | `GET /api/v1/course/2/3/enrollments` |
| 7 | 학생6이 결제 후 7일 이내 enrollmentId 6 취소 | `DELETE /api/v1/enrollment/9/6` |
| 8 | 취소 후 학생6 수강 신청 목록 확인 (CANCELLED 로 변경 확인) | `GET /api/v1/enrollment/9` |
| 9 | 취소 후 QueryDSL 마스터 수강생 목록 확인 (학생9 WAITLISTED → PENDING 승격 확인) | `GET /api/v1/course/2/3/enrollments` |
| 10 | 취소 전 학생9 수강 신청 목록 확인 (enrollmentId 9 WAITLISTED 확인) | `GET /api/v1/enrollment/12` |
| 11 | 학생9가 대기 중(WAITLISTED) enrollmentId 9 취소 | `DELETE /api/v1/enrollment/12/9` |
| 12 | 취소 후 학생9 수강 신청 목록 확인 (CANCELLED 로 변경 확인) | `GET /api/v1/enrollment/12` |

**수강 취소 실패 케이스**

| 순서 | 시나리오 | 방법 |
|---|---|---|
| 1 | 학생1의 수강 신청 목록 확인 (enrollmentId 13 CONFIRMED, 8일 전 결제 확인) | `GET /api/v1/enrollment/4` |
| 2 | 결제 후 7일 초과 취소 시도 → 실패 | `DELETE /api/v1/enrollment/4/13` |
| 3 | 학생1이 학생2의 enrollmentId 2 취소 시도 → 실패 | `DELETE /api/v1/enrollment/4/2` |
| 4 | 학생7 수강 신청 목록 확인 (enrollmentId 7 CANCELLED 확인) | `GET /api/v1/enrollment/10` |
| 5 | 이미 취소된 enrollmentId 7 재취소 시도 → 실패 | `DELETE /api/v1/enrollment/10/7` |
---

# ☘️ AI 활용 범위

본 프로젝트는 Claude를 활용하여 개발하였으며, 활용 범위는 아래와 같음.

### 활용한 부분

- 요구사항 분석 및 설계 방향 논의
- 도메인 모델 설계 및 엔티티 구조 검토
- 비즈니스 로직 정책 논의
- 코드 리뷰 및 개선 포인트 피드백
- 발생한 에러 원인 분석 및 해결 방향 제시
- 커밋 메시지 및 PR 내용 정리

### 직접 구현한 부분

- 모든 코드 작성 및 수정
- 설계 방향 및 정책 최종 결정
- 디버깅 및 테스트
