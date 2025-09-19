# Spring Plus (expert)

[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://adoptium.net/temurin/releases/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Database](https://img.shields.io/badge/Database-MySQL-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Cloud](https://img.shields.io/badge/AWS-S3-232F3E?logo=amazon-aws&logoColor=white)](https://aws.amazon.com/s3/)
[![QueryDSL](https://img.shields.io/badge/QueryDSL-Jakarta-1f6feb)](https://querydsl.com/)

## 🧰 기술 스택

- 언어/런타임: Java 17
- 프레임워크: Spring Boot 3.3.3 (Web, Data JPA, Security, Validation, Actuator)
- 데이터베이스: MySQL (H2 Runtime 의존성 포함)
- 보안/인증: JWT (jjwt), BCrypt
- 데이터 접근: QueryDSL (Jakarta)
- 클라우드: Spring Cloud AWS (S3)
- 빌드: Gradle Wrapper

## 📚 프로젝트 개요

회원 인증(JWT), 사용자 관리, Todo 관리(작성/목록/상세/검색), 파일 업로드(S3 Presigned URL), Todo 매니저 관리 등 실무 기능을 담은 Spring
Boot 3 기반 서버
애플리케이션입니다. 로컬/운영 프로필을 분리하고, 확장 가능한 도메인 구조를 채택했습니다.

## 🚀 주요 기능

- 회원가입/로그인 및 JWT 발급
- 사용자 정보 조회, 비밀번호 변경, 프로필 이미지 파일키 업데이트/조회
- 관리자 권한으로 사용자 역할 변경
- S3 Presigned URL 발급(업로드/다운로드)
- Todo 생성, 목록/상세 조회, 조건 검색
- Todo 매니저 등록/조회/삭제

## 🧩 핵심 도메인

- Auth: 회원가입/로그인, 토큰 발급
- User: 사용자 정보, 비밀번호/프로필 관리
- Admin(UserAdmin): 사용자 권한 변경(ADMIN)
- File: S3 Presigned URL 발급 및 파일키 관리
- Todo: 생성, 목록/상세 조회, 조건 검색
- Manager: Todo 담당자(매니저) 관리
- Log: 요청/비즈니스 로그 기록
- Common: 공통 예외/인증 컨텍스트 등 기반 구성

## 📘 API 명세서 (도메인별 요약)

### Auth

```http
# 회원가입
POST /auth/signup

# 로그인
POST /auth/signin
```

### User

```http
# 사용자 조회
GET /users/{userId}

# 프로필 이미지 파일키 변경
PATCH /users/profile

# 프로필 이미지 조회
GET /users/profile

# 비밀번호 변경
PUT /users
```

### Admin

```http
# 사용자 권한 변경
PATCH /admin/users/{userId}
```

### File

```http
# 업로드용 Presigned URL 발급
POST /files/presigned/upload

# 다운로드용 Presigned URL 발급
POST /files/presigned/download
```

### Todo

```http
# Todo 생성
POST /todos

# Todo 목록 조회
GET /todos?weather={weather}&startDate={yyyy-MM-dd}&endDate={yyyy-MM-dd}&page={1}&size={10}

# Todo 단건 조회
GET /todos/{todoId}

# Todo 검색 (관계 카운트 포함)
GET /todos/search?keyword={keyword}&startDate={yyyy-MM-dd}&endDate={yyyy-MM-dd}&managerName={name}&page={1}&size={10}
```

### Comment

```http
# 댓글 등록
POST /todos/{todoId}/comments

# 댓글 목록 조회
GET /todos/{todoId}/comments
```

### Todo Manager

```http
# 매니저 등록
POST /todos/{todoId}/managers

# 매니저 목록 조회
GET /todos/{todoId}/managers

# 매니저 삭제
DELETE /todos/{todoId}/managers/{managerId}
```

## 🗂️ 프로젝트 구조

```text
src/
├─ main/
│  ├─ java/
│  │  └─ org/example/expert/
│  │     ├─ aop/
│  │     ├─ client/
│  │     ├─ config/
│  │     └─ domain/
│  │        ├─ auth/
│  │        ├─ user/
│  │        ├─ manager/
│  │        ├─ todo/
│  │        ├─ file/
│  │        ├─ comment/
│  │        ├─ log/
│  │        └─ common/
│  └─ resources/
└─ test/
   ├─ java/
   │  └─ org/example/expert/
   │     ├─ config/
   │     ├─ domain/
   │     └─ support/
   └─ resources/
```

## 🧱 도메인 세부 구조

```text
src/main/java/org/example/expert/domain/
└─ user/
   ├─ controller/
   ├─ service/
   ├─ repository/
   ├─ entity/
   ├─ enums/
   └─ dto/
      ├─ request/
      └─ response/
```
