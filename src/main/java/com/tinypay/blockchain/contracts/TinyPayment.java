package com.tinypay.blockchain.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.8.0.
 */
@SuppressWarnings("rawtypes")
@Generated("org.web3j.codegen.SolidityFunctionWrapperGenerator")
public class TinyPayment extends Contract {
    public static final String BINARY = "0x60c060405234801561001057600080fd5b506040516117063803806117068339818101604052810190610032919061015d565b60016100506100456100c560201b60201c565b6100f060201b60201c565b600001819055508073ffffffffffffffffffffffffffffffffffffffff1660808173ffffffffffffffffffffffffffffffffffffffff16815250503373ffffffffffffffffffffffffffffffffffffffff1660a08173ffffffffffffffffffffffffffffffffffffffff16815250505061018a565b60007f9b779b17422d0df92223018b32b4d1fa46e071723d6817e2486d003becc55f0060001b905090565b6000819050919050565b600080fd5b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b600061012a826100ff565b9050919050565b61013a8161011f565b811461014557600080fd5b50565b60008151905061015781610131565b92915050565b600060208284031215610173576101726100fa565b5b600061018184828501610148565b91505092915050565b60805160a0516115506101b660003960006104d801526000818161011a015261036201526115506000f3fe608060405234801561001057600080fd5b50600436106100575760003560e01c80633e413bee1461005c5780633eab87531461007a5780638da5cb5b14610096578063c69207a3146100b4578063f9468713146100e4575b600080fd5b610064610118565b60405161007191906109ac565b60405180910390f35b610094600480360381019061008f9190610ab4565b61013c565b005b61009e6104d6565b6040516100ab9190610b6a565b60405180910390f35b6100ce60048036038101906100c99190610b85565b6104fa565b6040516100db9190610cf6565b60405180910390f35b6100fe60048036038101906100f99190610e48565b6106ec565b60405161010f959493929190610eea565b60405180910390f35b7f000000000000000000000000000000000000000000000000000000000000000081565b610144610800565b6000808787604051610157929190610f74565b908152602001604051809103902060030154146101a9576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101a090610fd9565b60405180910390fd5b600083116101ec576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101e390611045565b60405180910390fd5b6040518060a001604052803373ffffffffffffffffffffffffffffffffffffffff1681526020018573ffffffffffffffffffffffffffffffffffffffff16815260200184815260200142815260200183838080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505081525060008787604051610295929190610f74565b908152602001604051809103902060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506040820151816002015560608201518160030155608082015181600401908161035a9190611267565b5090505060007f000000000000000000000000000000000000000000000000000000000000000073ffffffffffffffffffffffffffffffffffffffff166323b872dd3387876040518463ffffffff1660e01b81526004016103bd93929190611339565b6020604051808303816000875af11580156103dc573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061040091906113a8565b905080610442576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161043990611421565b60405180910390fd5b8473ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff168888604051610480929190610f74565b60405180910390207f8252025948384b31bf7dcece83bf5bfb4acb844ee36d5576ffa09fb33e119986878787426040516104bd949392919061146e565b60405180910390a4506104ce610823565b505050505050565b7f000000000000000000000000000000000000000000000000000000000000000081565b6105026108d2565b6000808484604051610515929190610f74565b90815260200160405180910390206003015403610567576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161055e906114fa565b60405180910390fd5b60008383604051610579929190610f74565b90815260200160405180910390206040518060a00160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600282015481526020016003820154815260200160048201805461066290611094565b80601f016020809104026020016040519081016040528092919081815260200182805461068e90611094565b80156106db5780601f106106b0576101008083540402835291602001916106db565b820191906000526020600020905b8154815290600101906020018083116106be57829003601f168201915b505050505081525050905092915050565b6000818051602081018201805184825260208301602085012081835280955050505050506000915090508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169080600201549080600301549080600401805461077d90611094565b80601f01602080910402602001604051908101604052809291908181526020018280546107a990611094565b80156107f65780601f106107cb576101008083540402835291602001916107f6565b820191906000526020600020905b8154815290600101906020018083116107d957829003601f168201915b5050505050905085565b61080861083e565b600261081a61081561087f565b6108aa565b60000181905550565b600161083561083061087f565b6108aa565b60000181905550565b6108466108b4565b1561087d576040517f3ee5aeb500000000000000000000000000000000000000000000000000000000815260040160405180910390fd5b565b60007f9b779b17422d0df92223018b32b4d1fa46e071723d6817e2486d003becc55f0060001b905090565b6000819050919050565b600060026108c86108c361087f565b6108aa565b6000015414905090565b6040518060a00160405280600073ffffffffffffffffffffffffffffffffffffffff168152602001600073ffffffffffffffffffffffffffffffffffffffff1681526020016000815260200160008152602001606081525090565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b600061097261096d6109688461092d565b61094d565b61092d565b9050919050565b600061098482610957565b9050919050565b600061099682610979565b9050919050565b6109a68161098b565b82525050565b60006020820190506109c1600083018461099d565b92915050565b6000604051905090565b600080fd5b600080fd5b600080fd5b600080fd5b600080fd5b60008083601f840112610a00576109ff6109db565b5b8235905067ffffffffffffffff811115610a1d57610a1c6109e0565b5b602083019150836001820283011115610a3957610a386109e5565b5b9250929050565b6000610a4b8261092d565b9050919050565b610a5b81610a40565b8114610a6657600080fd5b50565b600081359050610a7881610a52565b92915050565b6000819050919050565b610a9181610a7e565b8114610a9c57600080fd5b50565b600081359050610aae81610a88565b92915050565b60008060008060008060808789031215610ad157610ad06109d1565b5b600087013567ffffffffffffffff811115610aef57610aee6109d6565b5b610afb89828a016109ea565b96509650506020610b0e89828a01610a69565b9450506040610b1f89828a01610a9f565b935050606087013567ffffffffffffffff811115610b4057610b3f6109d6565b5b610b4c89828a016109ea565b92509250509295509295509295565b610b6481610a40565b82525050565b6000602082019050610b7f6000830184610b5b565b92915050565b60008060208385031215610b9c57610b9b6109d1565b5b600083013567ffffffffffffffff811115610bba57610bb96109d6565b5b610bc6858286016109ea565b92509250509250929050565b610bdb81610a40565b82525050565b610bea81610a7e565b82525050565b600081519050919050565b600082825260208201905092915050565b60005b83811015610c2a578082015181840152602081019050610c0f565b60008484015250505050565b6000601f19601f8301169050919050565b6000610c5282610bf0565b610c5c8185610bfb565b9350610c6c818560208601610c0c565b610c7581610c36565b840191505092915050565b600060a083016000830151610c986000860182610bd2565b506020830151610cab6020860182610bd2565b506040830151610cbe6040860182610be1565b506060830151610cd16060860182610be1565b5060808301518482036080860152610ce98282610c47565b9150508091505092915050565b60006020820190508181036000830152610d108184610c80565b905092915050565b600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b610d5582610c36565b810181811067ffffffffffffffff82111715610d7457610d73610d1d565b5b80604052505050565b6000610d876109c7565b9050610d938282610d4c565b919050565b600067ffffffffffffffff821115610db357610db2610d1d565b5b610dbc82610c36565b9050602081019050919050565b82818337600083830152505050565b6000610deb610de684610d98565b610d7d565b905082815260208101848484011115610e0757610e06610d18565b5b610e12848285610dc9565b509392505050565b600082601f830112610e2f57610e2e6109db565b5b8135610e3f848260208601610dd8565b91505092915050565b600060208284031215610e5e57610e5d6109d1565b5b600082013567ffffffffffffffff811115610e7c57610e7b6109d6565b5b610e8884828501610e1a565b91505092915050565b610e9a81610a7e565b82525050565b600082825260208201905092915050565b6000610ebc82610bf0565b610ec68185610ea0565b9350610ed6818560208601610c0c565b610edf81610c36565b840191505092915050565b600060a082019050610eff6000830188610b5b565b610f0c6020830187610b5b565b610f196040830186610e91565b610f266060830185610e91565b8181036080830152610f388184610eb1565b90509695505050505050565b600081905092915050565b6000610f5b8385610f44565b9350610f68838584610dc9565b82840190509392505050565b6000610f81828486610f4f565b91508190509392505050565b7f4f7264657220616c726561647920706169640000000000000000000000000000600082015250565b6000610fc3601283610ea0565b9150610fce82610f8d565b602082019050919050565b60006020820190508181036000830152610ff281610fb6565b9050919050565b7f416d6f756e74206d7573742062652067726561746572207468616e2030000000600082015250565b600061102f601d83610ea0565b915061103a82610ff9565b602082019050919050565b6000602082019050818103600083015261105e81611022565b9050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b600060028204905060018216806110ac57607f821691505b6020821081036110bf576110be611065565b5b50919050565b60008190508160005260206000209050919050565b60006020601f8301049050919050565b600082821b905092915050565b6000600883026111277fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff826110ea565b61113186836110ea565b95508019841693508086168417925050509392505050565b600061116461115f61115a84610a7e565b61094d565b610a7e565b9050919050565b6000819050919050565b61117e83611149565b61119261118a8261116b565b8484546110f7565b825550505050565b600090565b6111a761119a565b6111b2818484611175565b505050565b5b818110156111d6576111cb60008261119f565b6001810190506111b8565b5050565b601f82111561121b576111ec816110c5565b6111f5846110da565b81016020851015611204578190505b611218611210856110da565b8301826111b7565b50505b505050565b600082821c905092915050565b600061123e60001984600802611220565b1980831691505092915050565b6000611257838361122d565b9150826002028217905092915050565b61127082610bf0565b67ffffffffffffffff81111561128957611288610d1d565b5b6112938254611094565b61129e8282856111da565b600060209050601f8311600181146112d157600084156112bf578287015190505b6112c9858261124b565b865550611331565b601f1984166112df866110c5565b60005b82811015611307578489015182556001820191506020850194506020810190506112e2565b868310156113245784890151611320601f89168261122d565b8355505b6001600288020188555050505b505050505050565b600060608201905061134e6000830186610b5b565b61135b6020830185610b5b565b6113686040830184610e91565b949350505050565b60008115159050919050565b61138581611370565b811461139057600080fd5b50565b6000815190506113a28161137c565b92915050565b6000602082840312156113be576113bd6109d1565b5b60006113cc84828501611393565b91505092915050565b7f55534443207472616e73666572206661696c6564000000000000000000000000600082015250565b600061140b601483610ea0565b9150611416826113d5565b602082019050919050565b6000602082019050818103600083015261143a816113fe565b9050919050565b600061144d8385610ea0565b935061145a838584610dc9565b61146383610c36565b840190509392505050565b60006060820190506114836000830187610e91565b8181036020830152611496818587611441565b90506114a56040830184610e91565b95945050505050565b7f5061796d656e74206e6f7420666f756e64000000000000000000000000000000600082015250565b60006114e4601183610ea0565b91506114ef826114ae565b602082019050919050565b60006020820190508181036000830152611513816114d7565b905091905056fea2646970667358221220cafc85ba3ea75c72226d0d6104b9b364b911250b57bd6f1a5495a05bd605a03964736f6c634300081c0033\n";

