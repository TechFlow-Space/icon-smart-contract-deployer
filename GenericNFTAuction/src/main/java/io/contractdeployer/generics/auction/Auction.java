package io.contractdeployer.generics.auction;

import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.Map;

import static io.contractdeployer.generics.auction.Constant.ZERO_ADDRESS;
import static io.contractdeployer.generics.auction.Vars.auction;
import static io.contractdeployer.generics.auction.Vars.currentAuctionIndex;
import static score.Context.*;

public class Auction {

    @External(readonly = true)
    public BigInteger getCurrentIndex() {
        return currentAuctionIndex.getOrDefault(BigInteger.ZERO);
    }

    @External(readonly = true)
    public AuctionDB getCurrentAuction() {
        return auction.get(currentAuctionIndex.getOrDefault(BigInteger.ZERO));
    }

    @External(readonly = true)
    public Map<String, Object>[] getAuctions(int offset, int limit, String order) {
        int auctionIndex = currentAuctionIndex.getOrDefault(BigInteger.ZERO).intValue();
        int maxCount = Math.min(offset + limit, auctionIndex);
        Map<String, Object>[] auctions = new Map[maxCount];
        if (order.equals("desc")) {
            for (int i = maxCount - 1, j = 0; i >= offset; i--, j++) {
                auctions[j] = auction.get(BigInteger.valueOf(i)).toObject();
            }
        } else {
            for (int i = offset, j = 0; i < maxCount; i++, j++) {
                auctions[j] = auction.get(BigInteger.valueOf(i)).toObject();
            }
        }
        return auctions;
    }

    @External
    public void createAuction(Address contractAddress, BigInteger id, BigInteger minimumBid, BigInteger auctionEndTime) {
        Address _from = getCaller();
        BigInteger currentIndex = currentAuctionIndex.getOrDefault(BigInteger.ZERO);
        require(minimumBid.compareTo(BigInteger.ZERO) > 0, "Minimum Bid must be greater than 0");
        require(auctionEndTime.compareTo(now()) > 0, "Invalid auction end time");

        if (!currentIndex.equals(BigInteger.ZERO)) {
            AuctionDB currentAuction = auction.get(currentIndex);
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
        require(!currentIndex.equals(BigInteger.ZERO), "No Auction Available");

        AuctionDB auctionDB = auction.get(currentIndex);
        require(!_from.equals(auctionDB.getAuctionCreator()), "Auction Creator Not Allowed To Bid");
        require(now().compareTo(auctionDB.getAuctionEndTime()) < 0, "Auction Ended");
        require(value.compareTo(auctionDB.getMinimumBid()) > 0 &&
                value.compareTo(auctionDB.getHighestBid()) > 0, "Invalid Bid Value");

        auctionDB.setHighestBid(value);
        auctionDB.setHighestBidder(_from);
        auction.set(currentIndex, auctionDB);
        HighestBidIncreased(currentIndex, value);
    }

    @External
    public void endAuction(BigInteger auctionId) {
        AuctionDB auctionDB = auction.get(auctionId);
        require(auctionDB != null, "Invalid Auction Id");
        require(getCaller().equals(auctionDB.getAuctionCreator()), "OnlyAuctionCreator");
        Address contractAddress = auctionDB.getContractAddress();
        Address highestBidder = auctionDB.getHighestBidder();
        BigInteger nftId = auctionDB.getNftId();

        if (auctionDB.getHighestBidder().equals(ZERO_ADDRESS)) {
            transferToBidder(contractAddress, auctionDB.getAuctionCreator(), nftId);
            auctionDB.setNoParticipation(true);
        } else {
            transferToBidder(contractAddress, highestBidder, nftId);
        }

        auctionDB.setTransferred(true);
        auction.set(auctionId, auctionDB);
        AuctionCompleted(auctionId, contractAddress, nftId);
    }

    @External
    public void transferToTreasury(Address treasury, BigInteger value) {
        require(getCaller().equals(getOwner()), "Owner Only");
        require(value.compareTo(getBalance(getAddress())) <= 0, "Invalid Value");
        transfer(treasury, value);
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
    public void HighestBidIncreased(BigInteger index, BigInteger value) {
    }

    @EventLog(indexed = 2)
    public void AuctionCompleted(BigInteger index, Address contractAddress, BigInteger nftId) {
    }


}
