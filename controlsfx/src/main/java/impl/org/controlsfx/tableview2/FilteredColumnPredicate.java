/**
 * Copyright (c) 2013, 2018 ControlsFX
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
package impl.org.controlsfx.tableview2;

import javafx.scene.control.TableColumnBase;
import org.controlsfx.control.tableview2.FilteredTableColumn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Predicate implementation for {@link org.controlsfx.control.tableview2.FilteredTableView }
 * that is used when the {@link org.controlsfx.control.tableview2.FilteredTableView#filter() } 
 * method is called.
 *
 * @param <S> the type of the input to the predicate
 * @param <T> The type of the content in all cells in the related FilteredTableColumn
 */
public class FilteredColumnPredicate<S, T> implements Predicate<S> {

    private final List<? extends TableColumnBase> columns;

    public FilteredColumnPredicate(TableColumnBase<S,T>... columns) {
        this(Arrays.asList(columns));
    }

    public FilteredColumnPredicate(List<? extends TableColumnBase> columns) {
        this.columns = Collections.unmodifiableList(columns);
    }

    /** {@inheritDoc} */
    @Override public boolean test(S s) {
        for (TableColumnBase<S,T> tc : columns) {
            if (tc instanceof FilteredTableColumn) {
                FilteredTableColumn<S, T> ftc = (FilteredTableColumn) tc;
                if (ftc.getPredicate() == null || ! ftc.isFilterable()) {
                    continue;
                }
                T value = tc.getCellData(s);
                boolean result = doFilter(ftc, value);

                // this is an and operation: bails with the first column that fails
                if (! result) {
                    return false;
                }
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.columns != null ? this.columns.hashCode() : 0);
        return hash;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FilteredColumnPredicate other = (FilteredColumnPredicate) obj;
        if (this.columns != other.columns && (this.columns == null || !this.columns.equals(other.columns))) {
            return false;
        }
        return true;
    }

    private boolean doFilter(final FilteredTableColumn<S, T> filter, final T value) {
        Predicate<? super T> p = filter.getPredicate();
        return p == null || p.test(value);
    }
    
    /** {@inheritDoc} */
    @Override public String toString() {
        return "FilteredColumnsPredicate [ columns: " + columns + "] ";
    }

}
