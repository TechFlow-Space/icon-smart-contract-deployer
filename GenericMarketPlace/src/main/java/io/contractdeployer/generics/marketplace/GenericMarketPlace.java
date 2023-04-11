package io.contractdeployer.generics.marketplace;

import io.contractdeployer.generics.marketplace.db.SaleDB;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;

import static io.contractdeployer.generics.marketplace.Constant.*;
import static io.contractdeployer.generics.marketplace.Vars.*;
import static io.contractdeployer.generics.marketplace.enums.SaleStatus.*;
import static io.contractdeployer.generics.marketplace.util.NumUtil.pow;
import static io.contractdeployer.generics.marketplace.util.StringUtil.bytesToHex;
import static java.lang.Math.min;
import static score.Context.require;

public class GenericMarketPlace {

    public GenericMarketPlace() {
        if (admin.get()==null){
            admin.set(Context.getCaller());
            countSale.set(BigInteger.ZERO);
            counter.set(0);
        }
    }

    @External(readonly = true)
    public int getCounter(){
        return counter.get();
    }

    @External
    public void setAdmin(Address _admin) {
        ownerRequired();
        admin.set(_admin);
        TransferAdminRight(Context.getCaller(),_admin);
    }

    @External(readonly = true)
    public Address getAdmin() {
        return admin.get();
    }

    @External
    public void setMarketplaceFee(Address scoreAddress, BigInteger fee) {
        adminRequired();
        supportedScoreRequired(scoreAddress);
        require(BigInteger.ZERO.compareTo(fee) <0 &&
                fee.compareTo(BigInteger.valueOf(100).multiply(pow(BigInteger.TEN, 18))) <= 0,
                Message.Not.feeInRange());
        genericMarketplaceCut.set(scoreAddress, fee);
    }

    @External(readonly = true)
    public BigInteger getMarketplaceFee(Address scoreAddress) {
        return genericMarketplaceCut.get(scoreAddress);
    }

    @External
    public void addScore(Address score){
        adminRequired();
        validateScore(score);
        require(!scores.contains(score), Message.Found.score());
        scores.add(score);
    }

    @External(readonly = true)
    public List<Address> getScores() {
        List<Address> scoreList = new ArrayList<>();
        int length = scores.length();
        for(int i=0; i<length; i++) {
            scoreList.add(scores.at(i));
        }
        return scoreList;
    }

    @External
    public void toggleSellingEnabled(Address score) {
        adminRequired();
        supportedScoreRequired(score);
        isSettingPriceEnabled.set(score, !isSellingEnabled(score));
    }

    @External(readonly = true)
    public boolean isSellingEnabled(Address score) {
        supportedScoreRequired(score);
        return isSettingPriceEnabled.getOrDefault(score, false);
    }

    @External
    public void toggleBuyingEnabled(Address score) {
        adminRequired();
        supportedScoreRequired(score);
        isBuyingEnabled.set(score, !isBuyingEnabled(score));
    }

    @External(readonly = true)
    public boolean isBuyingEnabled(Address score) {
        supportedScoreRequired(score);
        return isBuyingEnabled.getOrDefault(score, false);
    }

    Address getNftOwner(BigInteger saleId) {
        SaleDB saleDB = sales.get(saleId);
        require(saleDB != null, Message.Not.found(Sale));
        return saleDB.getOwner();
    }

    Address getNftOwner(Address score, BigInteger nftId) {
        supportedScoreRequired(score);
        return (Address) Context.call(score, ownerOf, nftId);
    }

    BigInteger getBalanceOfOwner(Address owner, Address score, BigInteger nftId) {
        supportedScoreRequired(score);
        return (BigInteger) Context.call(score, balanceOf, owner, nftId);
    }

    boolean operatorIsApprovedForAll(Address score, Address owner, Address operator) {
        supportedScoreRequired(score);
        return (Boolean) Context.call(score, isApprovedForAll, owner, operator);
    }

