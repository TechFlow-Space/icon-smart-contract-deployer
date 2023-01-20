package io.tokenfactory.score.utils;

import score.ArrayDB;

import java.util.List;

public class ArrayDBUtils {
    public static <T> Boolean arrayDbContains(ArrayDB<T> arrayDB, T item) {
        final int size = arrayDB.size();
        for (int i = 0; i < size; i++) {
            if (arrayDB.get(i).equals(item)) {
                return true;
            }
        }
        return false;
    }

    public static <T> Boolean removeFromArraydb(T _item, ArrayDB<T> _array) {
        final int size = _array.size();
        if (size < 1) {
            return false;
        }
        T top = _array.get(size - 1);
        for (int i = 0; i < size; i++) {
            if (_array.get(i).equals(_item)) {
                _array.set(i, top);
                _array.pop();
                return true;
            }
        }

        return false;
    }

    public static <T> List<T> arrayDbToList(ArrayDB<T> db) {
        int dbSize = db.size();
        @SuppressWarnings("unchecked")
        T[] addressList = (T[]) new Object[dbSize];
        for (int i = 0; i < dbSize; i++) {
            addressList[i] = db.get(i);
        }
        return List.of(addressList);
    }
}
