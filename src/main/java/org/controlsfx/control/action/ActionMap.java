package org.controlsfx.control.action;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.scene.image.Image;

public class ActionMap {

	private Map<String, org.controlsfx.control.action.Action> actions = new HashMap<>();

	public ActionMap(final Object target) {

		for (final Method method : target.getClass().getDeclaredMethods()) {
			
			// process only methods with no parameters or one parameter of type ActionEvent
			switch (method.getParameterCount()) {
			    case 0: break;
			    case 1: if ( method.getParameterTypes()[0] == ActionEvent.class) break; 
			    default: continue;
			}
			
			Annotation[] annotations = method.getAnnotationsByType(ActionProxy.class);
			if (annotations.length > 0) {
				ActionProxy annotation = (ActionProxy) annotations[0];
				String id = annotation.id().isEmpty()? method.getName(): annotation.id();
				actions.put(id, new AnnotatedAction( annotation, method, target));
			}
		}

	}

	public org.controlsfx.control.action.Action get(String id) {
		return actions.get(id);
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
		
		this.method = method;
		this.method.setAccessible(true);
		this.target = target;
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
