package org.controlsfx.control.action;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.scene.image.Image;

public class ActionMap {

	private static Map<String, AnnotatedAction> actions = new HashMap<>();

	private ActionMap() {
		// no-op
	}
	
	public static void register(final Object target) {

		for (final Method method : target.getClass().getDeclaredMethods()) {
			
			// process only methods with no parameters or one parameter of type ActionEvent
			int paramCount = method.getParameterCount();
			if ( paramCount > 1 || (paramCount == 1 && method.getParameterTypes()[0] != ActionEvent.class )){
				continue;
			}
			
			Annotation[] annotations = method.getAnnotationsByType(ActionProxy.class);
			if (annotations.length > 0) {
				ActionProxy annotation = (ActionProxy) annotations[0];
				String id = annotation.id().isEmpty()? method.getName(): annotation.id();
				if ( actions.containsKey(id)) {
					throw new IllegalArgumentException( String.format("Action proxy with key = '%s' already exists", id));
				}
				actions.put(id, new AnnotatedAction( annotation, method, target));
			}
		}

	}
	
	public void unregister(final Object target) {
		if ( target != null ) {
			for ( String key: actions.keySet() ) {
				if ( actions.get(key).getTarget() == target) {
					actions.remove(key);
				}
			}
		}
	}

	public static Action action(String id) {
		return actions.get(id);
	}

	public static Collection<Action> actions(String... ids) {
		List<Action> result = new ArrayList<>();
		for( String id: ids ) {
			if ( id.startsWith("---")) result.add(ActionUtils.ACTION_SEPARATOR);
			Action action = action(id);
			if ( action != null ) result.add(action);
		}
		return result;
	}
	
	
}

class AnnotatedAction extends AbstractAction {

	private Method method;
	private Object target;

	public AnnotatedAction(ActionProxy annotation, Method method, Object target) {
		super(annotation.text());
		
		String imageLocation = annotation.image().trim();
		if ( !imageLocation.isEmpty()) {
			this.setGraphic(new Image(imageLocation));
		}
		
		String longText = annotation.longText().trim();
		if ( !imageLocation.isEmpty()) {
			this.setLongText(longText);
		}
		
		this.method = method;
		this.method.setAccessible(true);
		this.target = target;
	}
	
	public Object getTarget() {
		return target;
	}

	@Override
	public void execute(ActionEvent ae) {
		try {
			int paramCount =  method.getParameterCount(); 
			if ( paramCount == 0 ) {
				method.invoke(target);
			} else if ( paramCount == 1 && method.getParameterTypes()[0] == ActionEvent.class ) {
				method.invoke(target, ae);
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
