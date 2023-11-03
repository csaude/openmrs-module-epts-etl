<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/epts-etl/css/epts-etl.css" />
<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="module/epts-etl/initSync.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<h2>
	<spring:message code="epts.etl.sync.header" />
</h2>

<br />
<br />

Error

<%@ include file="/WEB-INF/template/footer.jsp"%>
