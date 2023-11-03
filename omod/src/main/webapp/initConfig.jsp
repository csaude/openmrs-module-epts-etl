<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/epts-etl/css/epts-etl.css" />
<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="module/epts-etl/config.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="epts.etl.config.status" />
</h2>

<br />
<br />


<springform:form modelAttribute="vm" method="GET" action="initConfig.form">
	<table>
		<tr>
			<td>
				<select name="installationType" id="installationType">
					<option value=""><spring:message code="epts.etl.config.installationType.select" /></option>
					<option value="source"><spring:message code="epts.etl.config.installationType.source" /></option>
					<option value="destination"><spring:message code="epts.etl.config.installationType.destination" /></option>
				</select>
				
				<input type="button" value="<spring:message code="epts.etl.config.button.initConfig"/>" name="config" onclick="window.location='config.form?installationType=' + document.getElementById('installationType').value" />
			</td> 
		</tr>
	</table>	
</springform:form>

<script>
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
