
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
						<spring:bind path="vm.syncConfiguration.syncRootDirectory">
							<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" size="100" disabled="disabled"/>
						</spring:bind>
					</td> 
				</tr>
				<tr>
					<td>
						<spring:message code="eptssync.config.classPath.label" />
					</td>
					<td>
						<spring:bind path="vm.syncConfiguration.classPath">
							<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"  size="100" />
						</spring:bind>
					</td> 
				</tr>
				
				<tr>
					<td>
						<spring:message code="eptssync.config.originAppLocationCode.label" />
					</td>
					<td>	
						<spring:bind path="vm.syncConfiguration.originAppLocationCode">
							<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" size="100" ${vm.syncConfiguration.sourceInstallationType ? '' : 'disabled'}/>
						</spring:bind>
					</td> 
				</tr>	
				
				<tr>
					<td>
						<spring:message code="eptssync.config.dataBaseUserName.label" />
					</td>
					<td>
						<spring:bind path="vm.syncConfiguration.connInfo.dataBaseUserName">
							<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" size="100" />
						</spring:bind>
					</td> 
				</tr>	
				<tr>
					<td>
						<spring:message code="eptssync.config.dataBaseUserPassword.label" />
					</td>
					<td>
						<spring:bind path="vm.syncConfiguration.connInfo.dataBaseUserPassword">
							<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"  size="100" />
						</spring:bind>
					</td> 
				</tr>
				<tr>
					<td>
						<spring:message code="eptssync.config.connectionURI.label" />
					</td>
					<td>
						<spring:bind path="vm.syncConfiguration.connInfo.connectionURI">
							<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"  size="100" />
						</spring:bind>
					</td> 
				</tr>
				<tr>
					<td>
						<spring:message code="eptssync.config.driveClassName.label" />
					</td>
					<td>
						<spring:bind path="vm.syncConfiguration.connInfo.driveClassName">
							<input type="text" name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>" size="100" />
						</spring:bind>
					</td> 
				</tr>
			</table>
			<br>		
			<input type="submit" value='<spring:message code="eptssync.config.button.save"/>' name="saveConfig"/>
		</fieldset>
</c:if>