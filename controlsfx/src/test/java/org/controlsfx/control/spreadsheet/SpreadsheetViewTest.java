package org.controlsfx.control.spreadsheet;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.embed.swing.JFXPanel;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCellEditor;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.controlsfx.control.spreadsheet.SpreadsheetView.SpanType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author samir.hadzic
 */
public class SpreadsheetViewTest {

    private SpreadsheetView spv;

    public SpreadsheetViewTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        new JFXPanel();
        //100 rows and 15 columns
        spv = new SpreadsheetView();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setGrid method, of class SpreadsheetView.
     */
    @Test
    public void testSetGrid() {
        System.out.println("setGrid");
        Grid grid = null;
        SpreadsheetView instance = new SpreadsheetView();
        instance.setGrid(grid);
    }

    /**
     * Test of isRowFixable method, of class SpreadsheetView.
     */
    @Test
    public void testIsRowFixable() {
        System.out.println("isRowFixable");

        Grid grid = spv.getGrid();
        //Normal
        int row = 0;
        Assert.assertTrue(spv.isRowFixable(row));

        row = -1;
        Assert.assertFalse(spv.isRowFixable(row));

        row = Integer.MAX_VALUE;
        Assert.assertFalse(spv.isRowFixable(row));

        grid.spanColumn(5, 0, 0);
        spv.setGrid(grid);

        row = 0;
        Assert.assertTrue(spv.isRowFixable(row));

        grid.spanRow(3, 0, 0);
        spv.setGrid(grid);
        Assert.assertFalse(spv.isRowFixable(0));
        Assert.assertFalse(spv.isRowFixable(1));
        Assert.assertFalse(spv.isRowFixable(2));
        Assert.assertTrue(spv.isRowFixable(3));

    }

    /**
     * Test of areRowsFixable method, of class SpreadsheetView.
     */
    @Test
    public void testAreRowsFixable() {
        System.out.println("areRowsFixable");

        Grid grid = spv.getGrid();
        List<Integer> list = new ArrayList<>();

        Assert.assertFalse(spv.areRowsFixable(null));

        Assert.assertFalse(spv.areRowsFixable(Collections.emptyList()));

        list.clear();
        list.add(-1);
        Assert.assertFalse(spv.areRowsFixable(list));

        list.clear();
        list.add(Integer.MAX_VALUE);
        Assert.assertFalse(spv.areRowsFixable(list));

        list.clear();
        list.add(0);
        list.add(2);
        list.add(4);
        Assert.assertTrue(spv.areRowsFixable(list));

        grid.spanColumn(3, 0, 0);
        grid.spanColumn(3, 2, 0);
        grid.spanColumn(3, 4, 0);
        list.clear();
        list.add(0);
        list.add(2);
        list.add(4);
        Assert.assertTrue(spv.areRowsFixable(list));

        grid.spanRow(3, 0, 0);
        list.clear();
        list.add(0);
        list.add(1);
        list.add(2);
        Assert.assertTrue(spv.areRowsFixable(list));
    }

    /**
     * Test of isFixingRowsAllowed method, of class SpreadsheetView.
     */
    @Test
    public void testIsFixingRowsAllowed() {
        System.out.println("isFixingRowsAllowed");

        spv.setFixingRowsAllowed(true);
        Assert.assertTrue(spv.isFixingRowsAllowed());

        spv.setFixingRowsAllowed(false);
        Assert.assertFalse(spv.isFixingRowsAllowed());
    }

    /**
     * Test of setFixingRowsAllowed method, of class SpreadsheetView.
     */
    @Test
    public void testSetFixingRowsAllowed() {
        System.out.println("setFixingRowsAllowed");

        spv.setFixingRowsAllowed(true);
        Assert.assertTrue(spv.isFixingRowsAllowed());

        spv.setFixingRowsAllowed(false);
        Assert.assertFalse(spv.isFixingRowsAllowed());

        Assert.assertFalse(spv.isRowFixable(0));

        List<Integer> list = new ArrayList<>();
        list.add(1);
        Assert.assertFalse(spv.areRowsFixable(list));
    }

    /**
     * Test of isColumnFixable method, of class SpreadsheetView.
     */
    @Test
    @Ignore
    public void testIsColumnFixable() {
        System.out.println("isColumnFixable");
//        int columnIndex = 0;
//        SpreadsheetView instance = new SpreadsheetView();
//        boolean expResult = false;
//        boolean result = instance.isColumnFixable(columnIndex);
//        assertEquals(expResult, result);
    }

    /**
     * Test of isFixingColumnsAllowed method, of class SpreadsheetView.
     */
    @Test
    public void testIsFixingColumnsAllowed() {
        System.out.println("isFixingColumnsAllowed");
        spv.setFixingColumnsAllowed(true);
        Assert.assertTrue(spv.isFixingColumnsAllowed());

        spv.setFixingColumnsAllowed(false);
        Assert.assertFalse(spv.isFixingColumnsAllowed());
    }

