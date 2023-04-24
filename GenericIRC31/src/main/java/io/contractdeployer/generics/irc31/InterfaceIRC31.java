package io.contractdeployer.generics.irc31;

import score.Address;

import java.math.BigInteger;

public interface InterfaceIRC31 {

    String name();

    String symbol();

    BigInteger getMaxBatchMintCount();

    BigInteger getTotalSupply();

    BigInteger getCap();

    Address getAdmin();

    void setAdmin(Address _address);

    void mint(Address _to, BigInteger _id, BigInteger _amount,String _uri);

    void mintBatch(Address _owner, BigInteger[] _ids, BigInteger[] _amounts,String[] _uris);

    void burn(Address _owner, BigInteger _id, BigInteger _amount);

    void burnBatch(Address _owner, BigInteger[] _ids, BigInteger[] _amounts);
}

