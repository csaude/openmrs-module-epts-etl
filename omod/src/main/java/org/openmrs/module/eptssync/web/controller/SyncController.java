package org.openmrs.module.eptssync.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.AdministrationService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptssync.model.SyncVM;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
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
	public void initSync(ModelMap model) throws DBException, IOException {
		model.addAttribute("vm", SyncVM.getInstance());
	}
	
	@RequestMapping(value = "/module/eptssync/startSync", method = RequestMethod.GET)
	public ModelAndView startSync(Model model, HttpServletRequest request, @RequestParam String selectedConfiguration) throws IOException, DBException {
		SyncVM vm = (SyncVM) request.getSession().getAttribute("vm");
		
		vm.startSync(selectedConfiguration);
		
		return new ModelAndView("redirect:syncStatus.form");
	}
	
	@RequestMapping(value = "/module/eptssync/syncStatus", method = RequestMethod.GET)
	public void showSyncStatus(ModelMap model) throws DBException, IOException {
		System.out.println("");
	}
}
