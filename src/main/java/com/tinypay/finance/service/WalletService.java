package com.tinypay.finance.service;

import com.tinypay.blockchain.service.BlockchainService;
import com.tinypay.blockchain.service.CreateWalletResult;
import com.tinypay.finance.domain.BudgetPolicy;
import com.tinypay.finance.domain.Wallet;
import com.tinypay.finance.dto.request.CreateWalletRequest;
import com.tinypay.finance.dto.response.GetWalletResponse;
import com.tinypay.finance.repository.BudgetPolicyRepository;
import com.tinypay.finance.repository.WalletRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.user.domain.User;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final BudgetPolicyRepository budgetPolicyRepository;
    private final UserRepository userRepository;
    private final BlockchainService blockchainService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void createWallet(Long userId, CreateWalletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        if (!user.isPhoneVerified()) {
            throw new CustomException(ErrorType.PHONE_NOT_VERIFIED);
        }

        String hashedPin = passwordEncoder.encode(request.getWalletPin());

        CreateWalletResult result = blockchainService.createWallet();

        Wallet wallet = Wallet.builder()
                .user(user)
                .walletAddress(result.getWalletAddress())
                .privateKeyEncrypted(result.getEncryptedPrivateKey())
                .build();

        wallet.initializePin(hashedPin);
        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public GetWalletResponse getWallet(Long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new CustomException(ErrorType.WALLET_NOT_FOUND));

        BudgetPolicy policy = budgetPolicyRepository.findByUser_IdAndDeletedAtIsNull(userId)
                .orElse(null);

        boolean autoPaymentEnabled = policy != null && policy.isAutoPaymentEnabled();

        return GetWalletResponse.builder()
                .walletAddress(wallet.getWalletAddress())
                .balance(wallet.getBalance())
                .walletStatus(wallet.getWalletStatus().name())
                .autoPaymentEnabled(autoPaymentEnabled)
                .build();
    }
}