    private static String librariesLinkedBinary;

    public static final String FUNC_EXECUTEPAYMENT = "executePayment";

    public static final String FUNC_GETPAYMENT = "getPayment";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PAYMENTS = "payments";

    public static final String FUNC_USDC = "usdc";


    public static final Event PAYMENTEXECUTED_EVENT = new Event("PaymentExecuted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected TinyPayment(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TinyPayment(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TinyPayment(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TinyPayment(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<PaymentExecutedEventResponse> getPaymentExecutedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PAYMENTEXECUTED_EVENT, transactionReceipt);
        ArrayList<PaymentExecutedEventResponse> responses = new ArrayList<PaymentExecutedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PaymentExecutedEventResponse typedResponse = new PaymentExecutedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.orderId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.payer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.receiver = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.serviceType = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PaymentExecutedEventResponse getPaymentExecutedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PAYMENTEXECUTED_EVENT, log);
        PaymentExecutedEventResponse typedResponse = new PaymentExecutedEventResponse();
        typedResponse.log = log;
        typedResponse.orderId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.payer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.receiver = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.serviceType = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<PaymentExecutedEventResponse> paymentExecutedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPaymentExecutedEventFromLog(log));
    }

    public Flowable<PaymentExecutedEventResponse> paymentExecutedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PAYMENTEXECUTED_EVENT));
        return paymentExecutedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> executePayment(String orderId, String receiver,
            BigInteger amount, String serviceType) {
        final Function function = new Function(
                FUNC_EXECUTEPAYMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(orderId), 
                new org.web3j.abi.datatypes.Address(160, receiver), 
                new org.web3j.abi.datatypes.generated.Uint256(amount), 
                new org.web3j.abi.datatypes.Utf8String(serviceType)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Payment> getPayment(String orderId) {
        final Function function = new Function(FUNC_GETPAYMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(orderId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Payment>() {}));
        return executeRemoteCallSingleValueReturn(function, Payment.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple5<String, String, BigInteger, BigInteger, String>> payments(
            String param0) {
        final Function function = new Function(FUNC_PAYMENTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteFunctionCall<Tuple5<String, String, BigInteger, BigInteger, String>>(function,
                new Callable<Tuple5<String, String, BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple5<String, String, BigInteger, BigInteger, String> call() throws
                            Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<String, String, BigInteger, BigInteger, String>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (String) results.get(4).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> usdc() {
        final Function function = new Function(FUNC_USDC, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    @Deprecated
    public static TinyPayment load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new TinyPayment(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TinyPayment load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TinyPayment(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TinyPayment load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new TinyPayment(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TinyPayment load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new TinyPayment(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<TinyPayment> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider, String _usdcAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _usdcAddress)));
        return deployRemoteCall(TinyPayment.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    public static RemoteCall<TinyPayment> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider, String _usdcAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _usdcAddress)));
        return deployRemoteCall(TinyPayment.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<TinyPayment> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, String _usdcAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _usdcAddress)));
        return deployRemoteCall(TinyPayment.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<TinyPayment> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit, String _usdcAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _usdcAddress)));
        return deployRemoteCall(TinyPayment.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class Payment extends DynamicStruct {
        public String payer;

        public String receiver;

        public BigInteger amount;

        public BigInteger timestamp;

        public String serviceType;

        public Payment(String payer, String receiver, BigInteger amount, BigInteger timestamp,
                String serviceType) {
            super(new org.web3j.abi.datatypes.Address(160, payer), 
                    new org.web3j.abi.datatypes.Address(160, receiver), 
                    new org.web3j.abi.datatypes.generated.Uint256(amount), 
                    new org.web3j.abi.datatypes.generated.Uint256(timestamp), 
                    new org.web3j.abi.datatypes.Utf8String(serviceType));
            this.payer = payer;
            this.receiver = receiver;
            this.amount = amount;
            this.timestamp = timestamp;
            this.serviceType = serviceType;
        }

        public Payment(Address payer, Address receiver, Uint256 amount, Uint256 timestamp,
                Utf8String serviceType) {
            super(payer, receiver, amount, timestamp, serviceType);
            this.payer = payer.getValue();
            this.receiver = receiver.getValue();
            this.amount = amount.getValue();
            this.timestamp = timestamp.getValue();
            this.serviceType = serviceType.getValue();
        }
    }

    public static class PaymentExecutedEventResponse extends BaseEventResponse {
        public byte[] orderId;

        public String payer;

        public String receiver;

        public BigInteger amount;

        public String serviceType;

        public BigInteger timestamp;
    }
}
