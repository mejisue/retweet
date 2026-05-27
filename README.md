# Retweet

Twitter를 모티브로 한 소셜 미디어 서비스

## 기술 스택

**Frontend**

| 분류 | 기술 |
|------|------|
| UI | React 19, TailwindCSS v4, shadcn/ui |
| 라우팅 | React Router v7 |
| 서버 상태 | TanStack Query v5 |
| 클라이언트 상태 | Zustand |

**Backend**

| 분류 | 기술 |
|------|------|
| 프레임워크 | Spring Boot 3.5, Java 17 |
| 데이터베이스 | MySQL 8 (JPA), Redis 7 |
| 인증 | JWT, GitHub OAuth |
| 파일 스토리지 | AWS S3 |
| 이메일 | Spring Mail |

## 주요 기능

- 게시글 작성·수정·삭제 및 이미지 업로드 (AWS S3)
- 댓글·좋아요
- 프로필 조회·수정
- 이메일 회원가입 / GitHub OAuth 로그인
- 비밀번호 찾기 (이메일 인증)

## 구조

```
retweet/
├── front/      # React + TypeScript + Vite
└── backend/    # Spring Boot + MySQL + Redis
```

## 로컬 실행

### 사전 요구사항

- Node.js 20+, pnpm
- JDK 17+
- Docker

### Backend

```bash
cd backend

# MySQL + Redis 컨테이너 실행
docker compose up -d

# 서버 실행 (포트 8080)
./gradlew bootRun
```

### Frontend

```bash
cd front
pnpm install
pnpm dev    # http://localhost:5173
```
