package com.sun.javafx.scene.control.skin;

import javafx.scene.control.IndexedCell;
import javafx.util.Callback;

/**
 * This class is currently needed because VirtualFlow.setCreateCell(...) is protected and the grid
 * skin needs access...
 * 
 * @author hendrikebbers
 * 
 */
public class VirtualFlowHelper {

    /**
     * Set a cell factory for a VirtualFlow
     * 
     * @param flow
     * @param cc
     */
    public static void setCreateCell(VirtualFlow flow, Callback<VirtualFlow, ? extends IndexedCell> cc) {
        flow.setCreateCell(cc);
    }
}
