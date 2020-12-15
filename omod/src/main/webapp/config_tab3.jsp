
<c:if test="${vm.tablesTabActive}">

	<fieldset>
		<table style="width: 100%">
			<tr style="display: ${not empty vm.selectedTable ? 'block' : 'none'}">
				<td style="width: 100%">
					<table style="width: 100%">		
						<tr>
							<td style="width: 25%;">
								<spring:message code="eptssync.config.table.tableName" />
							</td>
							<td style="width: 75%;" colspan="2">
								<input type="text" name="tableName" value="${vm.selectedTable.tableName}" size="75" readonly="readonly"/>
							</td> 
						</tr>
						<tr>
							<td>
								<spring:message code="eptssync.config.table.metadata" />
							</td>
							<td colspan="2">
								<spring:bind path="vm.selectedTable.metadata">
									<input type="hidden" id="selectedTabletableMetadata" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
								</spring:bind>
								
								<spring:message code="eptssync.config.answer.yes"/><input type="radio" value="true" name="radioMetadatatable" onclick="document.getElementById('selectedTabletableMetadata').value = this.value" ${vm.selectedTable.metadata ? 'checked' : ''}/>
								<spring:message code="eptssync.config.answer.no"/> <input type="radio" value="false" name="radioMetadatatable" onclick="document.getElementById('selectedTabletableMetadata').value = this.value" ${!vm.selectedTable.metadata ? 'checked' : ''}/>
							</td> 
						</tr>
						<tr>
							<td>
								<spring:message code="eptssync.config.table.active" />
							</td>
							<td colspan="2">
								<spring:message code="eptssync.config.answer.yes"/> <input type="radio" value="false"  name="radioDisabledTable" onclick="document.getElementById('${vm.selectedTable.tableName}').value = this.value" ${!vm.selectedTable.disabled ? 'checked' : ''}/>
								<spring:message code="eptssync.config.answer.no"/>  <input type="radio" value="true" name="radioDisabledTable" onclick="document.getElementById('${vm.selectedTable.tableName}').value = this.value" ${vm.selectedTable.disabled ? 'checked' : ''}/>
							</td> 
						</tr>
						<tr>
							<td>
								<spring:message code="eptssync.config.table.removeForbidden" />
							</td>
							<td colspan="2">
								<input type="text" name="doIntegrityCheckInTheEnd" value="${vm.selectedTable.removeForbidden ? 'Nao' : 'Sim'}" size="75" readonly="readonly"/>
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
			<tr style="display: ${not empty vm.selectedTable ? 'block' : 'none'}">
				<td>
					<input type="submit" id="btnSaveTableConf" value='<spring:message code="eptssync.config.table.save"/>' name="saveConfig"/>
					<input type="button" value='<spring:message code="eptssync.config.operation.close"/>' name="closeOperation" onclick="window.location='loadTable.form?tableName='" />
				</td>
			</tr>
			<tr>
				<td>
					<table style="width: 100%">		
						<thead>
							<tr>
								<th>Sel.</th>
								<th><spring:message code="eptssync.config.table.tableName" /></th>
								<th><spring:message code="eptssync.config.table.removeForbidden" /></th>
								<th><spring:message code="eptssync.config.table.parents" /></th>
								<th><spring:message code="eptssync.config.table.active" /></th>
							</tr>
						</thead>
			
						<tbody>
							<c:forEach items="${vm.syncConfiguration.allTables}" var="item" varStatus="itemsRow">
								<tr>
									<td><input type="radio" name="tableSelectRadio" ${item == vm.selectedTable ? 'checked' : ''} value="${item.tableName}" onclick="window.location='loadTable.form?tableName=${item.tableName}'"/></td>
									<td>${item.tableName}</td>
									<td>${item.removeForbidden ? 'Nao' : 'Sim'}</td>
									<td>${not empty item.parentsAsString ? item.parentsAsString : 'Not loaded'}</td>
									
									<td>
										<spring:bind path="vm.syncConfiguration.allTables[${itemsRow.index}].disabled">
											<input type="hidden" id="${item.tableName}" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
										</spring:bind>
										
										<input type="checkbox" ${!item.disabled ? 'checked' : ''} onclick="document.getElementById('${item.tableName}').value = !this.checked;document.getElementById('btnSaveTableConf').click()"/>
									</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</td>
			</tr>
		</table>
	</fieldset>	
</c:if>