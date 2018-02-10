package impl.org.controlsfx;

import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.webkit.WebPage;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.*;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ReflectionUtils {

    private ReflectionUtils() {
        // no-op
    }

    /****************************************************************************************************
     *
     * TableHeaderRow
     *
     ****************************************************************************************************/

    public static Optional<NestedTableColumnHeader> getRootHeaderFrom(TableHeaderRow tableHeaderRow) {
        try {
            Method method = tableHeaderRow.getClass().getDeclaredMethod("getRootHeader");
            method.setAccessible(true);
            return Optional.of((NestedTableColumnHeader) method.invoke(tableHeaderRow));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /****************************************************************************************************
     *
     * NestedTableColumnHeader
     *
     ****************************************************************************************************/

    public static Optional<Region> columnReorderLine(NestedTableColumnHeader tableColumnHeader) {
        try {
            Field field = tableColumnHeader.getClass().getDeclaredField("columnReorderLine");
            field.setAccessible(true);
            return Optional.of((Region) field.get(tableColumnHeader));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /****************************************************************************************************
     *
     * VirtualFlow
     *
     ****************************************************************************************************/

    public static void recreateCells(VirtualFlow flow) {
        Class<?> clazz = flow.getClass();
        try {
            Method method = clazz.getDeclaredMethod("recreateCells");
            method.setAccessible(true);
            method.invoke(flow);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Cannot recreate cells on VirtualFlow");
        }
    }

     public static void rebuildCells(VirtualFlow flow) {
        Class<?> clazz = flow.getClass();
        try {
            Method method = clazz.getDeclaredMethod("rebuildCells");
            method.setAccessible(true);
            method.invoke(flow);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Cannot rebuild cells on VirtualFlow");
        }
    }

    public static void reconfigureCells(VirtualFlow flow) {
        Class<?> clazz = flow.getClass();
        try {
            Method method = clazz.getDeclaredMethod("reconfigureCells");
            method.setAccessible(true);
            method.invoke(flow);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Cannot reconfigure cells on VirtualFlow");
        }
    }

    public static void resizeCellSize(VirtualFlow flow, IndexedCell cell) {
        Class<?> clazz = flow.getClass();
        try {
            Method method = clazz.getDeclaredMethod("resizeCellSize", cell.getClass());
            method.setAccessible(true);
            method.invoke(flow, cell);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Cannot resize cell size on VirtualFlow");
        }
    }

    public static List getCells(VirtualFlow flow) {
        Class<? extends VirtualFlow> clazz = flow.getClass();
        try {
            Method method = clazz.getDeclaredMethod("getCells");
            method.setAccessible(true);
            return (List) method.invoke(flow);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static Optional<IndexedCell<?>> getCellWithinViewPort(VirtualFlow flow, String methodName) {
        Class<?> clazz = flow.getClass();
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return Optional.of((IndexedCell<?>) method.invoke(flow));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<ScrollBar> getBar(VirtualFlow flow, String methodName) {
        Class<?> clazz = flow.getClass();
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return Optional.of((ScrollBar) method.invoke(flow));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /****************************************************************************************************
     *
     * VirtualContainerBase
     *
     ****************************************************************************************************/

    public static VirtualFlow getVirtualFlow(VirtualContainerBase virtualContainerBase) {
        Class<?> clazz = virtualContainerBase.getClass();
        try {
            Field field = clazz.getSuperclass().getDeclaredField("flow");
            field.setAccessible(true);
            return (VirtualFlow) field.get(virtualContainerBase);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Could not fetch VirtualFlow from VirtualContainerBase");
        }
    }

    /****************************************************************************************************
     *
     * TableViewSkinBase
     *
     ****************************************************************************************************/

    public static Optional<TableHeaderRow> getTableHeaderRowFrom(TableViewSkin<?> skin) {
        try {
            Method method = skin.getClass().getSuperclass().getDeclaredMethod("getTableHeaderRow");
            method.setAccessible(true);
            return Optional.of((TableHeaderRow) method.invoke(skin));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<TableViewSkinBase<?, ?, ?, ?, TableColumnBase<?,?>>> getTableSkin(NestedTableColumnHeader tableColumnHeader) {
        try {
            Method method = tableColumnHeader.getClass().getDeclaredMethod("getTableSkin");
            method.setAccessible(true);
            return Optional.of((TableViewSkinBase<?, ?, ?, ?, TableColumnBase<?,?>>) method.invoke(tableColumnHeader));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static VirtualFlow getVirtualFlow(TableViewSkinBase tableViewSkinBase) {
        Class<? extends TableViewSkinBase> clazz = tableViewSkinBase.getClass();
        try {
            Field field = clazz.getDeclaredField("flow");
            field.setAccessible(true);
            return (VirtualFlow) field.get(tableViewSkinBase);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Cannot fetch VirtualFlow from TableViewSkinBase");
        }
    }

    public static Integer onScroll(TableViewSkinBase tableViewSkinBase, String methodName, Boolean bool) {
        Class<?> clazz = tableViewSkinBase.getClass();
        try {
            Method method = clazz.getDeclaredMethod(methodName, Boolean.class);
            method.setAccessible(true);
            return (Integer)method.invoke(tableViewSkinBase, bool);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /****************************************************************************************************
     *
     * StyleManager
     *
     ****************************************************************************************************/

    public static void addUserAgentStylesheet(String stylesheet) {
        try {
            Class<?> styleManagerClass = Class.forName("com.sun.javafx.css.StyleManager");
            Method getInstance = styleManagerClass.getMethod("getInstance");
            Object styleManager = getInstance.invoke(styleManagerClass);
            Method addUserStyleSheet = styleManagerClass.getMethod("addUserAgentStylesheet", String.class);
            addUserStyleSheet.invoke(styleManager, stylesheet);
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Cannot add UserAgentStylesheet as the method is not accessible");
        }
    }

    /****************************************************************************************************
     *
     * Accessor
     *
     ****************************************************************************************************/

    public static Optional<WebPage> getPageFor(WebEngine webEngine) {
        try {
            Class<?> accessorClass = Class.forName("com.sun.javafx.webkit.Accessor");
            Method getInstance = accessorClass.getMethod("getPageFor");
            WebPage webPage = (WebPage) getInstance.invoke(accessorClass, webEngine);
            return Optional.of(webPage);
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Cannot fetch WebPage from WebEngine");
        }
    }

    /****************************************************************************************************
     *
     * TraversalEngine
     *
     ****************************************************************************************************/

    public static void setTraversalEngine(Control control, ParentTraversalEngine engine) {
        try {
            Class<?> parentHelper = Class.forName("com.sun.javafx.scene.ParentHelper");
            Method method = parentHelper.getMethod("setTraversalEngine", Parent.class, engine.getClass());
            method.setAccessible(true);
            method.invoke(parentHelper, control, engine);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot set Traversal Engine");
        }
    }

    /****************************************************************************************************
     *
     * Miscellaneous
     *
     ****************************************************************************************************/

    public static void callMethod(Object object, String methodName, Object... params) {
        Class<?> clazz = object.getClass();
        try {
            Method method = clazz.getDeclaredMethod(methodName, Arrays.stream(params).map(Object::getClass).toArray(Class[]::new));
            method.setAccessible(true);
            method.invoke(object, params);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Cannot call method " + methodName + " on " + object.getClass());
        }
    }
}
