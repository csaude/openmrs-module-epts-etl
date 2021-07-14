<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/eptssync/css/eptssync.css" />
<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="module/eptssync/initSync.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="eptssync.sync.header" />
</h2>

<br />
<br />

<springform:form modelAttribute="syncVm" method="GET" action="syncStatus.form">
	<table>
		<tr>
			<td>
				<select name="selectedConfiguration" id="selectedConfiguration">
					<c:forEach items="${syncVm.avaliableConfigurations}" var="item" varStatus="itemsRow">
						<option value="${item.designation}" ${syncVm.activeConfiguration == item ? 'selected' : ''}>${item.designation}</option>
					</c:forEach>
				</select>
				
				<input type="button" value="<spring:message code="eptssync.sync.button.start"/>" name="startSync" onclick="window.location='startSync.form?selectedConfiguration=' + document.getElementById('selectedConfiguration').value" />
			</td> 
		</tr>
	</table>	
</springform:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
