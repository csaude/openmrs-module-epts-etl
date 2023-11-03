<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/epts-etl/css/epts.etl.css" />
<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="module/epts-etl/config.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="epts.etl.config.header" />
</h2>

<br />
<br />
<span> 
	<spring:message code="${openmrs_msg}" text="${vm.statusMessage}" />
</span>

<form:form id="configForm" modelAttribute="vm" method="post" action="saveConfig.form">
	<input type="button" ${vm.installationTabActive ? 'disabled' : ''} style="height: ${vm.installationTabActive ? '35px' : '30px'}; width: ${vm.installationTabActive ? '75px' : '75px'}" value='<spring:message code="epts.etl.config.button.installation"/>' name="initConfig" onclick="window.location='activeteTab.form?tab=1'" />
	<input type="button" ${vm.operationsTabActive ? 'disabled' : ''} style="height: ${vm.operationsTabActive ? '35px' : '30px'}; width: ${vm.installationTabActive ? '75px' : '75px'}" value='<spring:message code="epts.etl.config.button.operations"/>' name="initConfig" onclick="window.location='activeteTab.form?tab=2'" />
	<input type="button" ${vm.tablesTabActive ? 'disabled' : ''} style="height: ${vm.tablesTabActive ? '35px' : '30px'}; width: ${vm.installationTabActive ? '75px' : '75px'}" value='<spring:message code="epts.etl.config.button.tables"/>' name="initConfig" onclick="window.location='activeteTab.form?tab=3'" />
	<br/>
	
	<%@ include file="config_tab1.jsp" %>
	<%@ include file="config_tab2.jsp" %>
	<%@ include file="config_tab3.jsp" %>	
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>