
<c:if test="${vm.installationTabActive}">
	<fieldset>
		<table>		
			<tr>
				<td style="width: 25%;">
					<spring:message code="eptssync.config.installationType.label" />
				</td>
				<td style="width: 100%;">
					<input type="text" name="installationType" value="${vm.syncConfiguration.installationType}" size="100" disabled="disabled"/>
				</td> 
			</tr>
			<tr>
				<td>
					<spring:message code="eptssync.config.syncRootDirectory.label" />
				</td>
				<td>
					<input type="text" name="syncRootDirectory" value="${vm.syncConfiguration.syncRootDirectory}" size="100" />
				</td> 
			</tr>
			<tr>
				<td>
					<spring:message code="eptssync.config.classPath.label" />
				</td>
				<td>
					<input type="text" name="classPath" value="${vm.syncConfiguration.classPath}" size="100" />
				</td> 
			</tr>
			<tr>
				<td>
					<spring:message code="eptssync.config.originAppLocationCode.label" />
				</td>
				<td>
					<input type="text" name="originAppLocationCode" value="${vm.syncConfiguration.originAppLocationCode}" size="100" />
				</td> 
			</tr>	
			<tr>
				<td>
					<spring:message code="eptssync.config.dataBaseUserName.label" />
				</td>
				<td>
					<input type="text" name="dataBaseUserName" value="${vm.syncConfiguration.connInfo.dataBaseUserName}" size="100" />
				</td> 
			</tr>	
			<tr>
				<td>
					<spring:message code="eptssync.config.dataBaseUserPassword.label" />
				</td>
				<td>
					<input type="text" name="dataBaseUserPassword" value="${vm.syncConfiguration.connInfo.dataBaseUserPassword}" size="100" />
				</td> 
			</tr>
			<tr>
				<td>
					<spring:message code="eptssync.config.connectionURI.label" />
				</td>
				<td>
					<input type="text" name="connectionURI" value="${vm.syncConfiguration.connInfo.connectionURI}" size="100" />
				</td> 
			</tr>
			<tr>
				<td>
					<spring:message code="eptssync.config.driveClassName.label" />
				</td>
				<td>
					<input type="text" name="driveClassName" value="${vm.syncConfiguration.connInfo.driveClassName}" size="100" />
				</td> 
			</tr>
		</table>
		<br>		
		<input type="button" value='<spring:message code="eptssync.config.button.save"/>' name="save" onclick="window.location='save.form'" />
	</fieldset>	
</c:if>