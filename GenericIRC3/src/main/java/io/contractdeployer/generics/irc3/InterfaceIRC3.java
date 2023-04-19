package io.contractdeployer.generics.irc3;

import score.Address;

import java.math.BigInteger;

public interface InterfaceIRC3 {
    BigInteger getMintCost();

    BigInteger getTokenId();

    String getTokenUri(BigInteger _tokenId);

    BigInteger getCap();

    Address getAdmin();

    void setAdmin(Address _address);

    void setMintCost(BigInteger _mintCost);

    void mint(String _uri);

    void burn(BigInteger _tokenId);

    void TransferAdmin(Address _oldAmin, Address _newAdmin);

    void Burn(Address _from, BigInteger _tokenId);

    void Mint(Address _to, BigInteger _tokenId);
}
