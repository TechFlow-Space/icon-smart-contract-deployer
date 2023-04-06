package io.contractdeployer.generics.airdrop;

import score.Address;

import java.math.BigInteger;
import java.util.List;

public interface InterfaceAirdrop {

    String name();

    void airdropIRC2(Address _tokenAddress, Address[] _recipients, BigInteger[] _amounts);

    void airdropIRC2(Address _tokenAddress);

    List<AirdropDetails> getDistributionOf();

    void addReceipients(Address[] _recipients, BigInteger[] _amounts, BigInteger[] _timestamp);

    void airdropIRC3(Address _tokenAddress, Address[] _from, Address[] _recipients, BigInteger[] _tokenId);

    void airdropIRC31(Address _tokenAddress, Address[] _from, Address[] _recipients,
                      BigInteger[] _tokenId, BigInteger[] _amount);

    void AirdropToken(Address from, String key, Address to, BigInteger amount);
}
