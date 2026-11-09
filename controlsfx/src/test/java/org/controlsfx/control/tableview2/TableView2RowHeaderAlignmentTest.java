/**
 * Copyright (c) 2026, ControlsFX
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
package org.controlsfx.control.tableview2;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * With a fixed cell size, the rows of the row header must keep the same pitch
 * as the data rows at any display scale, even when the cell content is taller
 * than the fixed cell size.
 */
public class TableView2RowHeaderAlignmentTest extends FxRobot {

    private static final int COLUMNS = 3;
    private static final int COLUMN_WIDTH = 100;
    private static final int ROWS = 300;
    private static final int SCENE_WIDTH = 420;
    private static final int SCENE_HEIGHT = 303;
    private static final double FIXED_CELL_SIZE = 20;
    // makes the cell content taller than the fixed cell size regardless of the font
    private static final Insets CELL_PADDING = new Insets(8);
    private static final int SCROLL_TO_ROW = 40;
    private static final double[] RENDER_SCALES = {1.0, 1.25, 1.5, 1.75, 2.0, 2.5};

    private static class RowItem {
        final SimpleStringProperty[] values = new SimpleStringProperty[COLUMNS];

        RowItem(int row) {
            for (int c = 0; c < COLUMNS; c++) {
                values[c] = new SimpleStringProperty(row + "x" + c);
            }
        }
    }

    private TableView2<RowItem> tableView;
    private Stage stage;
    private final AtomicReference<CountDownLatch> pulseLatch = new AtomicReference<>(new CountDownLatch(0));

    @BeforeClass
    public static void beforeAll() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @Before
    public void beforeEach() throws TimeoutException {
        tableView = new TableView2<>();
        tableView.getColumns().setAll(IntStream.range(0, COLUMNS)
                .mapToObj(this::createColumn)
                .collect(Collectors.toList()));
        tableView.getItems().setAll(IntStream.range(0, ROWS)
                .mapToObj(RowItem::new)
                .collect(Collectors.toList()));
        tableView.setRowHeaderVisible(true);
        tableView.setFixedCellSize(FIXED_CELL_SIZE);

        FxToolkit.setupStage(stage -> {
            this.stage = stage;
            Scene scene = new Scene(tableView, SCENE_WIDTH, SCENE_HEIGHT);
            scene.addPostLayoutPulseListener(() -> pulseLatch.get().countDown());
            stage.setScene(scene);
            stage.show();
            stage.toFront();
        });
    }

    @After
    public void afterEach() throws TimeoutException {
        FxToolkit.setupStage(stage -> {
            stage.setRenderScaleX(1.0);
            stage.setRenderScaleY(1.0);
            stage.setScene(null);
        });
    }

    private TableColumn<RowItem, String> createColumn(int index) {
        TableColumn<RowItem, String> column = new TableColumn<>("#" + index);
        column.setPrefWidth(COLUMN_WIDTH);
        column.setCellValueFactory(param -> param.getValue().values[index]);
        column.setCellFactory(c -> new TableCell<>() {
            {
                setPadding(CELL_PADDING);
            }
        });
        return column;
    }

    @Test
    public void rowHeaderRowsAlignedWithDataRowsAtAnyRenderScale() throws InterruptedException {
        for (double scale : RENDER_SCALES) {
            interact(() -> {
                stage.setRenderScaleX(scale);
                stage.setRenderScaleY(scale);
            });
            waitForPulse();
            interact(() -> tableView.scrollTo(SCROLL_TO_ROW));
            waitForPulse();
            waitForPulse();
            interact(() -> assertRowHeaderAligned(scale));
        }
    }

    private void assertRowHeaderAligned(double scale) {
        Map<Integer, TableRow<?>> dataRows = new TreeMap<>();
        Map<Integer, TableRow<?>> headerRows = new TreeMap<>();
        for (Node node : tableView.lookupAll(".table-row-cell")) {
            if (!(node instanceof TableRow) || !node.isVisible()) {
                continue;
            }
            TableRow<?> row = (TableRow<?>) node;
            if (row.getIndex() < 0) {
                continue;
            }
            (row.getTableView() == tableView ? dataRows : headerRows).put(row.getIndex(), row);
        }

        // positions are snapped to device pixels, so allow at most one of them
        double devicePixel = 1 / scale + 0.001;
        int compared = 0;
        for (Map.Entry<Integer, TableRow<?>> entry : dataRows.entrySet()) {
            TableRow<?> dataRow = entry.getValue();
            TableRow<?> headerRow = headerRows.get(entry.getKey());
            if (headerRow == null) {
                continue;
            }
            String context = "scale " + scale + ", row " + entry.getKey();
            assertEquals("data row height, " + context, FIXED_CELL_SIZE, dataRow.getHeight(), 0.001);
            assertEquals("row header row height, " + context, FIXED_CELL_SIZE, headerRow.getHeight(), 0.001);
            assertEquals("row header row y, " + context, minYInTable(dataRow), minYInTable(headerRow), devicePixel);
            compared++;
        }
        assertTrue("too few rows compared at scale " + scale + ": " + compared, compared >= 10);
    }

    private double minYInTable(Node node) {
        return tableView.sceneToLocal(node.localToScene(node.getBoundsInLocal())).getMinY();
    }

    private void waitForPulse() throws InterruptedException {
        pulseLatch.set(new CountDownLatch(1));
        interact(() -> tableView.requestLayout());
        assertTrue("no layout pulse", pulseLatch.get().await(2, TimeUnit.SECONDS));
    }
}
