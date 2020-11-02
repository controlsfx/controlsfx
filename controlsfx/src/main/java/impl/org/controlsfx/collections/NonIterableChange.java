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

import java.util.Collections;
import java.util.List;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public abstract class NonIterableChange<E> extends Change<E> {

    private final int from;
    private final int to;
    private boolean invalid = true;

    protected NonIterableChange(int from, int to, ObservableList<E> list) {
        super(list);
        this.from = from;
        this.to = to;
    }

    @Override
    public int getFrom() {
        checkState();
        return from;
    }

    @Override
    public int getTo() {
        checkState();
        return to;
    }

    @Override
    protected int[] getPermutation() {
        checkState();
        return new int[0];
    }

    @Override
    public boolean next() {
        if (invalid) {
            invalid = false;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        invalid = true;
    }

    public void checkState() {
        if (invalid) {
            throw new IllegalStateException("Invalid change state: Make sure to call next() before inspecting the change.");
        }
    }

    @Override
    public String toString() {
        boolean tempInvalid = invalid;
        invalid = false;
        String string;
        if (wasPermutated()) {
            string = ChangeHelper.permChangeToString(getPermutation());
        } else if (wasUpdated()) {
            string = ChangeHelper.updateChangeToString(from, to);
        } else {
            string = ChangeHelper.addRemoveChangeToString(from, to, getList(), getRemoved());
        }
        invalid = tempInvalid;
        return "{ " + string + " }";
    }

     public static class GenericAddRemoveChange<E> extends NonIterableChange<E> {

        private final List<E> removed;

        public GenericAddRemoveChange(int from, int to, List<E> removed, ObservableList<E> list) {
            super(from, to, list);
            this.removed = removed;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return removed;
        }

    }
     
    public static class SimpleRemovedChange<E> extends NonIterableChange<E> {

        private final List<E> removed;
        
        public SimpleRemovedChange(int from, int to, E removed, ObservableList<E> list) {
            super(from, to, list);
            this.removed = Collections.singletonList(removed);
        }

        @Override
        public boolean wasRemoved() {
            checkState();
            return true;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return removed;
        }

    }

    public static class SimpleAddChange<E> extends NonIterableChange<E> {

        public SimpleAddChange(int from, int to, ObservableList<E> list) {
            super(from, to, list);
        }

        @Override
        public boolean wasRemoved() {
            checkState();
            return false;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return Collections.emptyList();
        }

    }
}

