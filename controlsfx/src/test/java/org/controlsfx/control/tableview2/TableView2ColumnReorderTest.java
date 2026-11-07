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
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TableView2ColumnReorderTest extends FxRobot {

    private static final int COLUMNS = 4;
    private static final int COLUMN_WIDTH = 120;
    private static final int ROWS = 30;
    private static final int SCENE_WIDTH = 600;
    private static final int SCENE_HEIGHT = 400;
    private static final int DRAGGED_COLUMN = 1;
    private static final double DRAG_DISTANCE = 10;

    private static class RowItem {
        final SimpleStringProperty[] values = new SimpleStringProperty[COLUMNS];

        RowItem(int row) {
            for (int c = 0; c < COLUMNS; c++) {
                values[c] = new SimpleStringProperty(row + "x" + c);
            }
        }
    }

    private TableView2<RowItem> tableView;
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

        FxToolkit.setupStage(stage -> {
            Scene scene = new Scene(tableView, SCENE_WIDTH, SCENE_HEIGHT);
            scene.addPostLayoutPulseListener(() -> pulseLatch.get().countDown());
            stage.setScene(scene);
            stage.show();
            stage.toFront();
        });
    }

    @After
    public void afterEach() throws TimeoutException {
        FxToolkit.setupStage(stage -> stage.setScene(null));
    }

    private TableColumn<RowItem, String> createColumn(int index) {
        TableColumn<RowItem, String> column = new TableColumn<>("#" + index);
        column.setPrefWidth(COLUMN_WIDTH);
        column.setCellValueFactory(param -> param.getValue().values[index]);
        return column;
    }

    @Test
    public void overlayAndLineAlignedWithDraggedColumnWhenRowHeaderVisible() throws InterruptedException {
        interact(() -> tableView.setRowHeaderVisible(true));
        assertOverlayAndLineAlignedWithDraggedColumn();
    }

    @Test
    public void overlayAndLineAlignedWithDraggedColumnWhenRowHeaderHidden() throws InterruptedException {
        interact(() -> tableView.setRowHeaderVisible(false));
        assertOverlayAndLineAlignedWithDraggedColumn();
    }

    private void assertOverlayAndLineAlignedWithDraggedColumn() throws InterruptedException {
        waitForPulse();
        TableColumnHeader header = columnHeader(tableView.getColumns().get(DRAGGED_COLUMN));
        Point2D press = header.localToScene(5, 5);

        // a short drag, so the drop position is the left edge of the dragged column
        interact(() -> Event.fireEvent(header, mouseEvent(MouseEvent.MOUSE_PRESSED, press)));
        interact(() -> Event.fireEvent(header, mouseEvent(MouseEvent.MOUSE_DRAGGED, press.add(DRAG_DISTANCE, 0))));
        waitForPulse();
        interact(() -> Event.fireEvent(header, mouseEvent(MouseEvent.MOUSE_DRAGGED, press.add(DRAG_DISTANCE + 2, 0))));
        waitForPulse();

        interact(() -> {
            Node overlay = tableView.lookup(".column-overlay");
            Node line = tableView.lookup(".column-resize-line");
            Node ghost = tableView.lookup(".column-drag-header");
            assertTrue("column overlay should be visible while dragging", overlay.isVisible());
            assertTrue("column reorder line should be visible while dragging", line.isVisible());
            assertTrue("ghost column header should be visible while dragging", ghost.isVisible());

            double columnX = minXInTable(header);
            assertEquals("column overlay x", columnX, minXInTable(overlay), 0.5);
            assertEquals("column reorder line x", columnX, minXInTable(line), 0.5);
            assertEquals("ghost column header x", columnX + DRAG_DISTANCE + 2, minXInTable(ghost), 0.5);
        });

        interact(() -> Event.fireEvent(header, mouseEvent(MouseEvent.MOUSE_RELEASED, press.add(DRAG_DISTANCE + 2, 0))));
    }

    private TableColumnHeader columnHeader(TableColumn<RowItem, ?> column) {
        return tableView.lookupAll(".column-header").stream()
                .filter(TableColumnHeader.class::isInstance)
                .map(TableColumnHeader.class::cast)
                .filter(h -> h.getTableColumn() == column)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No header for " + column.getText()));
    }

    private double minXInTable(Node node) {
        return tableView.sceneToLocal(node.localToScene(node.getBoundsInLocal())).getMinX();
    }

    private static MouseEvent mouseEvent(EventType<MouseEvent> type, Point2D scenePoint) {
        boolean primaryDown = type != MouseEvent.MOUSE_RELEASED;
        return new MouseEvent(type, scenePoint.getX(), scenePoint.getY(), scenePoint.getX(), scenePoint.getY(),
                MouseButton.PRIMARY, 1, false, false, false, false, primaryDown, false, false,
                false, false, false, null);
    }

    private void waitForPulse() throws InterruptedException {
        pulseLatch.set(new CountDownLatch(1));
        interact(() -> tableView.requestLayout());
        assertTrue("no layout pulse", pulseLatch.get().await(2, TimeUnit.SECONDS));
    }
}
