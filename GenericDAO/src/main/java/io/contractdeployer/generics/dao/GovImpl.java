package io.contractdeployer.generics.dao;

import io.contractdeployer.generics.dao.exception.GovernanceException;
import score.*;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;
import java.util.Map;

public class GovImpl {

    public static final String TAG = "Governance"; // TODO : is the tag okay?
    public static final BigInteger HOUR_IN_SECONDS = BigInteger.valueOf(3600);
    public static final BigInteger DAY_IN_SECONDS = HOUR_IN_SECONDS.multiply(BigInteger.valueOf(24));
    public static final BigInteger HOUR_IN_MICROSECONDS = HOUR_IN_SECONDS.multiply(BigInteger.valueOf(1_000_000));
    public static final BigInteger DAY_IN_MICROSECONDS = DAY_IN_SECONDS.multiply(BigInteger.valueOf(1_000_000));

    public final VarDB<String> name = Context.newVarDB("name",String.class);
    private final VarDB<Address> tokenAddress = Context.newVarDB("token_address", Address.class);
    private final VarDB<String> tokenType = Context.newVarDB("token_type", String.class);
    private final VarDB<BigInteger> tokenId = Context.newVarDB("token_id", BigInteger.class);
    private final VarDB<BigInteger> minimumThreshold = Context.newVarDB("minimum_threshold", BigInteger.class);

    private final VarDB<BigInteger> voteDuration = Context.newVarDB("vote_duration",BigInteger.class);
    private final VarDB<BigInteger> graceDuration = Context.newVarDB("grace_duration",BigInteger.class);
    private final VarDB<BigInteger> proposalId = Context.newVarDB("proposal_id", BigInteger.class);
    private final DictDB<BigInteger, Proposal> proposals = Context.newDictDB("proposals", Proposal.class);
    // proposalId => holder => token votes
    private final BranchDB<BigInteger, DictDB<Address, TokenVote>> tokenVotes = Context.newBranchDB("token_votes", TokenVote.class);
    private final DictDB<BigInteger, Votes> votes = Context.newDictDB("votes_sum", Votes.class);

    public GovImpl(String _name){
        if (name.get()==null){
            name.set(_name);
        }
    }

    @External(readonly=true)
    public String name() {
        return TAG + name.get();
    }

    @External(readonly=true)
    public Map<String, Object> governanceTokenInfo() {
        var type = tokenType();
        if (type == null) {
            return Map.of();
        }
        if (TokenProxy.IRC2.equals(type)) {
            return Map.of(
                    "_address", tokenAddress(),
                    "_type", type);
        } else {
            return Map.of(
                    "_address", tokenAddress(),
                    "_type", type,
                    "_id", tokenId());
        }
    }


    @External
    public void setGovernanceToken(Address _address, String _type, @Optional BigInteger _id) {
        onlyOwner();
        Context.require(tokenAddress() == null, GovernanceException.tokenSet());
        var type = _type.toLowerCase();
        switch (type) {
            case TokenProxy.IRC2:
            case TokenProxy.IRC31:
                tokenType.set(type);
                break;
            default:
                Context.revert(GovernanceException.invalidToken());
        }
        tokenAddress.set(_address);
        if (TokenProxy.IRC31.equals(type)) {
            tokenId.set(_id);
        }
    }

    @External(readonly=true)
    public BigInteger minimumThreshold() {
        return minimumThreshold.getOrDefault(BigInteger.ZERO);
    }

    @External
    public void setMinimumThreshold(BigInteger _amount) {
        onlyOwner();
        Context.require(_amount.signum() > 0, GovernanceException.greaterThanZero());
        minimumThreshold.set(_amount);
    }

    @External(readonly=true)
    public BigInteger lastProposalId() {
        return proposalId.getOrDefault(BigInteger.ZERO);
    }

    @External
    public void setVoteDuration(BigInteger _duration){
        onlyOwner();
        voteDuration.set(_duration);
    }

    @External
    public void setGraceDuration(BigInteger _duration){
        onlyOwner();
        Context.require(checkValidTimeStamp(_duration),GovernanceException.invalidTime());
        graceDuration.set(_duration);
    }

    @External(readonly = true)
    public BigInteger getVoteDuration(){
        return voteDuration.getOrDefault(DAY_IN_MICROSECONDS);
    }

    @External(readonly = true)
    public BigInteger getGraceDuration(){
        return graceDuration.getOrDefault(HOUR_IN_MICROSECONDS.multiply(BigInteger.valueOf(3)));
    }

    @External
    public void submitProposal(BigInteger _endTime, String _ipfsHash) {
        Address sender = Context.getCaller();
        Context.require(!sender.isContract(), GovernanceException.onlyEOA());
        checkEndTimeOrThrow(_endTime);

        Context.require(minimumThreshold().compareTo(getTokenBalance(sender)) <= 0,
                GovernanceException.thresholdNotMet(minimumThreshold()));

        BigInteger pid = getNextId();
        long createTime = Context.getBlockTimestamp();
        long endTime = _endTime.longValue();
        Proposal pl = new Proposal(sender, createTime, endTime, _ipfsHash, Proposal.STATUS_ACTIVE);
        proposals.set(pid, pl);
        ProposalSubmitted(pid, sender);
    }

    BigInteger getTokenBalance(Address sender) {
        var tokenProxy = new TokenProxy(tokenAddress(), tokenType(), tokenId());
        return  tokenProxy.balanceOf(sender);
    }

