<%@ taglib prefix="eptssync" uri="taglibs/eptssync.tld"%>

<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/eptssync/css/eptssync.css" />
<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="module/eptssync/config.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="eptssync.config.header" />
</h2>

<br />
<br />
<span> 
	<spring:message code="${openmrs_msg}" text="${vm.statusMessage}" />
</span>

<form:form id="syncStatusForm" modelAttribute="vm" method="get" action="syncStatus.form">
	<c:forEach items="${vm.activeConfiguration.operationsAsList}" var="item" varStatus="itemsRow">
		<input type="button" ${vm.isActivatedOperationTab(item) ? 'disabled' : ''} style="height: ${vm.isActivatedOperationTab(item) ? '60px' : '55px'}; width: ${vm.isActivatedOperationTab(item) ? '125px' : '120px'}" value='<spring:message code="eptssync.config.operation.${item.operationType}"/>' name="${item.operationType}" onclick="window.location='activeteOperationTab.form?tab=${item.operationType}'" />
	</c:forEach>
	
	<br/>
	
	<c:forEach items="${vm.activeConfiguration.operationsAsList}" var="item" varStatus="itemsRow">
		<eptssync:syncStatusTab operation="${item}" active="${vm.isActivatedOperationTab(item)}"/>
	</c:forEach>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>