package org.controlsfx.control.tableview2;

import java.time.Duration;
import java.time.LocalTime;

public final class TableView2TestUtils {

    private TableView2TestUtils() {
    }

    public static Duration measure(Runnable operation) {
        LocalTime start = LocalTime.now();
        operation.run();
        LocalTime end = LocalTime.now();
        return Duration.between(start, end);
    }

}
