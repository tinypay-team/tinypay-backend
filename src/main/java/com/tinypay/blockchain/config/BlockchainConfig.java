package com.tinypay.blockchain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

@Configuration
public class BlockchainConfig {

    @Value("${blockchain.rpc-url}")
    private String rpcUrl;

    @Value("${blockchain.server-wallet.private-key}")
    private String privateKey;

    @Value("${blockchain.gas.price}")
    private BigInteger gasPrice;

    @Value("${blockchain.gas.limit}")
    private BigInteger gasLimit;

    @Value("${blockchain.chain-id}")
    private long chainId;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

    @Bean
    public ContractGasProvider gasProvider() {
        return new StaticGasProvider(gasPrice, gasLimit);
    }
    
    @Bean
    public TransactionManager web3jTransactionManager(Web3j web3j, Credentials credentials) {
        return new RawTransactionManager(web3j, credentials, chainId);
    }
}
