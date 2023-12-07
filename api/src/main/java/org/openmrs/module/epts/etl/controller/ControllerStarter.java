package org.openmrs.module.epts.etl.controller;


public interface ControllerStarter extends Runnable{
	public abstract void finalize(Controller controller);
}
