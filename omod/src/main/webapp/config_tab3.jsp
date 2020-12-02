
<c:if test="${vm.tablesTabActive}">

	<fieldset>
		<table style="width: 100%">
			<c:if test="${not empty vm.selectedTable}">
				<tr>
					<td>
						<table style="width: 100%">		
							<tr>
								<td style="width: 25%;">
									<spring:message code="eptssync.config.table.tableName" />
								</td>
								<td style="width: 100%;" colspan="2">
									<input type="text" name="tableName" value="${vm.selectedTable.tableName}" size="100" readonly="readonly"/>
								</td> 
							</tr>
								
							<tr>
								<td>
									<spring:message code="eptssync.config.table.removeForbidden" />
								</td>
								<td colspan="2">
									<input type="text" name="doIntegrityCheckInTheEnd" value="${vm.selectedTable.removeForbidden ? 'Nao' : 'Sim'}" size="100" readonly="readonly"/>
								</td> 
							</tr>
							
							<tr>
								<td rowspan="${vm.selectedTable.parents.size()}">
									<spring:message code="eptssync.config.table.parents" />
								</td>
								<td><spring:message code="eptssync.config.table.tableName" /><input type="text" name="tableName" value="${vm.selectedTable.parents.get(0).tableName}" size="50" readonly="readonly"/></td>
								<td><spring:message code="eptssync.config.table.parent.defaultValueDueInconsistency" /><input type="text" name="tableName" value="${vm.selectedTable.parents.get(0).defaultValueDueInconsistency}" size="50" readonly="readonly"/></td>
							</tr>
												
							<c:forEach items="${vm.selectedTable.parents}" var="item" varStatus="itemsRow">
								<c:if test="${item != vm.selectedTable.parents.get(0)}">
									<tr>
										<td><spring:message code="eptssync.config.table.tableName" /><input type="text" name="tableName" value="${item.tableName}" size="50" readonly="readonly"/></td>
										<td><spring:message code="eptssync.config.table.parent.defaultValueDueInconsistency" /><input type="text" name="tableName" value="${item.defaultValueDueInconsistency}" size="50" readonly="readonly"/></td>
									</tr>
								</c:if>
							</c:forEach>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<input type="button" value='<spring:message code="eptssync.config.operation.save"/>' name="saveOperation" onclick="window.location='saveTable.form'" />
						<input type="button" value='<spring:message code="eptssync.config.operation.close"/>' name="closeOperation" onclick="window.location='loadTable.form?tableName='" />
					</td>
				</tr>
			
			</c:if>
			<tr>
				<td>
					<table style="width: 100%">		
						<thead>
							<tr>
								<th>Sel.</th>
								<th><spring:message code="eptssync.config.table.tableName" /></th>
								<th><spring:message code="eptssync.config.table.removeForbidden" /></th>
								<th><spring:message code="eptssync.config.table.parents" /></th>
							</tr>
						</thead>
			
						<tbody>
							<c:forEach items="${vm.syncConfiguration.tablesConfigurations}" var="item" varStatus="itemsRow">
								<tr>
									<td><input type="radio" name="tableSelectRadio" ${item == vm.selectedTable ? 'checked' : ''} value="${item.tableName}" onclick="window.location='loadTable.form?tableName=${item.tableName}'"/></td>
									<td>${item.tableName}</td>
									<td>${item.removeForbidden ? 'Nao' : 'Sim'}</td>
									<td>${item.parentsAsString}</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</td>
			</tr>
		</table>
	</fieldset>	
</c:if>