    @External
    public void setPrice(Address score, BigInteger rate, BigInteger nftId, @Optional int count) {
        supportedScoreRequired(score);
        Address owner = Context.getCaller();
        validateSale(score, owner, rate);
        count = count==0?1:count;
        BigInteger ownersBalance = getBalanceOfOwner(owner, score, nftId);
        require(ownersBalance.intValue()>=count, Message.Not.enough());
        String ownerScore = getOwnerPrefix(owner, score);
        BigInteger saleId = ownersNftSaleId.at(ownerScore).get(nftId);
        SaleDB saleDB;
        if(saleId==null) {
            saleId = countSale.get().add(BigInteger.ONE);
            setSetterAddress(saleId, owner);
            saleDB = new SaleDB(saleId, nftId, score, owner, rate, count, FOR_SALE.name(), BigInteger.valueOf(Context.getBlockTimestamp()), getTransactionHash(), null, null);
            sales.set(saleId, saleDB);
            countSale.set(saleId);
            genericPriceDb.set(saleId, rate);
            genericNftSalesHistory.at(getNftPrefix(nftId, score)).add(saleId);
            scoreAvailableSales.add(saleDB.getScore(), saleId);
            ownersSales.at(owner).add(saleId);
            ownersNftSaleId.at(ownerScore).set(nftId, saleId);
        }else{
            saleDB = sales.get(saleId);
            require(saleDB.getStatus().equals(FOR_SALE.name()), Message.Not.active());
            saleDB.setPrice(rate);
            saleDB.setCount(count);
        }
        sales.set(saleId, saleDB);
        genericPriceDb.set(saleId, rate);
        PutForSale(saleId, owner, rate);
    }

    public String getOwnerPrefix(Address owner, Address score){
        return owner+"_"+score;
    }

    public String getNftPrefix(BigInteger nftId, Address score){
        return nftId+"_"+score;
    }


    @External(readonly = true)
    public BigInteger getRate(BigInteger saleId) {
        SaleDB saleDB = sales.get(saleId);
        require(saleDB!=null, Message.Not.found(Sale));
        require(saleDB.getStatus().equals(FOR_SALE.name()), Message.Not.forSale());

        Address nftOwner = getNftOwner(saleId);
        require(getSellerAddress(saleId).equals(nftOwner), Message.Not.forSale());

        return genericPriceDb.get(saleId);
    }

    void  validateSale(Address score, Address owner, BigInteger rate){
        require(rate.compareTo(BigInteger.ZERO) > 0, Message.greaterThanZero(Price));
        require(isSellingEnabled(score), Message.Not.enabled(Selling));
        require(operatorIsApprovedForAll(score, owner, Context.getAddress()), Message.Not.approved());
    }

    @External
    public void changeRate(BigInteger saleId, BigInteger rate){
        Address owner = Context.getCaller();
        SaleDB saleDB = sales.get(saleId);
        require(saleDB!=null, Message.Not.found(Sale));
        require(saleDB.getOwner().equals(owner), Message.Not.nftOwner());
        require(saleDB.getStatus().equals(FOR_SALE.name()), Message.Not.forSale());
        validateSale(saleDB.getScore(), owner, rate);
        saleDB.setPrice(rate);
        sales.set(saleId, saleDB);
        genericPriceDb.set(saleId, rate);
    }

    public void setSetterAddress(BigInteger saleId, Address address) {
        genericSetterAddress.set(saleId, address);
    }

    @External(readonly = true)
    public Address getSellerAddress(BigInteger saleId) {
        SaleDB saleDB = sales.get(saleId);
        require(saleDB!=null, Message.Not.found(Sale));
        return genericSetterAddress.getOrDefault(saleId, null);
    }