    /**
     * Test of setFixingColumnsAllowed method, of class SpreadsheetView.
     */
    @Test
    public void testSetFixingColumnsAllowed() {
        System.out.println("setFixingColumnsAllowed");

        spv.setFixingColumnsAllowed(true);
        Assert.assertTrue(spv.isFixingColumnsAllowed());

        spv.setFixingColumnsAllowed(false);
        Assert.assertFalse(spv.isFixingColumnsAllowed());

        Assert.assertFalse(spv.getColumns().get(0).isColumnFixable());
    }

    /**
     * Test of setShowColumnHeader method, of class SpreadsheetView.
     */
    @Test
    public void testSetShowColumnHeader() {
        System.out.println("setShowColumnHeader");

        spv.setShowColumnHeader(false);
        Assert.assertFalse(spv.isShowColumnHeader());

        spv.setShowColumnHeader(true);
        Assert.assertTrue(spv.isShowColumnHeader());
    }

    /**
     * Test of isShowColumnHeader method, of class SpreadsheetView.
     */
    @Test
    public void testIsShowColumnHeader() {
        System.out.println("isShowColumnHeader");

        spv.setShowColumnHeader(false);
        Assert.assertFalse(spv.isShowColumnHeader());

        spv.setShowColumnHeader(true);
        Assert.assertTrue(spv.isShowColumnHeader());
    }

    /**
     * Test of setShowRowHeader method, of class SpreadsheetView.
     */
    @Test
    public void testSetShowRowHeader() {
        System.out.println("setShowRowHeader");

        spv.setShowRowHeader(false);
        Assert.assertFalse(spv.isShowRowHeader());

        spv.setShowRowHeader(true);
        Assert.assertTrue(spv.isShowRowHeader());
    }

    /**
     * Test of isShowRowHeader method, of class SpreadsheetView.
     */
    @Test
    public void testIsShowRowHeader() {
        System.out.println("isShowRowHeader");

        spv.setShowRowHeader(false);
        Assert.assertFalse(spv.isShowRowHeader());

        spv.setShowRowHeader(true);
        Assert.assertTrue(spv.isShowRowHeader());
    }

    /**
     * Test of getRowHeight method, of class SpreadsheetView.
     */
    @Test
    public void testGetRowHeight() {
        System.out.println("getRowHeight");

        Map<Integer, Double> rowHeight = new HashMap<>();
        rowHeight.put(1, 100.0);
        rowHeight.put(5, 12.0);

        GridBase grid = (GridBase) spv.getGrid();
        grid.setRowHeightCallback(new GridBase.MapBasedRowHeightFactory(rowHeight));

        double result = spv.getRowHeight(1);
        assertEquals(100.0, result, 0.0);
        
        result = spv.getRowHeight(5);
        assertEquals(12.0, result, 0.0);
    }

    /**
     * Test of getEditor method, of class SpreadsheetView.
     */
    @Test
    public void testGetEditor() {
        System.out.println("getEditor");
        
        SpreadsheetCellType cellType = null;
        Optional<SpreadsheetCellEditor> result = spv.getEditor(cellType);
        assertEquals(Optional.empty(), result);
        
        cellType = SpreadsheetCellType.DATE;
        result = spv.getEditor(cellType);
        assertNotNull(result);
        if(!result.isPresent()){
            fail();
        }
    }

    /**
     * Test of setEditable method, of class SpreadsheetView.
     */
    @Test
    public void testSetEditable() {
        System.out.println("setEditable");
      
        spv.setEditable(false);
        assertFalse(spv.isEditable());
        
        //FIXME To put in GridBase test
//        String value = "The cake is a lie";
//        spv.getGrid().setCellValue(0, 0, value);
//        
//        assertEquals("", spv.getGrid().getRows().get(0).get(0).getItem());
        
        spv.setEditable(true);
        assertTrue(spv.isEditable());
        
//        spv.getGrid().setCellValue(0, 0, value);
//        
//        assertEquals(value, spv.getGrid().getRows().get(0).get(0).getItem());
    }

    /**
     * Test of deleteSelectedCells method, of class SpreadsheetView.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testDeleteSelectedCells() throws InterruptedException {
        System.out.println("deleteSelectedCells");
        
        spv.setEditable(true);
        String value = "The cake is a lie";
        spv.getGrid().setCellValue(0, 0, value);
        
        assertEquals(value, spv.getGrid().getRows().get(0).get(0).getItem());

        while(spv.getSelectionModel().getTableView().getColumns().isEmpty()){
            Thread.sleep(500);
        }
        
        spv.getSelectionModel().select(0, spv.getSelectionModel().getTableView().getColumns().get(0));
        spv.deleteSelectedCells();
        
        assertNull(spv.getGrid().getRows().get(0).get(0).getItem());
    }

    /**
     * Test of getSpanType method, of class SpreadsheetView.
     */
    @Test
    public void testGetSpanType() {
        System.out.println("getSpanType");
      
        int row = 0;
        int column = 0;
        Grid grid = spv.getGrid();
        SpanType type = SpreadsheetView.SpanType.NORMAL_CELL;

        assertEquals(type, spv.getSpanType(-1, -1));
        assertEquals(type, spv.getSpanType(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(type, spv.getSpanType(Integer.MAX_VALUE, -1));
        assertEquals(type, spv.getSpanType(-1, Integer.MAX_VALUE));
        assertEquals(type, spv.getSpanType(grid.getRowCount(), grid.getColumnCount()));
        
    }
}
