
```markdown
# 블록체인 파트 상세 문서

> 작성자: 김나형 (블록체인 + 보안 담당)  
> 작성일: 2026.04.30

이 문서는 **블록체인 연동 + 영수증 5단계 교차 검증** 모듈에 대한 상세 설명입니다.

---

## 1. 담당 범위

명세서(`Project_proposal_티니페이.pdf`) 기준으로 김나형이 담당하는 영역:

- **블록체인 연동**: Web3j 설정, 스마트 컨트랙트 wrapper, 결제/충전 트랜잭션 실행
- **영수증 5단계 교차 검증**: Replay 방지 → 트랜잭션 성공 여부 → 컨트랙트 주소 → 수신자 → 금액
- **보안**: 어뷰징 탐지, 프롬프트 인젝션 방어 (이건 별도 작업, 추후 진행)

백엔드 팀(임은서, 현예진)은 이 문서에 정의된 `BlockchainService` 인터페이스를 호출하여 결제 흐름을 구현합니다.

---

## 2. 패키지 구조

```
com.tinypay.tinypay.blockchain/
├── config/
│   ├── BlockchainConfig.java         # Web3j, Credentials, GasProvider Bean 등록
│   └── RedisConfig.java              # StringRedisTemplate Bean 등록
├── contracts/
│   ├── MockUSDC.java                 # Web3j 자동 생성 wrapper (ERC-20 + mint)
│   ├── MockUSDC.java.stub.bak        # [임시] 기존 손수 작성 스텁 백업 (통합 테스트 후 삭제 예정)
│   └── TinyPayment.java              # 결제 컨트랙트 wrapper (재생성 예정)
├── service/
│   ├── BlockchainService.java        # 외부 인터페이스 (백엔드 팀이 호출)
│   └── BlockchainServiceImpl.java    # 구현체 (잔액 조회, 송금, 충전, 영수증 검증)
├── verification/
│   ├── ReceiptVerifier.java          # 검증 모듈 인터페이스
│   ├── ReceiptVerifierImpl.java      # 5단계 검증 본체
│   ├── VerificationResult.java       # 검증 결과 객체
│   └── FailReason.java               # 실패 사유 enum
└── exception/
    ├── BlockchainException.java          # 부모 예외
    ├── ReplayAttackException.java        # 1단계 실패
    ├── TransactionFailedException.java   # 2단계 실패
    ├── InvalidContractException.java     # 3단계 실패
    ├── InvalidRecipientException.java    # 4단계 실패
    └── InsufficientPaymentException.java # 5단계 실패
```

---

## 3. 외부 인터페이스 (백엔드 팀 사용 가이드)

`BlockchainService` 인터페이스의 4개 메서드를 통해 블록체인 기능을 사용합니다.

### 3-1. `transferUsdc()` — USDC 결제 실행

```java
String txHash = blockchainService.transferUsdc(
    orderId,         // 주문 ID (UUID 권장, 중복 결제 방지용)
    fromWallet,      // 사용자 지갑 주소
    toWallet,        // 판매자 지갑 주소
    amount,          // 결제 금액 (BigInteger, 6자리 소수점 적용값. 1 USDC = 1_000_000)
    serviceType      // 서비스 종류 (예: "video_generation")
);
```

반환: 트랜잭션 해시 (이후 `verifyReceipt()`에 전달)

### 3-2. `getBalance()` — USDC 잔액 조회

```java
BigDecimal balance = blockchainService.getBalance(walletAddress);
```

반환: USDC 잔액 (소수점 6자리 적용된 BigDecimal)

### 3-3. `mintUsdc()` — USDC 충전

```java
String txHash = blockchainService.mintUsdc(toWallet, amount);
```

반환: 트랜잭션 해시

> ⚠️ 서버 지갑(컨트랙트 owner)만 호출 가능. 일반 사용자가 호출하면 `UnauthorizedMintException` 발생.

### 3-4. `verifyReceipt()` — 영수증 5단계 검증 ⭐

```java
boolean valid = blockchainService.verifyReceipt(txHash, expectedReceiver, expectedAmount);
```

내부적으로 5단계를 순차 검증하며, 실패 시 단계별로 다른 예외를 던집니다.

| 단계 | 검증 내용 | 실패 시 예외 |
|---|---|---|
| 1 | Replay Attack 방지 (Redis 재사용 검사) | `ReplayAttackException` |
| 2 | 트랜잭션 성공 여부 (status == 1) | `TransactionFailedException` |
| 3 | 공식 MockUSDC 컨트랙트 주소 확인 | `InvalidContractException` |
| 4 | 수신자 주소 일치 확인 (Transfer 이벤트) | `InvalidRecipientException` |
| 5 | 결제 금액 충분 확인 (expectedAmount 이상) | `InsufficientPaymentException` |

**fail-fast 원칙**: 한 단계 실패 시 즉시 중단. 5단계 모두 통과한 영수증만 Redis에 1시간 TTL로 등록되어, 이후 재사용 시 1단계에서 차단됨.

호출 예시:

```java
try {
    blockchainService.verifyReceipt(txHash, sellerWallet, expectedAmount);
    // 검증 성공 → 결제 상태 SUCCESS로 업데이트, 서비스 접근 허용
} catch (BlockchainException e) {
    // 모든 검증 실패 케이스를 한 번에 catch 가능 (부모 예외)
    log.error("영수증 검증 실패: {}", e.getMessage());
    // 결제 거부 처리
}
```

---

## 4. 의존성 정보

### 4-1. application.yml 필수 설정

```yaml
blockchain:
  rpc-url: ${BLOCKCHAIN_RPC_URL}
  chain-id: 80002
  mock-usdc-address: ${MOCK_USDC_ADDRESS}
  tiny-payment-address: ${TINY_PAYMENT_ADDRESS}
  server-wallet:
    address: ${SERVER_WALLET_ADDRESS}
    private-key: ${SERVER_WALLET_PRIVATE_KEY}
  gas:
    price: 30000000000  # 30 Gwei
    limit: 300000

spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

### 4-2. 환경변수

`.env` 파일 또는 시스템 환경변수로 주입 (⚠️ Git 커밋 금지):

```
BLOCKCHAIN_RPC_URL=https://polygon-amoy.g.alchemy.com/v2/...
MOCK_USDC_ADDRESS=0x...
TINY_PAYMENT_ADDRESS=0x...
SERVER_WALLET_ADDRESS=0x...
SERVER_WALLET_PRIVATE_KEY=0x...
```

### 4-3. 외부 인프라

- **Polygon Amoy 테스트넷** (chain-id: 80002)
- **Redis** (영수증 재사용 방지용)
  - 로컬 개발: `docker run -d --name tinypay-redis -p 6379:6379 redis:7-alpine`

---

## 5. Redis 키 네임스페이스

다른 팀과 키 충돌을 피하기 위해 prefix를 분리합니다:

| 용도 | Key 패턴 | TTL | 담당 |
|---|---|---|---|
| 영수증 재사용 방지 | `tx:used:{tx_hash}` | 1시간 | **김나형** |
| Rate Limiting | `rate:user:{user_id}` | 1분 | 백엔드 팀 |
| 중복 요청 방지 | `request:{idempotency_key}` | 10분 | 백엔드 팀 |

> 백엔드 팀이 Redis 사용 시 위 prefix 규칙을 지켜주세요.

---

## 6. 작업 진행 상황

### 6-1. 완료된 작업

- ✅ Hardhat 환경 세팅
- ✅ MockUSDC, TinyPayment 스마트 컨트랙트 작성 + Slither 보안 점검
- ✅ Polygon Amoy 테스트넷 배포 + Polygonscan verify
- ✅ Spring Boot 블록체인 모듈 셋업 (Web3j Bean, BlockchainService 인터페이스)
- ✅ Redis 셋업 (Docker, 의존성, 설정, Bean)
- ✅ 영수증 5단계 검증 모듈 구현 (1~5단계 + markAsUsed)
- ✅ BlockchainServiceImpl과 ReceiptVerifier 위임 연결

### 6-2. 진행 예정

- ⏳ 통합 테스트 (실제 testnet 트랜잭션으로 5단계 검증 동작 확인)
- ⏳ TinyPayment.java wrapper 재생성 (현재 손수 작성 스텁 사용 중)
- ⏳ 어뷰징 탐지 모듈 (5월 2주차 일정)
- ⏳ 프롬프트 인젝션 검사 모듈 (5월 2주차 일정)

### 6-3. 알려진 이슈 / TODO

- `RuntimeException` → `BlockchainConnectionException`으로 교체 예정 (RPC 통신 에러 처리)
- 4·5단계가 같은 receipt를 중복 조회 → 통합 시 1회 조회로 리팩토링 예정
- `MockUSDC.java.stub.bak` 백업 파일은 통합 테스트 통과 후 삭제 예정

---

## 7. 백엔드 팀에 전달할 사항

### 7-1. ⚠️ DB 비밀번호 평문 노출

### 7-2. ERD에 누락된 테이블 4개

명세서 8번에 정의된 다음 테이블이 ERD에 반영되지 않았습니다:

- `wallet` — 사용자 지갑 정보
- `charge_history` — 충전 내역
- `tx_verification_log` — 영수증 5단계 검증 결과 로그 (단계별 boolean 컬럼)
- `abuse_log` — 어뷰징 탐지 로그

