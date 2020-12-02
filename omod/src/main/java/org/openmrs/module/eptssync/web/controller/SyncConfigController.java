package org.openmrs.module.eptssync.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.model.ConfVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * @uthor JP Boane <jpboane@gmail.com> on 25/11/2020.
 */

@Controller(SyncConfigController.CONTROLLER_NAME)
@SessionAttributes({ "vm" })
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
	
	@RequestMapping(value = "/module/eptssync/loadOperation", method = RequestMethod.GET)
	public ModelAndView loadOperation(Model model, HttpServletRequest request, @RequestParam String operationType) throws IOException {
		ConfVM vm = (ConfVM) request.getSession().getAttribute("vm");
		
		vm.selectOperation(operationType);
		
		return new ModelAndView("redirect:config.form?installationType=");
	}
	
	@RequestMapping(value = "/module/eptssync/loadTable", method = RequestMethod.GET)
	public ModelAndView loadTable(Model model, HttpServletRequest request, @RequestParam String tableName) throws IOException {
		ConfVM vm = (ConfVM) request.getSession().getAttribute("vm");
		
		vm.selectTable(tableName);
		
		return new ModelAndView("redirect:config.form?installationType=");
	}
	
	@RequestMapping(value = "/module/eptssync/activeteTab", method = RequestMethod.GET)
	public ModelAndView activateTab(Model model, HttpServletRequest request, @RequestParam String tab) throws IOException {
		ConfVM vm = (ConfVM) request.getSession().getAttribute("vm");
		
		vm.activateTab(tab);
		
		return new ModelAndView("redirect:config.form?installationType=");
	}
	
	
	@RequestMapping(value = "/module/eptssync/config", method = RequestMethod.GET)
	public void initConfig(Model model, HttpServletRequest request, @RequestParam String installationType) throws IOException {
		
		if (!installationType.isEmpty()) {
			model.addAttribute("vm", new ConfVM(request, installationType));
		}
	}

	

	@RequestMapping(value = "/module/eptssync/saveOperation", method = RequestMethod.POST)
	public ModelAndView save(@ModelAttribute("vm") final ConfVM vm, final Errors errors) {
		SyncOperationConfig confi = vm.getSelectedOperation();
		
		if (errors.hasErrors()) {
			return new ModelAndView();
		}

		return new ModelAndView("redirect:config.form?installationType=");
	}
}
