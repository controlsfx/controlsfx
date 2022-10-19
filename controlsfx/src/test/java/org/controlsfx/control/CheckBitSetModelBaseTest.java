/**
 * Copyright (c) 2022, ControlsFX
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

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CheckBitSetModelBaseTest {

    private CheckBitSetModelBase<String> model;
    private ObservableList<String> items;
    private Map<String, BooleanProperty> itemBooleanMap;

    private static final String ROW_1_VALUE = "Row 1";
    private static final String ROW_2_VALUE = "Row 2";
    private static final String ROW_3_VALUE = "Row 3";
    private static final String ROW_4_VALUE = "Row 4";
    private static final String ROW_5_VALUE = "Row 5";

    @Before
    public void setUp() {
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_2_VALUE, ROW_3_VALUE, ROW_4_VALUE, ROW_5_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);
    }

    @Test public void testCheckNullObject() {
        model.check(null);
        assertTrue(model.isEmpty());
    }

    @Test
    public void testSingleCheck() {
        assertFalse(model.isChecked(3));
        model.check(3);
    }

    @Test public void testMultipleChecks() {
        model.clearChecks();
        assertTrue(model.isEmpty());

        model.check(3);
        model.check(4);
        assertTrue(model.isChecked(3));
        assertTrue(model.isChecked(4));
        assertFalse(model.isChecked(2));
    }

    @Test
    public void clearCheckWithSingleCheck() {
        assertFalse(model.isChecked(3));
        model.check(3);
        assertTrue(model.isChecked(3));
        model.clearCheck(3);
        assertFalse(model.isChecked(3));
    }

    @Test public void ensureIsEmptyIsAccurate() {
        assertTrue(model.isEmpty());
        model.check(3);
        assertFalse(model.isEmpty());
        model.clearChecks();
        assertTrue(model.isEmpty());
    }

    @Test public void testSingleCheckCallsListenerOnce() {
        AtomicInteger count = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            count.getAndIncrement();
        });

        assertEquals(0, count.get());

        model.check(1);
        assertEquals(1, count.get());
        assertTrue(model.isChecked(1));
    }

    @Test
    public void testMultipleCheckCallsListenerOnce() {
        AtomicInteger count = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            count.getAndIncrement();
        });

        assertEquals(0, count.get());

        model.checkIndices(1, 2, 3);
        assertEquals(1, count.get());
        assertTrue(model.isChecked(1));
        assertTrue(model.isChecked(2));
        assertTrue(model.isChecked(3));
    }

    @Test
    public void testClearChecksCallsListenerOnce() {
        model.checkIndices(1, 2, 3);
        AtomicInteger count = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            count.getAndIncrement();
        });
        model.clearChecks();
        assertEquals(1, count.get());
    }
}
