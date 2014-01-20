package org.controlsfx.control.breadcrumbs;



public class SimpleBreadCrumbModel implements IBreadCrumbModel {

	private final String name;

	//transient private final EventHandlerEx<EventArgsG<IBreadCrumbModel>> OpenEvent = new  EventHandlerEx<EventArgsG<IBreadCrumbModel>>();

	//@Override
	//public IEvent<EventArgsG<IBreadCrumbModel>> getOpenEvent() { return OpenEvent; }

	public SimpleBreadCrumbModel(String name){
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void open() {
		//OpenEvent.fireEvent(this, EventArgsG.build((IBreadCrumbModel)this));
	}
}
