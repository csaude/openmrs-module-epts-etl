package org.openmrs.module.eptssync.model.base;

public class AnonymousVO extends BaseVO {
	private int objectId;
	
	@Override
	public int getObjectId() {
		return objectId;
	}

	@Override
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

}
