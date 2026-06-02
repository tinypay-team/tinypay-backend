package com.tinypay.blockchain.service;

import com.tinypay.abuse.domain.AbuseActionType;
import com.tinypay.abuse.domain.AbuseLog;
import com.tinypay.abuse.domain.AbuseLogRepository;
import com.tinypay.abuse.domain.AbuseType;
import com.tinypay.blockchain.contracts.MockUSDC;
import com.tinypay.blockchain.contracts.TinyPayment;
import com.tinypay.blockchain.crypto.EncryptionException;
import com.tinypay.blockchain.crypto.KeyEncryptor;
import com.tinypay.blockchain.exception.InsufficientPaymentException;
import com.tinypay.blockchain.exception.InvalidContractException;
import com.tinypay.blockchain.exception.InvalidRecipientException;
import com.tinypay.blockchain.exception.PaymentAuthException;
import com.tinypay.blockchain.exception.ReplayAttackException;
import com.tinypay.blockchain.exception.TransactionFailedException;
import com.tinypay.blockchain.verification.ReceiptVerifier;
import com.tinypay.blockchain.verification.VerificationResult;
import com.tinypay.finance.domain.Wallet;
import com.tinypay.finance.domain.WalletStatus;
import com.tinypay.finance.repository.WalletRepository;
import com.tinypay.global.common.auth.PaymentAuthContext;
import com.tinypay.user.domain.User;
import com.tinypay.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.TransactionManager;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class BlockchainServiceImpl implements BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainServiceImpl.class);

    private final Web3j web3j;
    private final Credentials credentials;
    private final MockUSDC mockUSDC;
    private final TinyPayment tinyPayment;
    private final String tinyPaymentAddress;
    private final ReceiptVerifier receiptVerifier;
    private final KeyEncryptor keyEncryptor;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final AbuseLogRepository abuseLogRepository;

    private static final int USDC_DECIMALS = 6;

    public BlockchainServiceImpl(
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider gasProvider,
            TransactionManager txManager,
            ReceiptVerifier receiptVerifier,
            KeyEncryptor keyEncryptor,
            WalletRepository walletRepository,
            UserRepository userRepository,
            AbuseLogRepository abuseLogRepository,
            @Value("${blockchain.mock-usdc-address}") String mockUsdcAddress,
            @Value("${blockchain.tiny-payment-address}") String tinyPaymentAddress
    ) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.receiptVerifier = receiptVerifier;
        this.keyEncryptor = keyEncryptor;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.abuseLogRepository = abuseLogRepository;
        this.tinyPaymentAddress = tinyPaymentAddress;
        this.mockUSDC = MockUSDC.load(
                mockUsdcAddress, web3j, txManager, gasProvider);
        this.tinyPayment = TinyPayment.load(
                tinyPaymentAddress, web3j, txManager, gasProvider);
    }

    @PostConstruct
    public void approveSpender() {
        try {
            BigInteger maxAllowance = BigInteger.TWO.pow(256).subtract(BigInteger.ONE);
            mockUSDC.approve(tinyPaymentAddress, maxAllowance).send();
            log.info("TinyPayment 컨트랙트 approve 완료: spender={}", tinyPaymentAddress);
        } catch (Exception e) {
            log.error("TinyPayment 컨트랙트 approve 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getBalance(String walletAddress) {
        try {
            BigInteger rawBalance = mockUSDC.balanceOf(walletAddress).send();
            return new BigDecimal(rawBalance)
                    .divide(BigDecimal.TEN.pow(USDC_DECIMALS));
        } catch (Exception e) {
            throw new RuntimeException("잔액 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String mintUsdc(String toWallet, BigInteger amount, PaymentAuthContext authCtx) {
        // ===== 보안 검증 =====

        // 1. authCtx null 체크 (백엔드 버그 방어)
        if (authCtx == null) {
            throw new PaymentAuthException("인증 컨텍스트 누락");
        }

        Long userId = authCtx.getUserId();

        // 2. 비밀번호 검증 통과 여부
        //    (서버가 통과한 결과만 보내야 하지만 이중 방어)
        if (!authCtx.isPasswordVerified()) {
            throw new PaymentAuthException(
                    "결제 비밀번호 검증 실패: userId=" + userId);
        }

        // 3. 본인인증 검증 (DB의 phoneVerified 직접 조회 — 이중 방어)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PaymentAuthException(
                        "사용자 없음: userId=" + userId));

        if (!user.isPhoneVerified()) {
            AbuseLog abuseLog = AbuseLog.builder()
                    .user(user)
                    .abuseType(AbuseType.UNAUTHORIZED_PAYMENT_ATTEMPT.name())
                    .actionTaken(AbuseActionType.BLOCKED)
                    .detail("본인인증 미완료 충전 시도: userId=" + userId)
                    .build();
            abuseLogRepository.save(abuseLog);
            throw new PaymentAuthException("본인인증 미완료: userId=" + userId);
        }

        // 4. Wallet 조회
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new PaymentAuthException(
                        "지갑 없음: userId=" + userId));

        // 5. Wallet 상태 검증 (ACTIVE만 허용)
        if (wallet.getWalletStatus() != WalletStatus.ACTIVE) {
            AbuseLog abuseLog = AbuseLog.builder()
                    .user(wallet.getUser())
                    .wallet(wallet)
                    .abuseType(AbuseType.LOCKED_WALLET_ACCESS.name())
                    .actionTaken(AbuseActionType.BLOCKED)
                    .detail("비정상 지갑 상태로 충전 시도: status=" + wallet.getWalletStatus())
                    .build();
            abuseLogRepository.save(abuseLog);
            throw new PaymentAuthException(
                    "지갑 상태 비정상: " + wallet.getWalletStatus());
        }

        // ===== 검증 통과 → 블록체인 호출 =====
        try {
            TransactionReceipt receipt = mockUSDC.mint(toWallet, amount).send();
            return receipt.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("USDC 충전 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String transferUsdc(String orderId, String fromWallet,
                               String toWallet, BigInteger amount,
                               String serviceType) {
        try {
            TransactionReceipt receipt = tinyPayment.executePayment(
                    orderId, toWallet, amount, serviceType).send();
            return receipt.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("결제 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyReceipt(String txHash, String expectedReceiver,
                                  BigInteger expectedAmount) {
        VerificationResult result = receiptVerifier.verify(txHash, expectedReceiver, expectedAmount);

        if (result.isValid()) {
            return true;
        }

        // 실패 사유별로 명세서 7번에 정의된 예외 throw
        String detail = result.getDetail();
        switch (result.getReason()) {
            case REPLAY_ATTACK:
                throw new ReplayAttackException(detail);
            case TRANSACTION_FAILED:
                throw new TransactionFailedException(detail);
            case INVALID_CONTRACT:
                throw new InvalidContractException(detail);
            case INVALID_RECIPIENT:
                throw new InvalidRecipientException(detail);
            case INSUFFICIENT_PAYMENT:
                throw new InsufficientPaymentException(detail);
            default:
                // SUCCESS는 위에서 이미 처리됐으므로 여기 도달 불가
                throw new IllegalStateException("불가능한 검증 상태: " + result.getReason());
        }
    }

    /**
     * 비밀번호 5회 실패 시 지갑 잠금
     * 백엔드가 Redis 카운트 관리 → 5회 도달 시 이 메서드 호출
     */
    @Override
    @Transactional
    public void lockWalletForBruteForce(Long userId) {
        // 1. userId로 지갑 조회
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new PaymentAuthException(
                        "지갑 없음: userId=" + userId));

        // 2. 지갑 잠금 (dirty checking으로 자동 저장됨)
        wallet.lock();

        // 3. AbuseLog 기록
        AbuseLog abuseLog = AbuseLog.builder()
                .user(wallet.getUser())
                .wallet(wallet)
                .abuseType(AbuseType.PAYMENT_PASSWORD_BRUTE_FORCE.name())
                .actionTaken(AbuseActionType.WALLET_LOCKED)
                .detail("비밀번호 5회 실패로 지갑 잠금: userId=" + userId)
                .build();
        abuseLogRepository.save(abuseLog);
    }

    @Override
    public CreateWalletResult createWallet() {
        try {
            // 1. ECKeyPair 새로 생성 (안전한 난수 기반)
            ECKeyPair keyPair = Keys.createEcKeyPair();

            // 2. 지갑 주소 추출 (0x + 40자리 hex)
            String walletAddress = "0x" + Keys.getAddress(keyPair);

            // 3. private key 추출 → hex 문자열로 변환
            //    BigInteger.toString(16)는 앞쪽 0을 생략하므로 64자리로 패딩
            String privateKeyHex = keyPair.getPrivateKey().toString(16);
            String paddedPrivateKey = String.format("%64s", privateKeyHex).replace(' ', '0');

            // 4. AES-256-GCM 으로 암호화
            String encryptedPrivateKey = keyEncryptor.encrypt(paddedPrivateKey);

            // 5. 결과 반환 (백엔드가 Wallet 엔티티 저장 시 사용)
            return new CreateWalletResult(walletAddress, encryptedPrivateKey);

        } catch (EncryptionException e) {
            // 암호화 실패는 그대로 위로 전파
            throw e;
        } catch (Exception e) {
            // 키페어 생성 실패 등 기타 예외
            throw new RuntimeException("지갑 생성 실패: " + e.getMessage(), e);
        }
    }
}