    @External(readonly = true)
    public List<SaleDB>  getScoreAvailableSales(Address score, int limit, int offset, String order) {
        require(limit <= 10, Message.maxTenAllowed());
        ArrayDB<BigInteger> availableSalesOfScore = scoreAvailableSales.at(score);
        require(offset <= availableSalesOfScore.size(), Message.Not.historyAvailable(offset));

        int maxCount = min(offset + limit, availableSalesOfScore.size());
        List<SaleDB> dataCollection = new ArrayList<>();

        if (order.equals(desc)) {
            for (int i = maxCount - 1; i >= offset; i--) {
                dataCollection.add(sales.get(availableSalesOfScore.get(i)));
            }
        } else {
            for (int i = offset; i < maxCount; i++) {
                dataCollection.add(sales.get(availableSalesOfScore.get(i)));
            }
        }
        return dataCollection;
    }

    @External(readonly = true)
    public int countScoreAvailableSales(Address score){
        return scoreAvailableSales.at(score).size();
    }

    @External(readonly = true)
    public List<SaleDB> getPutOnSaleHistory(Address score, BigInteger nftId, int limit, int offset, String order) {
        require(limit <= 10, Message.maxTenAllowed());

        ArrayDB<BigInteger> salesHistory = genericNftSalesHistory.at(getNftPrefix(nftId, score));
        List<SaleDB> saleList = new ArrayList<>();
        if(offset >= salesHistory.size()){
            return saleList;
        }

        //can't use getDataCollection method here as data is not directly taken from dict
        int maxCount = min(offset + limit, salesHistory.size());
        if (order.equals(desc)) {
            for (int i = maxCount - 1; i >= offset; i--) {
                saleList.add(sales.get(salesHistory.get(i)));
            }
        } else {
            for (int i = offset; i < maxCount; i++) {
                saleList.add(sales.get(salesHistory.get(i)));
            }
        }
        return saleList;
    }

    @External(readonly = true)
    public int countSaleHistory(Address score, BigInteger nftId){
       return genericNftSalesHistory.at(getNftPrefix(nftId, score)).size();
    }

    @External(readonly = true)
    public BigInteger countSale(){
        return countSale.get();
    }

    @External(readonly = true)
    public List<SaleDB> getOwnerSales(Address owner, int limit, int offset, String order) {
        ArrayDB<BigInteger> ownersSaleIds = ownersSales.at(owner);
        List<SaleDB> saleList = new ArrayList<>();
        if(offset >= ownersSaleIds.size()){
            return saleList;
        }

        //can't use getDataCollection method here as data is not directly taken from dict
        int maxCount = min(offset + limit, ownersSaleIds.size());
        if (order.equals(desc)) {
            for (int i = maxCount - 1; i >= offset; i--) {
                saleList.add(sales.get(ownersSaleIds.get(i)));
            }
        } else {
            for (int i = offset; i < maxCount; i++) {
                saleList.add(sales.get(ownersSaleIds.get(i)));
            }
        }
        return saleList;
    }

    @External(readonly = true)
    public SaleDB getSale(BigInteger saleId) {
        return sales.get(saleId);
    }

    @External(readonly = true)
    public SaleDB getSaleByNftId(Address score, BigInteger nftId) {
        ArrayDB<BigInteger> availableSales = scoreAvailableSales.at(score);
        int size = availableSales.size();
        for(int i = 0; i<size; i++){
            SaleDB saleDB = sales.get(availableSales.get(i));
            if(saleDB.getNftId().equals(nftId)){
                return saleDB;
            }
        }
        return null;
    }


    @External
    public void removeFromSale(BigInteger saleId) {
        SaleDB saleDB = sales.get(saleId);
        require(saleDB != null, Message.Not.found(Sale));
        require(Context.getCaller().equals(saleDB.getOwner()), Message.Not.nftOwner());
        saleDB.setStatus(CANCELED.name());
        sales.set(saleId, saleDB);
        genericPriceDb.set(saleId, null);
        scoreAvailableSales.remove(saleDB.getScore(), saleId);
        ownersNftSaleId.at(getOwnerPrefix(saleDB.getOwner(), saleDB.getScore())).set(saleDB.getNftId(), null);
        SaleRemoved(saleId);
    }

    @External
    public void transferToTreasury(Address address, BigInteger amt) {
        //Transfer funds to treasury
        adminRequired();
        BigInteger balance = Context.getBalance(Context.getAddress());
        require(balance.compareTo(amt) >= 0, Message.Not.enoughBalance());
        Context.transfer(address, amt);
    }