특히 `tx_verification_log`는 영수증 검증 결과를 단계별로 기록하기 위해 필요합니다.

### 7-3. `payment_log` 테이블 컬럼 보강 필요

ERD의 `payment_log` 테이블에 명세서 기준 누락 컬럼이 있습니다:
- `order_id` (UUID 형태, 중복 결제 방지)
- `payer_wallet`, `receiver_wallet`
- `tx_hash`, `payment_status`, `verification_status`, `verified_at`

### 7-4. web3j 라이브러리 버전 변경

`build.gradle`의 `org.web3j:core` 버전을 **4.10.3 → 4.12.3**으로 업그레이드했습니다.  
자동 생성된 컨트랙트 wrapper 호환을 위함이며, 다른 코드에는 영향 없는 것으로 확인됨.

### 7-5. `BlockchainService` 호출 패턴

`BlockchainServiceImpl`에서 컨트랙트 wrapper 호출 시 `.send()`가 필요합니다 (Web3j 4.x 표준):

```java
// O 자동 생성 wrapper
mockUSDC.balanceOf(addr).send();

// X 직접 작성 스텁 (deprecated)
mockUSDC.balanceOf(addr);
```

추후 메서드 추가 시 동일 패턴 적용 부탁드립니다.

### 7-6. 통합 테스트 미완료 상태

영수증 5단계 검증 모듈은 **빌드 성공 + 코드 작성 완료** 상태이며, 실제 testnet 트랜잭션으로의 통합 테스트는 백엔드 결제 흐름 통합 시점에 진행 예정입니다.

---

## 8. 설계 결정 사항 (의사결정 기록)

### 8-1. 검증 모듈을 별도 패키지로 분리한 이유

`BlockchainServiceImpl` 안에 5단계 검증 로직을 직접 넣지 않고 `verification` 패키지로 분리.

**이유**:
- 단일 책임 원칙: `BlockchainServiceImpl`은 블록체인 통신만 책임
- 테스트 용이성: 검증 로직 단독으로 단위 테스트 가능
- 어뷰징 탐지 모듈에서도 `ReceiptVerifier`를 직접 가져다 쓸 가능성

### 8-2. 외부 API는 boolean + 예외, 내부는 VerificationResult

명세서의 `verifyReceipt()`는 `boolean` 반환 + 실패 시 예외 throw 패턴.  
내부 `ReceiptVerifier`는 `VerificationResult` 객체 반환.

**이유**:
- 외부 인터페이스는 명세서 그대로 유지 (백엔드 팀 변경 없음)
- 내부에서는 단계별 결과/사유를 객체로 보존 → 어뷰징 분석, DB 로그 활용 가능
- `BlockchainServiceImpl.verifyReceipt()`에서 `VerificationResult` → 예외 변환

### 8-3. fail-fast 검증 순서

1단계 실패 시 2~5단계는 실행하지 않음.

**이유**:
- RPC 호출 비용 절약 (Replay 공격이면 온체인 조회 자체가 낭비)
- 실패 단계가 명확해야 디버깅/어뷰징 분석 용이
- 2단계(트랜잭션 실패) 시 3·4·5단계는 의미 없음 (실패한 tx는 이벤트 없음)

### 8-4. markAsUsed는 5단계 모두 통과 후에만

1단계에서 미리 키를 등록하지 않고, 모든 단계 통과 후 등록.

**이유**:
- 중간 단계 실패 시 키가 남아있으면 재검증 불가능 (사용자 불편)
- 5단계까지 통과한 진짜 유효한 영수증만 "사용됨" 표시하는 게 의미상 정확

### 8-5. web3j 라이브러리 4.10.3 → 4.12.3 업그레이드

Web3j CLI 1.8.0이 자동 생성한 wrapper가 최신 라이브러리 클래스(`CustomError`)를 참조해서 4.12.3으로 업그레이드.

**부작용**:
- 4.12.3에도 `CustomError` 클래스가 없어 wrapper에서 해당 부분 수동 제거 필요했음
- 자동 생성 wrapper 메서드 시그니처 변경 (`balanceOf()`가 `RemoteFunctionCall<BigInteger>` 반환) → `BlockchainServiceImpl`에 `.send()` 추가

---

## 9. 참고 자료

- 프로젝트 명세서: `Project_proposal_티니페이.pdf` (4. Coding 섹션)
- ERC-20 Transfer 이벤트: `event Transfer(address indexed from, address indexed to, uint256 value)`
- Web3j 공식 문서: https://docs.web3j.io/
- Polygon Amoy 테스트넷: https://amoy.polygonscan.com/
