package io.tokenfactory.score;

import score.Address;

import java.util.List;

import static io.tokenfactory.score.enums.ContractName.*;

public class Constant {

    public static final String TAG = "TokenFactory";
    public static final List<String> availableContracts = List.of(MARKETPLACE.name(), IRC2.name(), IRC3.name(), IRC31.name());
    public static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    public static final Address CHAIN_ADDRESS = Address.fromString("cx0000000000000000000000000000000000000000");

    // ========================
    // DB Variable
    // ========================
    public static final String ADMIN = "admin";
    public static final String TREASURY = "treasury";
    public static final String DEPLOY_FEE = "deploy_fee";
    public static final String SCORE_CONTENT = "score_content";
    public static final String DEPLOYMENT_DETAIL = "deployment_detail";
    public static final String CONTRACT_DEPLOYER = "contract_deployer";
}
