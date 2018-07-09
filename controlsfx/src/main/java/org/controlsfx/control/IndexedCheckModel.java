/**
 * Copyright (c) 2014, 2018 ControlsFX
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
package org.controlsfx.control;

import javafx.collections.ObservableList;

public interface IndexedCheckModel<T> extends CheckModel<T> {

    /**
     * Returns the item in the given index in the control.
     * @param index Index for the item in the control.
     */
    public T getItem(int index);

    /**
     * Returns the index of the given item.
     * @param item Item whose index needs to be fetched.
     */
    public int getItemIndex(T item);

    /**
     * Returns a read-only list of the currently checked indices in the control.
     */
    public ObservableList<Integer> getCheckedIndices();

    /**
     * Checks the given indices in the control
     * @param indices Indices of item to uncheck.
     */
    public void checkIndices(int... indices);

    /**
     * Unchecks the given index in the control
     *  @param index Index of the item to uncheck.
     */
    public void clearCheck(int index);

    /**
     * Returns true if the given index represents an item that is checked in the control.
     *  @param index Index of the item to be tested.
     */
    public boolean isChecked(int index);

    /**
     * Checks the item in the given index in the control.
     * @param index Index of the item to check.
     */
    public void check(int index);

    /**
     * Toggles the check state of the item in the given index of the control.
     * @param index Index of the item whose check state needs to be toggled.
     */
    public void toggleCheckState(int index);

}