package org.openmrs.module.eptssync.web.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.servlet.http.HttpSession;

import org.openmrs.api.AdministrationService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptssync.api.SyncConfigurationService;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @uthor JP Boane <jpboane@gmail.com> on 25/11/2020.
 */

@Controller(SyncConfigController.CONTROLLER_NAME)
public class SyncConfigController {
	public static final String CONTROLLER_NAME = "eptssync.syncConfigurationController";
	public static final String SYNC_CONFIGURATION_INIT_STEP = "/module/eptssync/initConfiguration";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncConfigController.class);

	@SuppressWarnings("unused")
	@Autowired
	private SyncConfigurationService syncConfigurationService;

	@SuppressWarnings("unused")
	@Autowired
	private MessageSourceService messageSourceService;

	@SuppressWarnings("unused")
	@Autowired
	private AdministrationService adminService;

	@RequestMapping(value = SYNC_CONFIGURATION_INIT_STEP, method = RequestMethod.GET)
	public ModelAndView visitTypesHarmonyAnalysis(HttpSession session, @RequestParam("installationType") String installationType) throws IOException {
		ModelAndView modelAndView = new ModelAndView();

		String rootDirectory = session.getServletContext().getRealPath("/");

		String configFileName = installationType.equals("source") ? "source_sync_config.json" : "dest_sync_config.json";

		File config = new File(rootDirectory + FileUtilities.getPathSeparator() + configFileName);

		SyncConfiguration syncConfiguration = null;

		if (config.exists()) {
			syncConfiguration = SyncConfiguration.loadFromFile(config);
		} else {
			rootDirectory = Paths.get(".").normalize().toAbsolutePath().toString();
			
			rootDirectory = session.getServletContext().getRealPath("/");

			
			configFileName = installationType.equals("source") ? "initial_configuration_source_file.json"
					: "initial_configuration_dest_file.json";

			config = new File(rootDirectory + FileUtilities.getPathSeparator() + "resources" + FileUtilities.getPathSeparator() + configFileName);

			syncConfiguration = SyncConfiguration.loadFromFile(config);
		}

		modelAndView.addObject("syncConfiguration", syncConfiguration);

		return modelAndView;
	}

}
