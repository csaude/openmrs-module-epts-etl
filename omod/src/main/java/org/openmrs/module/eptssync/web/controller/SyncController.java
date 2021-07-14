package org.openmrs.module.eptssync.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.AdministrationService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.eptssync.model.SyncVM;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller(SyncController.CONTROLLER_NAME)
@SessionAttributes({ "syncVm" })
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
	public ModelAndView initSync(ModelMap model, HttpServletRequest request) throws DBException, IOException {
		SyncVM syncVm = (SyncVM) request.getSession().getAttribute("syncVm");
		
		try {
			if (syncVm == null) {
				syncVm = SyncVM.getInstance();
				
				request.getSession().setAttribute("syncVm", syncVm);
			}
			else {
				if (syncVm.getActiveConfiguration().getRelatedController() != null) {
					return new ModelAndView("redirect:syncStatus.form");
				}
			}
		} 
		finally {
			model.addAttribute("syncVm", syncVm);
		}
		
		return new ModelAndView();
	}
	
	@RequestMapping(value = "/module/eptssync/startSync", method = RequestMethod.GET)
	public ModelAndView startSync(Model model, HttpServletRequest request, @RequestParam String selectedConfiguration) throws IOException, DBException {
		SyncVM syncVm = (SyncVM) request.getSession().getAttribute("syncVm");
		
		syncVm.startSync(selectedConfiguration);
		
		return new ModelAndView("redirect:syncStatus.form");
	}
	
	@RequestMapping(value = "/module/eptssync/syncStatus", method = RequestMethod.GET)
	public void showSyncStatus(ModelMap model) throws DBException, IOException {
		System.out.println("");
	}
	
	/*
	@RequestMapping(value = "/module/eptssync/refreshStatus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String refresh(ModelMap model) throws DBException, IOException {
		String var = "response";
		String response = "My JSON Response";
		
		return "{ \"" + var + "\" : \""+ response + "\" }";
	}*/
	

   /*@RequestMapping(value = "/module/eptssync/refreshStatus",  method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map getString() {
        return Collections.singletonMap("response", "Hello World");
    }*/
	   
   @RequestMapping(value="/module/eptssync/refreshStatus", method=RequestMethod.GET)
   @ResponseBody
   public String refreshStatus(@ModelAttribute("syncVm") SyncVM syncVm) {
	 FileUtilities.write("/home/jpboane/working/prg/jee/tmp/response.json", syncVm.getActiveOperationController().getProgressInfo().parseToJSON());
	 
     return syncVm.getActiveOperationController().getProgressInfo().parseToJSON();
   }
   
	@RequestMapping(value = "/module/eptssync/activeteOperationTab", method = RequestMethod.GET)
	public ModelAndView activateTab(Model model, HttpServletRequest request, @RequestParam String tab) throws IOException {
		SyncVM syncVm = (SyncVM) request.getSession().getAttribute("syncVm");
		
		syncVm.activateTab(tab);
		
		return new ModelAndView("redirect:syncStatus.form");
	}
	

}
