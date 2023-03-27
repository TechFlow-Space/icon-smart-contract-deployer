package io.contractdeployer.generics.marketplace;

import com.iconloop.score.util.EnumerableSet;
import io.contractdeployer.generics.marketplace.db.SaleDB;
import io.contractdeployer.generics.marketplace.util.CustomEnumerableSet;
import score.*;

import java.math.BigInteger;

import static io.contractdeployer.generics.marketplace.Constant.*;
import static score.Context.*;
import static score.Context.newBranchDB;

public class Vars {

    static final VarDB<Address> admin = newVarDB(ADMIN, Address.class);
    static final DictDB<BigInteger, BigInteger> genericPriceDb = newDictDB(PRICE_DB, BigInteger.class);
    //static final ArrayDB<Address> scores = newArrayDB(SCORES, Address.class);
    static final EnumerableSet<Address> scores = new EnumerableSet<>(SCORES, Address.class);
//    static final DictDB<Address, String> scoreTypes = newDictDB(SCORES, String.class);
    static final VarDB<BigInteger> countSale = newVarDB(COUNT_SALE, BigInteger.class);
    static final DictDB<Address, BigInteger> genericMarketplaceCut = newDictDB(MARKETPLACE_CUT, BigInteger.class);
    static final DictDB<Address, Boolean> isBuyingEnabled = newDictDB(IS_BUYING_ENABLED, Boolean.class);
    static final DictDB<Address, Boolean> isSettingPriceEnabled = newDictDB(SETTING_PRICE_ENABLED, Boolean.class);
    static final DictDB<BigInteger, Address> genericSetterAddress = newDictDB(SETTER_ADDRESS, Address.class);
    static final BranchDB<String, ArrayDB<BigInteger>> genericNftSalesHistory = newBranchDB(NFT_SALES_HISTORY, BigInteger.class);
    static final DictDB<BigInteger, SaleDB> sales = newDictDB(SALES, SaleDB.class);
    static final CustomEnumerableSet<Address, BigInteger> scoreAvailableSales = new CustomEnumerableSet<> (SCORE_AVAILABLE_SALES, BigInteger.class);
    static final BranchDB<Address, ArrayDB<BigInteger>> ownersSales = newBranchDB(OWNERS_SALES, BigInteger.class);

    static final BranchDB<String, DictDB<BigInteger, BigInteger>> ownersNftSaleId = newBranchDB(OWNERS_NFT_SALE_ID, BigInteger.class);

    static final VarDB<Integer> counter = newVarDB(COUNTER, Integer.class);

}
