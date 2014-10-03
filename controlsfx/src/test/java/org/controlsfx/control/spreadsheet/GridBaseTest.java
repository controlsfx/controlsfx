/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.control.spreadsheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import org.controlsfx.control.spreadsheet.SpreadsheetView.SpanType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author samir.hadzic
 */
public class GridBaseTest {

    private GridBase grid;

    public GridBaseTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        new JFXPanel();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        grid = new GridBase(15, 15);
        List<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();

        for (int row = 0; row < grid.getRowCount(); ++row) {
            ObservableList<SpreadsheetCell> currentRow = FXCollections.observableArrayList();
            for (int column = 0; column < grid.getColumnCount(); ++column) {
                currentRow.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1, ""));
            }
            rows.add(currentRow);
        }
        grid.setRows(rows);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setCellValue method, of class GridBase.
     */
    @Test
    public void testSetCellValue() {
        System.out.println("setCellValue");

        String value = "The cake is a lie";
        grid.setCellValue(0, 0, value);
        assertEquals(value, grid.getRows().get(0).get(0).getItem());
    }

    /**
     * Test of getRowCount method, of class GridBase.
     */
    @Test
    public void testGetRowCount() {
        System.out.println("getRowCount");
        assertEquals(15, grid.getRowCount());
        
        ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
        grid.getRows().add(list);

        assertEquals(16, grid.getRowCount());
    }

    /**
     * Test of getColumnCount method, of class GridBase.
     */
    @Test
    public void testGetColumnCount() {
        System.out.println("getColumnCount");

        assertEquals(15, grid.getRowCount());
    }

    /**
     * Test of getSpanType method, of class GridBase.
     */
    @Test
    public void testGetSpanType() {
        System.out.println("getSpanType");
        SpreadsheetView spv = new SpreadsheetView(grid);
        SpreadsheetView.SpanType type = SpanType.NORMAL_CELL;

        assertEquals(type, grid.getSpanType(spv, -1, -1));
        assertEquals(type, grid.getSpanType(spv, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(type, grid.getSpanType(spv, Integer.MAX_VALUE, -1));
        assertEquals(type, grid.getSpanType(spv, -1, Integer.MAX_VALUE));
        assertEquals(type, grid.getSpanType(spv, grid.getRowCount(), grid.getColumnCount()));
        
        grid.spanColumn(5, 0, 0);
        assertEquals(SpanType.NORMAL_CELL, grid.getSpanType(spv, 0, 0));
        assertEquals(SpanType.COLUMN_SPAN_INVISIBLE, grid.getSpanType(spv, 0, 1));
        assertEquals(SpanType.COLUMN_SPAN_INVISIBLE, grid.getSpanType(spv, 0, 2));
        assertEquals(SpanType.COLUMN_SPAN_INVISIBLE, grid.getSpanType(spv, 0, 3));
        assertEquals(SpanType.COLUMN_SPAN_INVISIBLE, grid.getSpanType(spv, 0, 4));
        
        grid.spanRow(5, 0, 0);
        assertEquals(SpanType.ROW_VISIBLE, grid.getSpanType(spv, 0, 0));
        assertEquals(SpanType.ROW_SPAN_INVISIBLE, grid.getSpanType(spv, 1, 0));
        assertEquals(SpanType.ROW_SPAN_INVISIBLE, grid.getSpanType(spv, 2, 0));
        assertEquals(SpanType.ROW_SPAN_INVISIBLE, grid.getSpanType(spv, 3, 0));
        assertEquals(SpanType.ROW_SPAN_INVISIBLE, grid.getSpanType(spv, 4, 0));
        
        assertEquals(SpanType.BOTH_INVISIBLE, grid.getSpanType(spv, 1, 1));
        assertEquals(SpanType.BOTH_INVISIBLE, grid.getSpanType(spv, 3, 4));
        assertEquals(SpanType.BOTH_INVISIBLE, grid.getSpanType(spv, 2, 1));
    }

    /**
     * Test of getRowHeight method, of class GridBase.
     */
    @Test
    public void testGetRowHeight() {
        System.out.println("getRowHeight");
        Map<Integer, Double> rowHeight = new HashMap<>();
        rowHeight.put(1, 100.0);
        rowHeight.put(5, 12.0);

        grid.setRowHeightCallback(new GridBase.MapBasedRowHeightFactory(rowHeight));

        double result = grid.getRowHeight(1);
        assertEquals(100.0, result, 0.0);

        result = grid.getRowHeight(5);
        assertEquals(12.0, result, 0.0);
    }

    /**
     * Test of setLocked method, of class GridBase.
     */
    @Test
    public void testSetLocked() {
        System.out.println("setLocked");

        assertFalse(grid.isLocked());

        grid.setLocked(true);
        assertTrue(grid.isLocked());

        String value = "The cake is a lie";
        grid.setCellValue(0, 0, value);
        assertEquals("", grid.getRows().get(0).get(0).getItem());
    }

    /**
     * Test of spanRow method, of class GridBase.
     */
    @Test
    public void testSpanRow() {
        System.out.println("spanRow");

        grid.spanRow(0, 0, 0);
        assertEquals(1, grid.getRows().get(0).get(0).getRowSpan());

        grid.spanRow(-1, -1, -1);
        grid.spanRow(0, -1, 0);
        grid.spanRow(0, 0, -1);
        grid.spanRow(-1, 0, 0);
        grid.spanRow(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        grid.spanRow(2, grid.getRowCount(), grid.getColumnCount());
        
        grid.spanRow(1, 0, 0);
        assertEquals(1, grid.getRows().get(0).get(0).getRowSpan());

        grid.spanRow(2, 0, 0);
        assertEquals(2, grid.getRows().get(0).get(0).getRowSpan());
        assertEquals(2, grid.getRows().get(1).get(0).getRowSpan());

        grid.spanRow(3, 0, 0);
        SpreadsheetCell cell = grid.getRows().get(0).get(0);
        assertEquals(cell, grid.getRows().get(1).get(0));
        assertEquals(cell, grid.getRows().get(2).get(0));
    }

    /**
     * Test of mixed Span.
     */
    @Test
    public void testSpanBoth() {
        grid.spanRow(4, 0, 0);
        grid.spanColumn(5, 0, 0);
        SpreadsheetCell cell = grid.getRows().get(0).get(0);
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 5; j++) {
                assertEquals(cell, grid.getRows().get(i).get(j));
            }
        }
    }

    /**
     * Test of mixed Span.
     */
    @Test
    public void testSpanBoth2() {
        grid.spanColumn(5, 0, 0);
        grid.spanRow(4, 0, 0);
        SpreadsheetCell cell = grid.getRows().get(0).get(0);
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 5; j++) {
                assertEquals(cell, grid.getRows().get(i).get(j));
            }
        }
    }

    /**
     * Test of spanColumn method, of class GridBase.
     */
    @Test
    public void testSpanColumn() {
        System.out.println("spanColumn");
        grid.spanColumn(0, 0, 0);
        assertEquals(1, grid.getRows().get(0).get(0).getColumnSpan());

        grid.spanColumn(-1, -1, -1);
        grid.spanColumn(0, -1, 0);
        grid.spanColumn(0, 0, -1);
        grid.spanColumn(-1, 0, 0);
        grid.spanColumn(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        grid.spanColumn(2, grid.getRowCount(), grid.getColumnCount());
        
        grid.spanColumn(1, 0, 0);
        assertEquals(1, grid.getRows().get(0).get(0).getColumnSpan());

        grid.spanColumn(2, 0, 0);
        assertEquals(2, grid.getRows().get(0).get(0).getColumnSpan());
        assertEquals(2, grid.getRows().get(0).get(1).getColumnSpan());

        grid.spanColumn(3, 0, 0);
        SpreadsheetCell cell = grid.getRows().get(0).get(0);
        assertEquals(cell, grid.getRows().get(0).get(1));
        assertEquals(cell, grid.getRows().get(0).get(2));
    }
}
