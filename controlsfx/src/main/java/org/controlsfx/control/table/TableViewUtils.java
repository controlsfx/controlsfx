/**
 * Copyright (c) 2014, 2016 ControlsFX
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
package org.controlsfx.control.table;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;

/**
 * A utility class for API revolving around the JavaFX {@link TableView} and
 * {@link TreeTableView} controls.
 */
// not public as not ready for 8.20.7
final class TableViewUtils {

    /**
     * Call this method to be able to programatically manipulate the 
     * {@link TableView#tableMenuButtonVisibleProperty() TableView menu button}
     * (assuming it is visible). This allows developers to, for example, add in
     * new {@link MenuItem}.
     */
    public static void modifyTableMenu(final TableView<?> tableView, final Consumer<ContextMenu> consumer) {
        modifyTableMenu((Control)tableView, consumer);
    }
    
    /**
     * Call this method to be able to programatically manipulate the 
     * {@link TreeTableView#tableMenuButtonVisibleProperty() TreeTableView menu button}
     * (assuming it is visible). This allows developers to, for example, add in
     * new {@link MenuItem}.
     */
    public static void modifyTableMenu(final TreeTableView<?> treeTableView, final Consumer<ContextMenu> consumer) {
        modifyTableMenu((Control)treeTableView, consumer);
    }
    
    private static void modifyTableMenu(final Control control, final Consumer<ContextMenu> consumer) {
        if (control.getScene() == null) {
            control.sceneProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable o) {
                    control.sceneProperty().removeListener(this);
                    modifyTableMenu(control, consumer);
                }
            });
            
            return;
        }
        
        Skin<?> skin = control.getSkin();
        if (skin == null) {
            control.skinProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable o) {
                    control.skinProperty().removeListener(this);
                    modifyTableMenu(control, consumer);
                }
            });
            
            return;
        }

        doModify(skin, consumer);
    }

    private static void doModify(Skin<?> skin, Consumer<ContextMenu> consumer) {
        if (! (skin instanceof TableViewSkinBase)) return;

        TableViewSkin<?> tableSkin = (TableViewSkin<?>)skin;
        TableHeaderRow headerRow = getHeaderRow(tableSkin);
        if (headerRow == null) return;

        ContextMenu contextMenu = getContextMenu(headerRow);
        consumer.accept(contextMenu);        
    }

    private static TableHeaderRow getHeaderRow(TableViewSkin<?> tableSkin) {
        ObservableList<Node> children = tableSkin.getChildren();
        for (int i = 0, max = children.size(); i < max; i++) {
            Node child = children.get(i);
            if (child instanceof TableHeaderRow) return (TableHeaderRow) child;
        }
        return null;
    }

    private static ContextMenu getContextMenu(TableHeaderRow headerRow) {
        try {
            Field privateContextMenuField = TableHeaderRow.class.getDeclaredField("columnPopupMenu"); //$NON-NLS-1$
            privateContextMenuField.setAccessible(true);
            ContextMenu contextMenu = (ContextMenu) privateContextMenuField.get(headerRow);
            return contextMenu;
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
