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
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNull;

public class TableView2LayoutTest extends FxRobot {

    private static final int FIXED_CELL_SIZE = 20;
    private static final int COLUMNS = 6;
    private static final int COLUMN_WIDTH = 120;
    // narrower than the columns, so the horizontal scrollbar shows up
    private static final int SCENE_WIDTH = 500;
    private static final int SCENE_HEIGHT = 300;
    private static final int BATCHES = 10;
    private static final int ROWS_PER_BATCH = 4;

    private static class RowItem {
        final SimpleStringProperty[] values = new SimpleStringProperty[COLUMNS];

        RowItem(int row) {
            for (int c = 0; c < COLUMNS; c++) {
                values[c] = new SimpleStringProperty(row + "x" + c);
            }
        }
    }

    private TableView2<RowItem> tableView;
    private Scene scene;
    private final AtomicReference<Throwable> fxThreadError = new AtomicReference<>();
    private final AtomicReference<String> reentrantLayout = new AtomicReference<>();
    private final AtomicReference<CountDownLatch> pulseLatch = new AtomicReference<>(new CountDownLatch(0));
    private Thread.UncaughtExceptionHandler previousHandler;

    @BeforeClass
    public static void beforeAll() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @Before
    public void beforeEach() throws TimeoutException {
        tableView = new TableView2<>();
        tableView.setFixedCellSize(FIXED_CELL_SIZE);
        tableView.getColumns().setAll(IntStream.range(0, COLUMNS)
                .mapToObj(this::createColumn)
                .collect(Collectors.toList()));

        FxToolkit.setupStage(stage -> {
            previousHandler = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> fxThreadError.compareAndSet(null, e));
            scene = new Scene(tableView, SCENE_WIDTH, SCENE_HEIGHT);
            scene.addPostLayoutPulseListener(() -> pulseLatch.get().countDown());
            stage.setScene(scene);
            stage.show();
            stage.toFront();
        });
        interact(this::watchSheets);
    }

    @After
    public void afterEach() throws TimeoutException {
        FxToolkit.setupStage(stage -> {
            Thread.currentThread().setUncaughtExceptionHandler(previousHandler);
            stage.setScene(null);
        });
    }

    private TableColumn<RowItem, String> createColumn(int index) {
        TableColumn<RowItem, String> column = new TableColumn<>("#" + index);
        column.setPrefWidth(COLUMN_WIDTH);
        column.setCellValueFactory(param -> param.getValue().values[index]);
        column.setCellFactory(c -> new TableCell<>() {
            {
                // taller than FIXED_CELL_SIZE
                setPadding(new Insets(8, 4, 8, 4));
            }
        });
        return column;
    }

    private void watchSheets() {
        tableView.lookupAll(".sheet").stream()
            .filter(Parent.class::isInstance)
            .map(Parent.class::cast)
            .forEach(sheet -> {
                sheet.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
                    while (change.next()) {
                        if (change.wasRemoved() && isInsideRowLayout()) {
                            reentrantLayout.compareAndSet(null, "sheet modified from "
                                    + "TableRow2Skin.layoutChildren with " + tableView.getItems().size() + " items");
                        }
                    }
                });
            });
    }

    private static boolean isInsideRowLayout() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(frame -> frame.getClassName().endsWith("TableRow2Skin") &&
                        frame.getMethodName().equals("layoutChildren"));
    }

    @Test
    public void addingRowsTallerThanFixedCellSizeDoesNotLayoutFlowFromRow() throws InterruptedException {
        for (int batch = 0; batch < BATCHES; batch++) {
            final int first = batch * ROWS_PER_BATCH;
            pulseLatch.set(new CountDownLatch(1));
            interact(() -> {
                tableView.getItems().addAll(IntStream.range(first, first + ROWS_PER_BATCH)
                        .mapToObj(RowItem::new)
                        .collect(Collectors.toList()));
            });
            pulseLatch.get().await(2, TimeUnit.SECONDS);
            assertNull(String.valueOf(fxThreadError.get()), fxThreadError.get());
            assertNull(reentrantLayout.get(), reentrantLayout.get());
        }
    }
}
