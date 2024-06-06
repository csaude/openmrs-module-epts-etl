package org.openmrs.module.epts.etl.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.ConfVM;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
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

/**
 * @uthor JP Boane <jpboane@gmail.com> on 25/11/2020.
 */

@Controller(SyncConfigController.CONTROLLER_NAME)
@SessionAttributes({ "vm" })
public class SyncConfigController {
	public static final String CONTROLLER_NAME = "epts.etl.syncConfigurationController";
	public static final String SYNC_CONFIGURATION_INIT_STEP = "/module/epts/etl/initConfig";

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

	@RequestMapping(value = "/module/epts/etl/initConfig", method = RequestMethod.GET)
	public void initConfig(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	@RequestMapping(value = "/module/epts/etl/loadOperation", method = RequestMethod.GET)
	public ModelAndView loadOperation(Model model, HttpServletRequest request, @RequestParam String operationType) throws IOException {
		ConfVM vm = (ConfVM) request.getSession().getAttribute("vm");
		
		vm.selectOperation(EtlOperationType.valueOf(operationType));
		
		return new ModelAndView("redirect:dstConf.form?installationType=");
	}
	
	@RequestMapping(value = "/module/epts/etl/loadTable", method = RequestMethod.GET)
	public ModelAndView loadTable(Model model, HttpServletRequest request, @RequestParam String tableName) throws IOException {
		ConfVM vm = (ConfVM) request.getSession().getAttribute("vm");
		
		vm.selectTable(tableName);
		
		return new ModelAndView("redirect:dstConf.form?installationType=");
	}
	
	@RequestMapping(value = "/module/epts/etl/activeteTab", method = RequestMethod.GET)
	public ModelAndView activateTab(Model model, HttpServletRequest request, @RequestParam String tab) throws IOException {
		ConfVM vm = (ConfVM) request.getSession().getAttribute("vm");
		
		vm.activateTab(tab);
		
		vm.save();
		
		return new ModelAndView("redirect:dstConf.form?installationType=");
	}
	
	@RequestMapping(value = "/module/epts/etl/dstConf", method = RequestMethod.GET)
	public void config(Model model,@RequestParam String installationType) throws IOException, DBException {
		
		if (!installationType.isEmpty()) {
			model.addAttribute("vm", ConfVM.getInstance(installationType));
		}
	}

	@RequestMapping(value = "/module/epts/etl/saveConfig", method = RequestMethod.POST)
	public ModelAndView save(@ModelAttribute("vm") ConfVM vm, Model model) {
		EtlConfiguration confi = vm.getSyncConfiguration();
		confi.refreshTables();
	
		try {
			confi.validate();
			
			vm.save();
		} catch (ForbiddenOperationException e) {
			vm.setStatusMessage(e.getLocalizedMessage());
		}
	
		return new ModelAndView("redirect:dstConf.form?installationType=");
	}
}
