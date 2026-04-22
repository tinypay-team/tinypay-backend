package com.tinypay.tinypay.blockchain.contracts;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class TinyPayment extends Contract {

    public TinyPayment(String contractAddress, Web3j web3j,
                       Credentials credentials, ContractGasProvider gasProvider) {
        super("", contractAddress, web3j, credentials, gasProvider);
    }

    public static TinyPayment load(String contractAddress, Web3j web3j,
                                   Credentials credentials, ContractGasProvider gasProvider) {
        return new TinyPayment(contractAddress, web3j, credentials, gasProvider);
    }

    // 결제 실행
    public TransactionReceipt executePayment(String orderId, String receiver,
                                             BigInteger amount, String serviceType) throws Exception {
        Function function = new Function("executePayment",
                Arrays.asList(
                        new Utf8String(orderId),
                        new Address(receiver),
                        new Uint256(amount),
                        new Utf8String(serviceType)),
                Collections.emptyList());
        return executeTransaction(function);
    }
}
