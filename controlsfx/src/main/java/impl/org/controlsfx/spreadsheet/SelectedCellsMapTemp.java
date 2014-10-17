/**
 * Copyright (c) 2014 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.spreadsheet;

import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TablePositionBase;

/**
 * This class is copied from com.sun.javafx.scene.control.SelectedCellsMap
 * temporary in 8u20 to resolve https://javafx-jira.kenai.com/browse/RT-38306
 * 
 * Will be removed in 8u40
 *
 * @param <T>
 */
public class SelectedCellsMapTemp<T extends TablePositionBase> {
    private final ObservableList<T> selectedCells;
    private final ObservableList<T> sortedSelectedCells;

    private final Map<Integer, BitSet> selectedCellBitSetMap;

    public SelectedCellsMapTemp(final ListChangeListener<T> listener) {
        selectedCells = FXCollections.<T>observableArrayList();
        sortedSelectedCells = new SortedList<>(selectedCells, (T o1, T o2) -> {
            int result =  o1.getRow() - o2.getRow();
           return result == 0 ? (o1.getColumn() - o2.getColumn())  : result;
        });
        sortedSelectedCells.addListener(listener);

        selectedCellBitSetMap = new TreeMap<>((o1, o2) -> o1.compareTo(o2));
    }

    public int size() {
        return selectedCells.size();
    }

    public T get(int i) {
        if (i < 0) {
            return null;
        }
        return sortedSelectedCells.get(i);
    }

    public void add(T tp) {
        final int row = tp.getRow();
        final int columnIndex = tp.getColumn();

        // update the bitset map
        BitSet bitset;
        if (! selectedCellBitSetMap.containsKey(row)) {
            bitset = new BitSet();
            selectedCellBitSetMap.put(row, bitset);
        } else {
            bitset = selectedCellBitSetMap.get(row);
        }

        if (columnIndex >= 0) {
            boolean isAlreadySet = bitset.get(columnIndex);
            bitset.set(columnIndex);

            if (! isAlreadySet) {
                // add into the list
                selectedCells.add(tp);
            }
        } else {
            // FIXME slow path (for now)
            if (! selectedCells.contains(tp)) {
                selectedCells.add(tp);
            }
        }
    }

    public void addAll(Collection<T> cells) {
        // update bitset
        for (T tp : cells) {
            final int row = tp.getRow();
            final int columnIndex = tp.getColumn();

            // update the bitset map
            BitSet bitset;
            if (! selectedCellBitSetMap.containsKey(row)) {
                bitset = new BitSet();
                selectedCellBitSetMap.put(row, bitset);
            } else {
                bitset = selectedCellBitSetMap.get(row);
            }

            if (columnIndex < 0) {
                continue;
            }

            bitset.set(columnIndex);
        }

        // add into the list
        selectedCells.addAll(cells);
    }

    public void setAll(Collection<T> cells) {
        // update bitset
        selectedCellBitSetMap.clear();
        for (T tp : cells) {
            final int row = tp.getRow();
            final int columnIndex = tp.getColumn();

            // update the bitset map
            BitSet bitset;
            if (! selectedCellBitSetMap.containsKey(row)) {
                bitset = new BitSet();
                selectedCellBitSetMap.put(row, bitset);
            } else {
                bitset = selectedCellBitSetMap.get(row);
            }

            if (columnIndex < 0) {
                continue;
            }

            bitset.set(columnIndex);
        }

        // add into the list
        selectedCells.setAll(cells);
    }

    public void remove(T tp) {
        final int row = tp.getRow();
        final int columnIndex = tp.getColumn();

        // update the bitset map
        if (selectedCellBitSetMap.containsKey(row)) {
            BitSet bitset = selectedCellBitSetMap.get(row);

            if (columnIndex >= 0) {
                bitset.clear(columnIndex);
            }

            if (bitset.isEmpty()) {
                selectedCellBitSetMap.remove(row);
            }
        }

        // update list
        selectedCells.remove(tp);
    }

    public void clear() {
        // update bitset
        selectedCellBitSetMap.clear();

        // update list
        selectedCells.clear();
    }

    public boolean isSelected(int row, int columnIndex) {
        if (columnIndex < 0) {
            return selectedCellBitSetMap.containsKey(row);
        } else {
            return selectedCellBitSetMap.containsKey(row) ? selectedCellBitSetMap.get(row).get(columnIndex) : false;
        }
    }

    public int indexOf(T tp) {
        return sortedSelectedCells.indexOf(tp);
    }

    public boolean isEmpty() {
        return selectedCells.isEmpty();
    }

    public ObservableList<T> getSelectedCells() {
        return selectedCells;
    }
}
