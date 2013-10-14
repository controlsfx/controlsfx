package org.controlsfx.control;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

public class CheckBoxComboBox<T> extends ComboBox<T> {
    
    private ObservableList<T> items;
    private final Map<T, BooleanProperty> itemBooleanMap;
    
    private BitSet selectedIndices;
    
    private ReadOnlyUnbackedObservableList<Integer> selectedIndicesList;
    private ReadOnlyUnbackedObservableList<T> selectedItemsList;

    public CheckBoxComboBox() {
        this(null);
    }
    
    public CheckBoxComboBox(final ObservableList<T> items) {
        super(items);
        
        final int initialSize = items == null ? 32 : items.size(); 
        this.itemBooleanMap = new HashMap<T, BooleanProperty>(initialSize);
        
        this.items = items == null ? FXCollections.<T>observableArrayList() : items;
        this.items.addListener(new ListChangeListener<T>() {
            @Override public void onChanged(Change<? extends T> c) {
                updateMap(c);
            }
        });
        updateMap(null);
        
        this.selectedIndices = new BitSet(initialSize);
        
        this.selectedIndicesList = new ReadOnlyUnbackedObservableList<Integer>() {
            @Override public Integer get(int index) {
                if (index < 0 || index >= items.size()) return -1;

                for (int pos = 0, val = selectedIndices.nextSetBit(0);
                    val >= 0 || pos == index;
                    pos++, val = selectedIndices.nextSetBit(val+1)) {
                        if (pos == index) return val;
                }

                return -1;
            }

            @Override public int size() {
                return selectedIndices.cardinality();
            }

            @Override public boolean contains(Object o) {
                if (o instanceof Number) {
                    Number n = (Number) o;
                    int index = n.intValue();

                    return index >= 0 && index < selectedIndices.length() &&
                            selectedIndices.get(index);
                }

                return false;
            }
        };
        
        this.selectedItemsList = new ReadOnlyUnbackedObservableList<T>() {
            @Override public T get(int i) {
                int pos = selectedIndicesList.get(i);
                if (pos < 0 || pos >= items.size()) return null;
                return items.get(pos);
            }

            @Override public int size() {
                return selectedIndices.cardinality();
            }
        };
        
        
        setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            public ListCell<T> call(ListView<T> listView) {
                return new CheckBoxListCell<T>(new Callback<T, ObservableValue<Boolean>>() {
                    @Override public ObservableValue<Boolean> call(T item) {
                        return itemBooleanMap.get(item);
                    }
                });
            };
        });
        
        final MappingChange.Map<Integer,T> map = new MappingChange.Map<Integer,T>() {
            @Override public T map(Integer f) {
                return items.get(f);
            }
        };
        selectedIndicesList.addListener(new ListChangeListener<Integer>() {
            @Override public void onChanged(final Change<? extends Integer> c) {
                // when the selectedIndices ObservableList changes, we manually call
                // the observers of the selectedItems ObservableList.
                selectedItemsList.callObservers(new MappingChange<Integer,T>(c, map, selectedItemsList));
                c.reset();
            }
        });
        
        
        
        
        // listening
//        selectedIndicesList.addListener(new ListChangeListener<Integer>() {
//            @Override
//            public void onChanged(
//                    javafx.collections.ListChangeListener.Change<? extends Integer> c) {
//                System.out.println(selectedIndicesList);
//            }
//        });
//        selectedItemsList.addListener(new ListChangeListener<T>() {
//            @Override
//            public void onChanged(
//                    javafx.collections.ListChangeListener.Change<? extends T> c) {
////                while (c.next()) {
////                    System.out.println(c);
////                }
//                System.out.println(selectedItemsList);
//            }
//        });
    }
    
    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new ComboBoxListViewSkin<T>(this) {
            // overridden to prevent the popup from disappearing
            @Override protected boolean isHideOnClickEnabled() {
                return false;
            }
        };
    }

    private void updateMap(Change<? extends T> c) {
        if  (c == null) {
            // reset the map
            itemBooleanMap.clear();
            for (int i = 0; i < items.size(); i++) {
                final int index = i;
                final T item = items.get(index);
                
                final BooleanProperty booleanProperty = new SimpleBooleanProperty(item, "selected", false);
                itemBooleanMap.put(item, booleanProperty);
                
                booleanProperty.addListener(new InvalidationListener() {
                    @Override public void invalidated(Observable o) {
                        final int changeIndex = selectedIndicesList.indexOf(index);
                        
                        if (booleanProperty.get()) {
                            selectedIndices.set(index);
                            selectedIndicesList.callObservers(new NonIterableChange.SimpleAddChange<Integer>(changeIndex, changeIndex+1, selectedIndicesList));                            
                        } else {
                            selectedIndices.clear(index);
                            selectedIndicesList.callObservers(new NonIterableChange.SimpleRemovedChange<Integer>(changeIndex, changeIndex+1, index, selectedIndicesList));
                        }
                    }
                });
            }
        } else {
            // TODO
        }
    }
    
}
