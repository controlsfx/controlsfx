/**
 * Copyright (c) 2015, ControlsFX
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

package impl.org.controlsfx.table;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Maintains a distinct ObservableList of mapped values derived from another ObservableList */
    final class ObservableDistinctList<P,V> extends ObservableListBase<V> {

        private final ObservableList<P> parentList;
        private final Function<P,V> valueExtractor;
        private final List<V> values;

        public ObservableDistinctList(ObservableList<P> parentList, Function<P,V> valueExtractor) {
            this.parentList = parentList;
            this.valueExtractor = valueExtractor;
            this.values = parentList.stream().map(p -> valueExtractor.apply(p)).distinct().collect(Collectors.toList());
            System.out.println(values);

            this.parentList.addListener((ListChangeListener.Change<? extends P> c) -> {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        final Stream<V> candidatesForRemoval = c.getRemoved().stream().map(p -> valueExtractor.apply(p));
                        final List<V> persistingValues = parentList.stream().map(p -> valueExtractor.apply(p)).distinct().collect(Collectors.toList());

                        final Stream<V> valuesToRemove = candidatesForRemoval.filter(v -> ! persistingValues.contains(v));

                        valuesToRemove.peek(v -> System.out.println("REMOVING " + v)).forEach(v -> values.remove(v));
                    }

                    if (c.wasAdded()) {
                        final Stream<V> candidatesForAdd = c.getAddedSubList().stream().map(p -> valueExtractor.apply(p));
                        final List<V> existingValues = parentList.stream().map(p -> valueExtractor.apply(p)).distinct().collect(Collectors.toList());

                        final Stream<V> valuesToAdd = candidatesForAdd.filter(v -> ! values.contains(v));

                        valuesToAdd.peek(v -> System.out.println("ADDING " + v)).forEach(v -> values.add(v));
                    }
                }
            });
        }
        @Override
        public V get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }
    }
