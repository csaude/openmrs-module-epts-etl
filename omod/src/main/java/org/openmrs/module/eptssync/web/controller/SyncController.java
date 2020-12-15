package org.openmrs.module.eptssync.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.ConfVM;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller(SyncController.CONTROLLER_NAME)
@SessionAttributes({ "vm" })
public class SyncController {
	public static final String CONTROLLER_NAME = "eptssync.syncController";
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncController.class);

	//@SuppressWarnings("unused")
	//@Autowired
	//private SyncConfigurationService syncConfigurationService;

	@SuppressWarnings("unused")
	@Autowired
	private MessageSourceService messageSourceService;

	@SuppressWarnings("unused")
	@Autowired
	private AdministrationService adminService;

	@RequestMapping(value = "/module/eptssync/initSync", method = RequestMethod.GET)
	public void initSync(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	@RequestMapping(value = "/module/eptssync/startSync", method = RequestMethod.GET)
	public void startSync(Model model, HttpServletRequest request, @RequestParam String installationType) throws IOException, DBException {
		
		if (!installationType.isEmpty()) {
			model.addAttribute("vm", ConfVM.getInstance(request, installationType));
		}
	}
	
	@RequestMapping(value = "/module/eptssync/syncInit", method = RequestMethod.GET)
	public void initSync(Model model, HttpServletRequest request, @RequestParam String installationType) throws IOException, DBException {
		
		if (!installationType.isEmpty()) {
			model.addAttribute("vm", ConfVM.getInstance(request, installationType));
		}
	}

	@RequestMapping(value = "/module/eptssync/saveConfig", method = RequestMethod.POST)
	public ModelAndView save(@ModelAttribute("vm") ConfVM vm, Model model) {
		SyncConfiguration confi = vm.getSyncConfiguration();
		confi.refreshTables();
	
		try {
			confi.validate();
			
			vm.save();
		} catch (ForbiddenOperationException e) {
			vm.setStatusMessage(e.getLocalizedMessage());
		}
		
		/*if (errors.hasErrors()) {
			return new ModelAndView();
		}*/

		return new ModelAndView("redirect:config.form?installationType=");
	}
}
