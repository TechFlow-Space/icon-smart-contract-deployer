package io.contractdeployer.generics.auction;

import io.contractdeployer.generics.auction.exception.AuctionException;
import score.Address;
import score.Context;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;

import static io.contractdeployer.generics.auction.Constant.TAG;
import static io.contractdeployer.generics.auction.Constant.ZERO_ADDRESS;
import static io.contractdeployer.generics.auction.Vars.auction;
import static io.contractdeployer.generics.auction.Vars.currentAuctionIndex;
import static score.Context.*;

public class Auction {

    @External(readonly = true)
    public String name(){
        return TAG;
    }

    @External(readonly = true)
    public BigInteger getCurrentIndex() {
        return currentAuctionIndex.getOrDefault(BigInteger.ZERO);
    }

    @External(readonly = true)
    public AuctionDB getCurrentAuction() {
        return auction.get(currentAuctionIndex.getOrDefault(BigInteger.ZERO));
    }

    @External(readonly = true)
    public AuctionDB[] getAuctions(int offset, int limit, String order) {

        int auctionIndex = currentAuctionIndex.getOrDefault(BigInteger.ZERO).intValue();
        if (offset == 0) {
            offset++;
            limit++;
            auctionIndex++;
        }

        int maxCount = Math.min(offset + limit, auctionIndex);
        AuctionDB[] auctions = new AuctionDB[maxCount - offset];
        if (order.equals("desc")) {
            for (int i = maxCount - 1, j = 0; i >= offset; i--, j++) {
                auctions[j] = auction.get(BigInteger.valueOf(i));
            }
        } else {
            for (int i = offset, j = 0; i < maxCount; i++, j++) {
                auctions[j] = auction.get(BigInteger.valueOf(i));
            }
        }
        return auctions;
    }

    @External
    public void createAuction(Address contractAddress, BigInteger id, BigInteger minimumBid, BigInteger auctionEndTime) {
        Address _from = getCaller();
        BigInteger currentIndex = currentAuctionIndex.getOrDefault(BigInteger.ZERO);
        require(minimumBid.compareTo(BigInteger.ZERO) > 0,
                AuctionException.invalidBid("Minimum Bid must be greater than 0"));
        require(auctionEndTime.compareTo(now()) > 0, AuctionException.invalidEndTime());

        if (!currentIndex.equals(BigInteger.ZERO)) {
            AuctionDB currentAuction = auction.get(currentIndex);
            // use case of end auction time
            require(currentAuction.getTransferred() || currentAuction.getAuctionEndTime().compareTo(now()) > 0, "Auction Ongoing");
        }

        validateApproval(contractAddress, id);
        transferToContract(contractAddress, _from, id);
        validateOwner(contractAddress, getAddress(), id);

        BigInteger newIndex = currentIndex.add(BigInteger.ONE);
        AuctionDB auctionDB = new AuctionDB(newIndex, _from, contractAddress, minimumBid, BigInteger.ZERO, ZERO_ADDRESS, id, auctionEndTime);
        currentAuctionIndex.set(newIndex);
        auction.set(newIndex, auctionDB);
        AuctionCreated(newIndex, _from, contractAddress, id);
    }

    @External
    @Payable
    public void bid() {
        Address _from = getCaller();
        BigInteger value = getValue();
        BigInteger currentIndex = currentAuctionIndex.getOrDefault(BigInteger.ZERO);
        require(!currentIndex.equals(BigInteger.ZERO), AuctionException.unavailable());

        AuctionDB auctionDB = auction.get(currentIndex);
        require(!_from.equals(auctionDB.getAuctionCreator()), AuctionException.creator());
        require(!auctionDB.getTransferred() || !auctionDB.getNoParticipation() || now().compareTo(auctionDB.getAuctionEndTime()) < 0, "Auction Ended");
        require(value.compareTo(auctionDB.getMinimumBid()) > 0 &&
                value.compareTo(auctionDB.getHighestBid()) > 0,
                AuctionException.invalidBid("Bid should be greater than zero/previous bidder"));

        // transfer to previous bidder
        if (!auctionDB.getHighestBidder().equals(ZERO_ADDRESS) && !auctionDB.getHighestBid().equals(BigInteger.ZERO)){
            transferFromScore(auctionDB.getHighestBidder(),auctionDB.getHighestBid());
        }

        auctionDB.setHighestBid(value);
        auctionDB.setHighestBidder(_from);
        auction.set(currentIndex, auctionDB);
        HighestBidIncreased(currentIndex, value);
    }

    @External
    public void endAuction(BigInteger auctionId) {
        AuctionDB auctionDB = auction.get(auctionId);
        require(auctionDB != null, AuctionException.invalidId());
        require(!auctionDB.getTransferred(),AuctionException.auctionEnded(auctionId));
        require(getCaller().equals(auctionDB.getAuctionCreator()), AuctionException.onlyAuctionCreator());
        Address contractAddress = auctionDB.getContractAddress();
        Address highestBidder = auctionDB.getHighestBidder();
        BigInteger nftId = auctionDB.getNftId();

        if (auctionDB.getHighestBidder().equals(ZERO_ADDRESS)) {

            transferToBidder(contractAddress, auctionDB.getAuctionCreator(), nftId);
            auctionDB.setNoParticipation(true);
            AuctionCompleted(auctionId, auctionDB.getAuctionCreator(),contractAddress, nftId);
        } else {
            transferToBidder(contractAddress, highestBidder, nftId);
            AuctionCompleted(auctionId, auctionDB.getHighestBidder(), contractAddress,nftId);
        }

        auctionDB.setTransferred(true);
        auctionDB.setAuctionEndTime(now());
        auction.set(auctionId, auctionDB);
    }

    @External
    public void transferToTreasury(Address treasury) {
        require(getCaller().equals(getOwner()), "Owner Only");
        BigInteger balance = getBalance(getAddress());

        transferFromScore(treasury, balance);

        TransferToTreasury(treasury,balance);
    }

    void transferFromScore(Address treasury,BigInteger balance){
        Context.transfer(treasury,balance);
    }

    void transferToContract(Address contractAddress, Address _from, BigInteger _tokenId) {
        call(contractAddress, "transferFrom", _from, getAddress(), _tokenId);
    }

    void transferToBidder(Address contractAddress, Address bidder, BigInteger _tokenId) {
        call(contractAddress, "transfer", bidder, _tokenId);
    }

    void validateApproval(Address contractAddress, BigInteger nftId) {
        Address approvalTo = (Address) call(contractAddress, "getApproved", nftId);
        require(approvalTo.equals(getAddress()), "ApprovalNotSet");
    }

    void validateOwner(Address contractAddress, Address _owner, BigInteger nftId) {
        Address owner = (Address) call(contractAddress, "ownerOf", nftId);
        require(owner.equals(_owner), "NotTheOwner");
    }

    BigInteger now() {
        return BigInteger.valueOf(getBlockTimestamp());
    }

    @EventLog(indexed = 2)
    public void AuctionCreated(BigInteger index, Address creator, Address contractAddress, BigInteger nftId) {
    }

    @EventLog(indexed = 2)
    public void TransferToTreasury(Address treasury,BigInteger amount) {
    }

    @EventLog(indexed = 2)
    public void HighestBidIncreased(BigInteger index, BigInteger value) {
    }

    @EventLog(indexed = 2)
    public void AuctionCompleted(BigInteger index, Address highestBidder, Address contractAddress, BigInteger nftId) {
    }


}
