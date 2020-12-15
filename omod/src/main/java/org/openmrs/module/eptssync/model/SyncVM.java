package org.openmrs.module.eptssync.model;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncVM {
	private SyncConfiguration avaliableConfigurations;
	
	private SyncVM(HttpServletRequest request, String installationType) throws IOException, DBException {
	
	}
	
	public SyncConfiguration getAvaliableConfigurations() {
		return avaliableConfigurations;
	}
}
