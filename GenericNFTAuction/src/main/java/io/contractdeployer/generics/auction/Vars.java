package io.contractdeployer.generics.auction;

import score.Address;
import score.DictDB;
import score.VarDB;

import java.math.BigInteger;

import static io.contractdeployer.generics.auction.Constant.*;
import static score.Context.*;

public class Vars {

    static final VarDB<BigInteger> currentAuctionIndex = newVarDB("CURRENT_AUCTION_INDEX", BigInteger.class);
    static final DictDB<BigInteger, AuctionDB> auction = newDictDB("AUCTION", AuctionDB.class);

}
