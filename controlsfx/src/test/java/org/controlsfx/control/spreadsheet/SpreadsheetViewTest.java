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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TablePosition;
import org.controlsfx.control.spreadsheet.SpreadsheetView.SpanType;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

public class SpreadsheetViewTest {
    @Rule public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();
    
    private SpreadsheetView spv;

    public SpreadsheetViewTest() {
    }

    @Before
    public void setUp() {
        //100 rows and 15 columns
        spv = new SpreadsheetView();
    }

    private GridBase buildGrid() {
        GridBase tempGrid;
        tempGrid = new GridBase(15, 15);
        List<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();

        for (int row = 0; row < tempGrid.getRowCount(); ++row) {
            ObservableList<SpreadsheetCell> currentRow = FXCollections.observableArrayList();
            for (int column = 0; column < tempGrid.getColumnCount(); ++column) {
                currentRow.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1, ""));
            }
            rows.add(currentRow);
        }
        tempGrid.setRows(rows);
        return tempGrid;
    }
    
    private static class NonSerializableClass{
        
    }
    
    
    /**
     * We test that a non-serializable item can be put into the Grid, and then
     * try to copy without throwing an exception.
     */
    @Test public void testCopyClipBoard() {
        spv.getSelectionModel().select(0, spv.getColumns().get(0));

        Grid grid = spv.getGrid();
        SpreadsheetCell cell = new SpreadsheetCellBase(0, 0, 1, 1, SpreadsheetCellType.OBJECT);
        cell.setItem(new NonSerializableClass());
        grid.getRows().get(0).set(0, cell);

        spv.copyClipboard();
    }
    
    /**
     * We test that a null item does not throw exception in copyClipboard.
     */
    @Test public void testCopyClipBoardNull() {
        spv.getSelectionModel().select(0, spv.getColumns().get(0));

        Grid grid = spv.getGrid();
        SpreadsheetCell cell = new SpreadsheetCellBase(0, 0, 1, 1, SpreadsheetCellType.OBJECT);
        cell.setItem(null);
        grid.getRows().get(0).set(0, cell);

        spv.copyClipboard();
    }

    /**
     * Try to select a cell, then set a new grid, and verify that the
     * selectedCells are well updated because we have modified the TableColumn
     * so the TablePosition are normally wrong.
     *
     */
    @Test public void testSelectionModel(){
        spv.getSelectionModel().select(10, spv.getColumns().get(10));
        spv.setGrid(buildGrid());

        if (spv.getSelectionModel().getSelectedCells().size() != 1) {
            fail();
        }

        TablePosition position = spv.getSelectionModel().getSelectedCells().get(0);
        assertEquals(10, position.getRow());
        assertEquals(10, position.getColumn());
    }
    /**
     * Test of isRowFixable method, of class SpreadsheetView.
     */
    @Test public void testIsRowFixable() {
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
    @Test public void testAreRowsFixable() {
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
    @Test public void testIsFixingRowsAllowed() {
        spv.setFixingRowsAllowed(true);
        Assert.assertTrue(spv.isFixingRowsAllowed());

        spv.setFixingRowsAllowed(false);
        Assert.assertFalse(spv.isFixingRowsAllowed());
    }

    /**
     * Test of setFixingRowsAllowed method, of class SpreadsheetView.
     */
    @Test public void testSetFixingRowsAllowed() {
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
//        System.out.println("isColumnFixable");
//        int columnIndex = 0;
//        SpreadsheetView instance = new SpreadsheetView();
//        boolean expResult = false;
//        boolean result = instance.isColumnFixable(columnIndex);
//        assertEquals(expResult, result);
    }

    /**
     * Test of isFixingColumnsAllowed method, of class SpreadsheetView.
     */
    @Test public void testIsFixingColumnsAllowed() {
        spv.setFixingColumnsAllowed(true);
        Assert.assertTrue(spv.isFixingColumnsAllowed());

        spv.setFixingColumnsAllowed(false);
        Assert.assertFalse(spv.isFixingColumnsAllowed());
    }

    /**
     * Test of setFixingColumnsAllowed method, of class SpreadsheetView.
     */
    @Test public void testSetFixingColumnsAllowed() {

        spv.setFixingColumnsAllowed(true);
        Assert.assertTrue(spv.isFixingColumnsAllowed());

        spv.setFixingColumnsAllowed(false);
        Assert.assertFalse(spv.isFixingColumnsAllowed());

        Assert.assertFalse(spv.getColumns().get(0).isColumnFixable());
    }

    /**
     * Test of setShowColumnHeader method, of class SpreadsheetView.
     */
    @Test public void testSetShowColumnHeader() {
        spv.setShowColumnHeader(false);
        Assert.assertFalse(spv.isShowColumnHeader());

        spv.setShowColumnHeader(true);
        Assert.assertTrue(spv.isShowColumnHeader());
    }

    /**
     * Test of isShowColumnHeader method, of class SpreadsheetView.
     */
    @Test public void testIsShowColumnHeader() {
        spv.setShowColumnHeader(false);
        Assert.assertFalse(spv.isShowColumnHeader());

        spv.setShowColumnHeader(true);
        Assert.assertTrue(spv.isShowColumnHeader());
    }

    /**
     * Test of setShowRowHeader method, of class SpreadsheetView.
     */
    @Test public void testSetShowRowHeader() {
        spv.setShowRowHeader(false);
        Assert.assertFalse(spv.isShowRowHeader());

        spv.setShowRowHeader(true);
        Assert.assertTrue(spv.isShowRowHeader());
    }

    /**
     * Test of isShowRowHeader method, of class SpreadsheetView.
     */
    @Test public void testIsShowRowHeader() {
        spv.setShowRowHeader(false);
        Assert.assertFalse(spv.isShowRowHeader());

        spv.setShowRowHeader(true);
        Assert.assertTrue(spv.isShowRowHeader());
    }

    /**
     * Test of getRowHeight method, of class SpreadsheetView.
     */
    @Test public void testGetRowHeight() {
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
    @Test public void testGetEditor() {
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
    @Test public void testSetEditable() {
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
    @Test public void testDeleteSelectedCells() throws InterruptedException {
        System.out.println("deleteSelectedCells");
        
        spv.setEditable(true);
        String value = "The cake is a lie";
        spv.getGrid().setCellValue(0, 0, value);
        
        assertEquals(value, spv.getGrid().getRows().get(0).get(0).getItem());

        spv.getSelectionModel().select(0, spv.getColumns().get(0));
        spv.deleteSelectedCells();
        
        assertNull(spv.getGrid().getRows().get(0).get(0).getItem());
    }

    /**
     * Test of getSpanType method, of class SpreadsheetView.
     */
    @Test public void testGetSpanType() {
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
