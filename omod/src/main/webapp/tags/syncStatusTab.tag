<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt" %>

<%@ attribute name="active" required="true"%>
<%@ attribute name="operation" required="true" type="org.openmrs.module.eptssync.controller.conf.SyncOperationConfig"%>

<c:if test="${active}">
	
	<fieldset>
		<legend>${operation.operationType}</legend>
	
		<table style="width: 100%">		
			<thead>
				<tr>
					<th><spring:message code="eptssync.config.table.tableName"/></th>
					<th><spring:message code="eptssync.sync.progress.bar" /></th>
					<th><spring:message code="eptssync.sync.progress.summary"/></th>
				</tr>
			</thead>

			<tbody>
				<c:forEach items="${operation.relatedSyncConfig.tablesConfigurations}" var="item" varStatus="itemsRow">
					<tr>
						<td>${item.tableName}</td>
						<td></td>
						<td></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</fieldset>
</c:if>
