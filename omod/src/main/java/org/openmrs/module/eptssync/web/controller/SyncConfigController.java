package org.openmrs.module.eptssync.web.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.model.ConfigData;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @uthor JP Boane <jpboane@gmail.com> on 25/11/2020.
 */

@Controller(SyncConfigController.CONTROLLER_NAME)
public class SyncConfigController {
	public static final String CONTROLLER_NAME = "eptssync.syncConfigurationController";
	public static final String SYNC_CONFIGURATION_INIT_STEP = "/module/eptssync/initConfig";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncConfigController.class);

	//@SuppressWarnings("unused")
	//@Autowired
	//private SyncConfigurationService syncConfigurationService;

	@SuppressWarnings("unused")
	@Autowired
	private MessageSourceService messageSourceService;

	@SuppressWarnings("unused")
	@Autowired
	private AdministrationService adminService;

	@RequestMapping(value = "/module/eptssync/initConfig", method = RequestMethod.GET)
	public void config(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	/*@RequestMapping(value = "/module/eptssync/config", method = RequestMethod.GET)
	public void initConfig(HttpServletRequest request, @RequestParam String installationType, @ModelAttribute("syncConfiguration") SyncConfiguration syncConfiguration) throws IOException {
		String rootDirectory = OpenmrsUtil.getApplicationDataDirectory() + FileUtilities.getPathSeparator() + "syncConf";
		
		String configFileName = installationType.equals("source") ? "source_sync_config.json" : "dest_sync_config.json";

		File config = new File(rootDirectory + FileUtilities.getPathSeparator() + configFileName);
	
		if (config.exists()) {
			syncConfiguration = SyncConfiguration.loadFromFile(config);
		} else {
						
			String json = installationType.equals("source") ? ConfigData.generateDefaultSourcetConfig() : ConfigData.generateDefaultDestinationConfig();
			
			config = new File(rootDirectory + FileUtilities.getPathSeparator() + "resources" + FileUtilities.getPathSeparator() + configFileName);

			syncConfiguration = SyncConfiguration.loadFromJSON(json);
		}
		
		syncConfiguration.setClassPath(retrieveClassPath());
	}*/
	
	@RequestMapping(value = "/module/eptssync/config", method = RequestMethod.GET)
	public void initConfig(Model model, HttpServletRequest request, @RequestParam String installationType) throws IOException {
		//ModelAndView modelAndView = new ModelAndView();
		
		String rootDirectory = OpenmrsUtil.getApplicationDataDirectory() + FileUtilities.getPathSeparator();
		
		String configFileName = installationType.equals("source") ? "source_sync_config.json" : "dest_sync_config.json";

		File config = new File(rootDirectory + FileUtilities.getPathSeparator() + "syncConf" + FileUtilities.getPathSeparator() + configFileName);

		SyncConfiguration syncConfiguration = null;

		if (config.exists()) {
			syncConfiguration = SyncConfiguration.loadFromFile(config);
		} else {
						
			String json = installationType.equals("source") ? ConfigData.generateDefaultSourcetConfig() : ConfigData.generateDefaultDestinationConfig();
			
			config = new File(rootDirectory + FileUtilities.getPathSeparator() + "resources" + FileUtilities.getPathSeparator() + configFileName);

			syncConfiguration = SyncConfiguration.loadFromJSON(json);
			syncConfiguration.setSyncRootDirectory(rootDirectory+ FileUtilities.getPathSeparator() + "syncConf");
		
			Properties properties = new Properties();
			
			File openMrsRuntimePropertyFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "openmrs-runtime.properties");
			
			properties.load(FileUtilities.createStreamFromFile(openMrsRuntimePropertyFile));
			
			syncConfiguration.getConnInfo().setConnectionURI(properties.getProperty("connection.url"));
			syncConfiguration.getConnInfo().setDataBaseUserName(properties.getProperty("connection.username"));
			syncConfiguration.getConnInfo().setDataBaseUserPassword(properties.getProperty("connection.password"));
		}
		
		syncConfiguration.setClassPath(retrieveClassPath());
		
		
		model.addAttribute("syncConfiguration", syncConfiguration);
	
		//modelAndView.addObject("syncConfiguration", syncConfiguration);
		//return modelAndView;
	}

	private String retrieveClassPath() {
		String rootDirectory = Paths.get(".").normalize().toAbsolutePath().toString();
		
		File[] allFiles = new File(rootDirectory + FileUtilities.getPathSeparator() + "temp").listFiles();
		
		Arrays.sort(allFiles);
		
		for (int i = allFiles.length - 1; i >= 0; i--) {
			if (allFiles[i].isDirectory() && allFiles[i].getAbsolutePath().contains("openmrs-lib-cache")) {
				File classPath = new File(allFiles[i].getAbsoluteFile() + FileUtilities.getPathSeparator() + "eptssync" + FileUtilities.getPathSeparator() + "lib");
				
				return classPath.getAbsolutePath();
			}
		}
		
		return null;
	}

}
