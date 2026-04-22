package com.tinypay.tinypay.blockchain.contracts;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class MockUSDC extends Contract {

    public MockUSDC(String contractAddress, Web3j web3j,
                    Credentials credentials, ContractGasProvider gasProvider) {
        super("", contractAddress, web3j, credentials, gasProvider);
    }

    public static MockUSDC load(String contractAddress, Web3j web3j,
                                Credentials credentials, ContractGasProvider gasProvider) {
        return new MockUSDC(contractAddress, web3j, credentials, gasProvider);
    }

    // 잔액 조회
    public BigInteger balanceOf(String owner) throws Exception {
        Function function = new Function("balanceOf",
                Arrays.asList(new Address(owner)),
                Arrays.asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturn(function, BigInteger.class);
    }

    // 충전 (owner만 가능)
    public TransactionReceipt mint(String to, BigInteger amount) throws Exception {
        Function function = new Function("mint",
                Arrays.asList(new Address(to), new Uint256(amount)),
                Collections.emptyList());
        return executeTransaction(function);
    }

    // 승인 (transferFrom 전에 필요)
    public TransactionReceipt approve(String spender, BigInteger amount) throws Exception {
        Function function = new Function("approve",
                Arrays.asList(new Address(spender), new Uint256(amount)),
                Collections.emptyList());
        return executeTransaction(function);
    }
}
