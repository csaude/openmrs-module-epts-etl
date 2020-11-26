<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude
	file="${pageContext.request.contextPath}/moduleResources/eptssync/css/eptssync.css" />

<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm"
	redirect="module/eptssync/config.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>
<h2>
	<spring:message code="eptssync.config.status" />
</h2>
<br />
<br />
<c:if test="${not empty summaries}">
	<div id="openmrs_msg">
		<c:forEach var="summary" items="${summaries}">
			<span>${summary}</span>
			<br />
		</c:forEach>
	</div>
	<br />
</c:if>

AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA

<%@ include file="/WEB-INF/template/footer.jsp"%>