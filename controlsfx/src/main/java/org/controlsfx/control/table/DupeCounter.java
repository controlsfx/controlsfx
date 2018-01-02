package org.controlsfx.control.table;

import java.util.HashMap;
import java.util.Optional;


final class DupeCounter<T> {

    private final HashMap<T,Integer> counts = new HashMap<>();
    private final boolean enforceFloor;

    public DupeCounter(boolean enforceFloor) {
        this.enforceFloor = enforceFloor;
    }
    public int add(T value) {
        Integer prev = counts.get(value);
        int newVal;
        if (prev == null) {
            newVal = 1;
            counts.put(value, newVal);
        }  else {
            newVal = prev + 1;
            counts.put(value, newVal);
        }
        return newVal;
    }
    public int get(T value) {
        return Optional.ofNullable(counts.get(value)).orElse(0);
    }
    public int remove(T value) {
        Integer prev = counts.get(value);
        if (prev != null && prev > 0) {
            int newVal = prev - 1;
            if (newVal == 0) {
                counts.remove(value);
            } else {
                counts.put(value, newVal);
            }
            return newVal;
        }
        else if (enforceFloor) {
            throw new IllegalStateException();
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return counts.toString();
    }
}
