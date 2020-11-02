/*
 * Copyright (c) 2018, ControlsFX
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
package impl.org.controlsfx.collections;

import java.util.AbstractList;
import java.util.List;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public final class MappingChange<E, F> extends Change<F> {
    
    private final MappingChange.Map<E, F> map;
    private final Change<? extends E> original;
    private List<F> removed;

    public static final Map NOOP_MAP = new Map() {

        @Override
        public Object map(Object original) {
            return original;
        }
    };

    public interface Map<E, F> {
        F map(E original);
    }

    public MappingChange(Change<? extends E> original, MappingChange.Map<E, F> map, ObservableList<F> list) {
        super(list);
        this.original = original;
        this.map = map;
    }

    @Override
    public boolean next() {
        return original.next();
    }

    @Override
    public void reset() {
        original.reset();
    }

    @Override
    public int getFrom() {
        return original.getFrom();
    }

    @Override
    public int getTo() {
        return original.getTo();
    }

    @Override
    public List<F> getRemoved() {
        if (removed == null) {
            removed = new AbstractList<>() {

                @Override
                public F get(int index) {
                    return map.map(original.getRemoved().get(index));
                }

                @Override
                public int size() {
                    return original.getRemovedSize();
                }
            };
        }
        return removed;
    }

    @Override
    protected int[] getPermutation() {
        return new int[0];
    }

    @Override
    public boolean wasPermutated() {
        return original.wasPermutated();
    }

    @Override
    public boolean wasUpdated() {
        return original.wasUpdated();
    }

    @Override
    public int getPermutation(int i) {
        return original.getPermutation(i);
    }

    @Override
    public String toString() {

        int size = 0, pos = 0, posToEnd = 0;
        
        while (next()) {
            posToEnd++;
        }

        reset();
        while (next()) {
            size++;
        }
        reset();

        StringBuilder b = new StringBuilder();
        b.append("{ ");
        while (next()) {
            if (wasPermutated()) {
                b.append(ChangeHelper.permChangeToString(getPermutation()));
            } else if (wasUpdated()) {
                b.append(ChangeHelper.updateChangeToString(getFrom(), getTo()));
            } else {
                b.append(ChangeHelper.addRemoveChangeToString(getFrom(), getTo(), getList(), getRemoved()));
            }
            if (pos != size) {
                b.append(", ");
            }
        }
        b.append(" }");

        reset();
        pos = size - posToEnd;
        while (pos-- > 0) {
            next();
        }

        return b.toString();
    }

}