    @Payable
    @External
    public void buy(BigInteger saleId) {
        SaleDB saleDB = sales.get(saleId);
        require(saleDB != null, Message.Not.found(Sale));
        require(isBuyingEnabled(saleDB.getScore()), Message.Not.enabled(Buying));
        validateBalance(saleDB);
        BigInteger price = saleDB.getPrice().multiply(BigInteger.valueOf(saleDB.getCount()));
        BigInteger paidAmount = getPaidAmount();
        require(paidAmount.equals(price), Message.priceMisMatch(price, paidAmount));

        Address owner = getNftOwner(saleId);
        Address buyer = Context.getCaller();
        require(!owner.equals(buyer), Message.Found.own());
        require(owner.equals(getSellerAddress(saleId)), Message.Not.currentOwner());
        require(operatorIsApprovedForAll(saleDB.getScore(), saleDB.getOwner(), Context.getAddress()), Message.Not.approved());

        BigInteger marketCut = genericMarketplaceCut.get(saleDB.getScore()).multiply(paidAmount).divide(BigInteger.valueOf(100)).divide(pow(BigInteger.TEN, 18)); // (100 * 10 ** 18)
        BigInteger sellersCut = paidAmount.subtract(marketCut);
        saleDB.setStatus(SOLD.name());
        saleDB.setSettleTimestamp(BigInteger.valueOf(Context.getBlockTimestamp()));
        saleDB.setNewOwner(buyer);
        saleDB.setHash(getTransactionHash());
        sales.set(saleId, saleDB);
        genericPriceDb.set(saleId, null);
        ownersNftSaleId.at(getOwnerPrefix(saleDB.getOwner(), saleDB.getScore())).set(saleDB.getNftId(), null);
        scoreAvailableSales.remove(saleDB.getScore(), saleId);
        NFTSold(saleId, owner, buyer, price);

        byte[] data = new byte[0];
        payToSeller(owner, sellersCut);
        transferOwnershipFrom(owner, buyer, saleDB, data);
    }

    void validateBalance(SaleDB saleDB){
        BigInteger balance = getBalanceOfOwner(saleDB.getOwner(), saleDB.getScore(), saleDB.getNftId());
        require(balance.compareTo(BigInteger.valueOf(saleDB.getCount()))>=0, Message.Not.enoughBalance());
    }

    BigInteger getPaidAmount() {
        return Context.getValue();
    }

    void payToSeller(Address seller, BigInteger sellersCut) {
        Context.transfer(seller, sellersCut);
    }

    void transferOwnershipFrom(Address owner, Address to, SaleDB saleDB, byte[] data) {
        Context.call(saleDB.getScore(), transferFrom, owner, to, saleDB.getNftId(), saleDB.getCount(), data);
    }

    private void validateScore(Address score){
       require(score.isContract(), Message.Invalid.score());
    }

    private void supportedScoreRequired(Address score){
        require(scores.contains(score), Message.Not.supportedScore(score));
    }

    private void ownerRequired() {
        require(Context.getCaller().equals(Context.getOwner()), Message.Not.owner());
    }

    void adminRequired() {
        Address _admin = admin.get();
        require(Context.getCaller().equals(_admin), Message.Not.admin(_admin));
    }

    String getTransactionHash() {
        if(Context.getTransactionHash()==null){
            return "NoHash";
        }
        return bytesToHex(Context.getTransactionHash());
    }

    @EventLog(indexed = 3)
    public void PutForSale(BigInteger saleId, Address owner_Address, BigInteger amount) {
    }

    @EventLog(indexed = 3)
    public void NFTSold(BigInteger nftId, Address from_, Address to_, BigInteger amount) {}

    @EventLog(indexed = 1)
    public void SaleRemoved(BigInteger saleId) {}

    @EventLog(indexed = 2)
    public void TransferAdminRight(Address _oldAdmin,Address _newAdmin){}

}
