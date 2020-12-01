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


<springform:form modelAttribute="syncConfiguration" method="post" action="save">
	<table>
		<tr>
			<td>
				<input type="button" value="<spring:message code="eptssync.config.button.installation"/>" name="initConfig" onclick="window.location='initConfig.form?installationType=' + document.getElementById('installationType').value" />
				<input type="button" value="<spring:message code="eptssync.config.button.operations"/>" name="initConfig" onclick="window.location='initConfig.form?installationType=' + document.getElementById('installationType').value" />
				<input type="button" value="<spring:message code="eptssync.config.button.tables"/>" name="initConfig" onclick="window.location='initConfig.form?installationType=' + document.getElementById('installationType').value" />
			<td>
		</tr>
		<tr>
			<td style="width: 25%;">
				<spring:message code="eptssync.config.installationType.label" />
			</td>
			<td style="width: 100%;">
				<input type="text" name="installationType" value="${syncConfiguration.installationType}" size="100" />
			</td> 
		</tr>
		<tr>
			<td>
				<spring:message code="eptssync.config.syncRootDirectory.label" />
			</td>
			<td>
				<input type="text" name="syncRootDirectory" value="${syncConfiguration.syncRootDirectory}" size="100" />
			</td> 
		</tr>
		<tr>
			<td>
				<spring:message code="eptssync.config.classPath.label" />
			</td>
			<td>
				<input type="text" name="classPath" value="${syncConfiguration.classPath}" size="100" />
			</td> 
		</tr>
		<tr>
			<td>
				<spring:message code="eptssync.config.originAppLocationCode.label" />
			</td>
			<td>
				<input type="text" name="originAppLocationCode" value="${syncConfiguration.originAppLocationCode}" size="100" />
			</td> 
		</tr>	
		<tr>
			<td>
				<spring:message code="eptssync.config.dataBaseUserName.label" />
			</td>
			<td>
				<input type="text" name="dataBaseUserName" value="${syncConfiguration.connInfo.dataBaseUserName}" size="100" />
			</td> 
		</tr>	
		<tr>
			<td>
				<spring:message code="eptssync.config.dataBaseUserPassword.label" />
			</td>
			<td>
				<input type="text" name="dataBaseUserPassword" value="${syncConfiguration.connInfo.dataBaseUserPassword}" size="100" />
			</td> 
		</tr>
		<tr>
			<td>
				<spring:message code="eptssync.config.connectionURI.label" />
			</td>
			<td>
				<input type="text" name="connectionURI" value="${syncConfiguration.connInfo.connectionURI}" size="100" />
			</td> 
		</tr>
		<tr>
			<td>
				<spring:message code="eptssync.config.driveClassName.label" />
			</td>
			<td>
				<input type="text" name="driveClassName" value="${syncConfiguration.connInfo.driveClassName}" size="100" />
			</td> 
		</tr>
	</table>	
</springform:form>

<script>
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>