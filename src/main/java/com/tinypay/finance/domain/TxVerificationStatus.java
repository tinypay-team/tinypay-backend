package com.tinypay.finance.domain;

public enum TxVerificationStatus {
    PASSED,
    FAILED_REPLAY,
    FAILED_TX,
    FAILED_CONTRACT,
    FAILED_RECEIVER,
    FAILED_AMOUNT
}