    @External
    public void vote(BigInteger _proposalId, String _vote) {
        Address sender = Context.getCaller();
        Context.require(!sender.isContract(), GovernanceException.onlyEOA());

        Proposal pl = proposals.get(_proposalId);
        Context.require(pl != null, GovernanceException.invalidId(_proposalId));
        Context.require(pl.getStatus() == Proposal.STATUS_ACTIVE, GovernanceException.notActive());

        BigInteger balance = getTokenBalance(sender);
        Context.require(balance.signum() > 0, GovernanceException.notTokenHolder());

        var vote = _vote.toLowerCase();
        Context.require(Votes.isValid(vote), GovernanceException.invalidVote("Invalid vote type"));

        Context.require(tokenVotes.at(_proposalId).get(sender) == null,
                GovernanceException.invalidVote("Caller has already voted"));
        tokenVotes.at(_proposalId).set(sender, new TokenVote(vote, balance));
        var vs = votes.get(_proposalId);
        if (vs == null) {
            vs = new Votes();
        }
        vs.increase(vote, balance);
        votes.set(_proposalId, vs);
    }

    @External
    public void cancelProposal(BigInteger _proposalId) {
        Address sender = Context.getCaller();
        Proposal pl = proposals.get(_proposalId);
        Context.require(pl != null, GovernanceException.invalidId(_proposalId));
        Context.require(pl.getCreator().equals(sender), GovernanceException.onlyCreator());
        Context.require(pl.getStatus() == Proposal.STATUS_ACTIVE, GovernanceException.notActive());

        long now = Context.getBlockTimestamp();
        long graceTime = getGraceDuration().longValue();
        Context.require(pl.getStartTime() + graceTime > now,
                GovernanceException.unknown("Grace duration has passed"));

        pl.setStatus(Proposal.STATUS_CANCELED);
        proposals.set(_proposalId, pl);
        ProposalCanceled(_proposalId);
    }

    @External
    public void closeProposal(BigInteger _proposalId) {
        Proposal pl = proposals.get(_proposalId);
        Context.require(pl != null, GovernanceException.invalidId(_proposalId));
        Context.require(pl.getStatus() == Proposal.STATUS_ACTIVE, GovernanceException.notActive());

        long now = Context.getBlockTimestamp();
        Context.require(pl.getEndTime() <= now,
                GovernanceException.unknown("End time of proposal has not reached"));

        pl.setStatus(Proposal.STATUS_CLOSED);
        proposals.set(_proposalId, pl);
        ProposalClosed(_proposalId);
    }

    @External(readonly=true)
    public Map<String, Object> getProposal(BigInteger _proposalId) {
        Proposal pl = proposals.get(_proposalId);
        Context.require(pl != null, GovernanceException.invalidId(_proposalId));

        var vs = votes.get(_proposalId);
        if (vs == null) {
            vs = new Votes();
        }
        return Map.ofEntries(
                Map.entry("_proposalId", _proposalId),
                Map.entry("_creator", pl.getCreator()),
                Map.entry("_status", Proposal.STATUS_MSG[pl.getStatus()]),
                Map.entry("_endTime", pl.getEndTime()),
                Map.entry("_startTime", pl.getStartTime()),
                Map.entry("_ipfsHash", pl.getIpfsHash()),
                Map.entry("_forVoices", vs.getFor()),
                Map.entry("_againstVoices", vs.getAgainst()),
                Map.entry("_abstainVoices", vs.getAbstain())
        );
    }

    @External(readonly=true)
    public Map<String, Object> getVote(Address _voter, BigInteger _proposalId) {
        var tokenVote = tokenVotes.at(_proposalId).get(_voter);
        if (tokenVote != null) {
            return Map.of(
                    "_vote", tokenVote.getVote(),
                    "_power", tokenVote.getAmount()
            );
        }
        return Map.of();
    }

    @EventLog(indexed=2)
    public void ProposalSubmitted(BigInteger _proposalId, Address _creator) {}

    @EventLog(indexed=1)
    public void ProposalCanceled(BigInteger _proposalId) {}

    @EventLog(indexed=1)
    public void ProposalClosed(BigInteger _proposalId) {}

    private void checkCallerOrThrow(Address caller, String errMsg) {
        Context.require(Context.getCaller().equals(caller), errMsg);
    }

    private void onlyOwner() {
        checkCallerOrThrow(Context.getOwner(), GovernanceException.onlyOwner());
    }

    private Address tokenAddress() {
        return tokenAddress.get();
    }

    private String tokenType() {
        return tokenType.get();
    }

    private BigInteger tokenId() {
        return tokenId.getOrDefault(BigInteger.ZERO);
    }

    private void checkEndTimeOrThrow(BigInteger _endTime) {
        BigInteger minimumEnd = getVoteDuration();
        Context.require(checkValidTimeStamp(_endTime),
                GovernanceException.invalidTime());
        Context.require(_endTime.compareTo(now().add(minimumEnd))>0,
                GovernanceException.invalidEndTime());
    }

    private boolean checkValidTimeStamp(BigInteger timestamp) {
        return timestamp.toString().length() == 16;
    }

    private BigInteger now(){
        return BigInteger.valueOf(Context.getBlockTimestamp());
    }

    private BigInteger getNextId() {
        BigInteger _id = lastProposalId();
        _id = _id.add(BigInteger.ONE);
        proposalId.set(_id);
        return _id;
    }

}
