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
package org.controlsfx.control.table;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;

/**
 *
 */
public final class TableMenuButtonAccessor {

    public static void modifyTableMenu(final TableView<?> tableView, final Consumer<ContextMenu> consumer) {
        if (tableView.getScene() == null) {
            tableView.sceneProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable o) {
                    tableView.sceneProperty().removeListener(this);
                    modifyTableMenu(tableView, consumer);
                }
            });
            
            return;
        }
        
        Skin<?> skin = tableView.getSkin();
        if (skin == null) {
            tableView.skinProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable o) {
                    tableView.skinProperty().removeListener(this);
                    modifyTableMenu(tableView, consumer);
                }
            });
            
            return;
        }

        doModify(tableView, consumer);
    }

    private static void doModify(TableView<?> tableView, Consumer<ContextMenu> consumer) {
        Skin<?> skin = tableView.getSkin();
        if (! (skin instanceof TableViewSkin)) return;

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
            Field privateContextMenuField = TableHeaderRow.class.getDeclaredField("columnPopupMenu");
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
