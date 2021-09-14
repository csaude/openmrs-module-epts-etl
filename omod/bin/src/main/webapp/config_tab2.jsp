
<c:if test="${vm.operationsTabActive}">

	<fieldset>
		<table style="width: 100%">
			<c:if test="${not empty vm.selectedOperation}">
				<tr>
					<td>
						<table style="width: 100%">		
							<tr>
								<td style="width: 25%;">
									<spring:message code="eptssync.config.operation.operationType" />
								</td>
								<td style="width: 100%;">
									<input type="text" name="operationType" value="${vm.selectedOperation.operationType}" size="100" disabled="disabled"/>
								</td> 
							</tr>
							<tr>
								<td>
									<spring:message code="eptssync.config.operation.processingMode" />
								</td>
								<td>
									<input type="text" name="processingMode" value="${vm.selectedOperation.processingMode}" size="100" disabled="disabled"/>
								</td> 
							</tr>
							<tr>
								<td>
									<spring:message code="eptssync.config.operation.maxSupportedEngines" />
								</td>
								<td>
									<spring:bind path="vm.selectedOperation.maxSupportedEngines">
										<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" size="100" />
									</spring:bind>
								</td> 
							</tr>
							<tr>
								<td>
									<spring:message code="eptssync.config.operation.maxRecordPerProcessing" />
								</td>
								<td>
									<spring:bind path="vm.selectedOperation.maxRecordPerProcessing">
										<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" size="100" />
									</spring:bind>
								</td> 
							</tr>	
							<tr>
								<td>
									<spring:message code="eptssync.config.operation.minRecordsPerEngine" />
								</td>
								<td>
									<spring:bind path="vm.selectedOperation.minRecordsPerEngine">
										<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" size="100" />
									</spring:bind>
								</td> 
							</tr>
							<tr>
								<td>
									<spring:message code="eptssync.config.operation.sourceFolders" />
								</td>
								<td>
									<spring:bind path="vm.selectedOperation.sourceFoldersAsString">
										<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" size="100" ${!vm.selectedOperation.sourceFoldersRequired ? 'disabled' : ''}/>
									</spring:bind>
								</td> 
							</tr>
							
							<tr>
								<td>
									<spring:message code="eptssync.config.operation.doIntegrityCheckInTheEnd" />
								</td>
								<td>
									<input type="text" name="doIntegrityCheckInTheEnd" value="${vm.selectedOperation.doIntegrityCheckInTheEnd ? 'Sim' : 'Nao'}" size="100" disabled="disabled"/>
								</td> 
							</tr>
							<tr>
								<td>
									<spring:message code="eptssync.config.operation.disabled" />
								</td>
								<td>
									<input type="text" name="disabled" value="${vm.selectedOperation.disabled ? 'Inativo' : 'Activo'}" size="100" disabled="disabled" />
								</td> 
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<input type="submit" value='<spring:message code="eptssync.config.operation.save"/>' name="saveConfig"/>
						
						<input type="button" value='<spring:message code="eptssync.config.operation.close"/>' name="closeOperation" onclick="window.location='loadOperation.form?operationType='" />
					</td>
				</tr>
			
			</c:if>
			<tr>
				<td>
					<table style="width: 100%">		
						<thead>
							<tr>
								<th>Sel.</th>
								<th><spring:message code="eptssync.config.operation.operationType" /></th>
								<th><spring:message code="eptssync.config.operation.processingMode" /></th>
								<th><spring:message code="eptssync.config.operation.maxSupportedEngines" /></th>
								<th><spring:message code="eptssync.config.operation.maxRecordPerProcessing" /></th>
								<th><spring:message code="eptssync.config.operation.minRecordsPerEngine" /></th>
								<th><spring:message code="eptssync.config.operation.doIntegrityCheckInTheEnd" /></th>
								<th><spring:message code="eptssync.config.operation.disabled" /></th>
								<th><spring:message code="eptssync.config.operation.sourceFolders" /></th>
							</tr>
						</thead>
			
						<tbody>
							<c:forEach items="${vm.syncConfiguration.operationsAsList}" var="item" varStatus="itemsRow">
								<tr>
									<td><input type="radio" name="operationSelectRadio" ${item == vm.selectedOperation ? 'checked' : ''} value="${item.operationType}" onclick="window.location='loadOperation.form?operationType=${item.operationType}'"/></td>
									<td>${item.operationType}</td>
									<td>${item.processingMode}</td>
									<td>${item.maxSupportedEngines}</td>
									<td>${item.maxRecordPerProcessing}</td>
									<td>${item.minRecordsPerEngine}</td>
									<td>${item.doIntegrityCheckInTheEnd ? 'Sim' : 'Nao'}</td>
									<td>${item.disabled ? 'Inactivo' : 'Activo'}</td>
									<td>${item.sourceFoldersAsString}</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</td>
			</tr>
		</table>
	</fieldset>	
</c:if>