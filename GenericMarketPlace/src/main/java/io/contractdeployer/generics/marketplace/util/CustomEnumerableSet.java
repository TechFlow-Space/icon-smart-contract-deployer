package io.contractdeployer.generics.marketplace.util;

import io.contractdeployer.generics.marketplace.MarketPlaceException;
import score.*;

public class CustomEnumerableSet<A, V> {
    private final BranchDB<A, ArrayDB<V>> baseEntries;
    private final BranchDB<A, DictDB<V, Integer>> baseIndexes;

    public CustomEnumerableSet(String id, Class<V> valueClass) {
        this.baseEntries = Context.newBranchDB(id + "_base_entries", valueClass);
        this.baseIndexes = Context.newBranchDB(id + "_base_indexes", Integer.class);
    }

    public int length(A base) {
        return baseEntries.at(base).size();
    }

    public V at(A base, int index) {
        return baseEntries.at(base).get(index);
    }

    public ArrayDB<V> at(A base) {
        return baseEntries.at(base);
    }

    public boolean contains(A base, V value) {
        return baseIndexes.at(base).get(value) != null;
    }

    public Integer indexOf(A base, V value) {
        // returns null if value doesn't exist
        Integer result = baseIndexes.at(base).get(value);
        if (result != null) {
            return result - 1;
        }
        return null;
    }

    public void add(A base, V value) {
        if (!contains(base, value)) {
            // add new value
            baseEntries.at(base).add(value);
            baseIndexes.at(base).set(value, baseEntries.at(base).size());
        }
    }

    public void remove(A base, V value) {
        var valueIndex = baseIndexes.at(base).get(value);
        if (valueIndex != null) {
            // pop and swap with the last entry
            int lastIndex = baseEntries.at(base).size();
            V lastValue = baseEntries.at(base).pop();
            baseIndexes.at(base).set(value, null);
            if (lastIndex != valueIndex) {
                baseEntries.at(base).set(valueIndex - 1, lastValue);
                baseIndexes.at(base).set(lastValue, valueIndex);
            }
        }else{
            throw new UserRevertedException(MarketPlaceException.Not.forSale());
        }
    }
}
