<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/eptssync/css/eptssync.css" />
<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="module/eptssync/config.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="eptssync.config.status" />
</h2>

<br />
<br />


<springform:form modelAttribute="vm" method="post" action="save">
	<input type="button" ${vm.installationTabActive ? 'disabled' : ''} style="height: ${vm.installationTabActive ? '35px' : '30px'}; width: ${vm.installationTabActive ? '75px' : '75px'}" value='<spring:message code="eptssync.config.button.installation"/>' name="initConfig" onclick="window.location='activeteTab.form?tab=1'" />
	<input type="button" ${vm.operationsTabActive ? 'disabled' : ''} style="height: ${vm.operationsTabActive ? '35px' : '30px'}; width: ${vm.installationTabActive ? '75px' : '75px'}" value='<spring:message code="eptssync.config.button.operations"/>' name="initConfig" onclick="window.location='activeteTab.form?tab=2'" />
	<input type="button" ${vm.tablesTabActive ? 'disabled' : ''} style="height: ${vm.tablesTabActive ? '35px' : '30px'}; width: ${vm.installationTabActive ? '75px' : '75px'}" value='<spring:message code="eptssync.config.button.tables"/>' name="initConfig" onclick="window.location='activeteTab.form?tab=3'" />
	
	<br/>
		
	<%@ include file="config_tab1.jsp" %>
	<%@ include file="config_tab2.jsp" %>
	<%@ include file="config_tab3.jsp" %>	
</springform:form>

<script>
</script>
