<%@ taglib prefix="eptssync" uri="taglibs/eptssync.tld"%>
<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/eptssync/css/eptssync.css" />
<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="module/eptssync/config.form" />

<script src="https://code.jquery.com/jquery-1.11.3.min.js"></script>

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<style>
	.progress {
	  height: 1.5em;
	  width: 100%;
	  background-color: #c9c9c9;
	  position: relative;
	  line-height: 10px;
	}
	.progress:before {
	  content: attr(data-label);
	  font-size: 1em;
	  position: absolute;
	  text-align: center;
	  color: #000000;
	  top: 5px;
	  left: 0;
	  right: 0;
	}
	.progress .value {
	  background-color: #4CAF50;
	  display: inline-block;
	  height: 100%;
	}
</style>

<script type="text/javascript">
  	window.setInterval(refreshStatus, 1000);

	function refreshStatus(){
		var data = new FormData();

			jQuery.ajax({
		       url: '${pageContext.request.contextPath}/module/eptssync/refreshStatus.form',
		       type: 'GET',
		       dataType: 'json',
		       processData: false, 
		       contentType: false,
		       success: function(data) {

			    	data.itemsProgressInfo.forEach(function(progressInfo) {
			    		var progressMeter = progressInfo.progressMeter;
			    		var _progress = progressMeter != null  ? ('' + progressMeter.progress + '%') : 'Not Started';
						console.log('_progress ' + _progress);
			    		
						var _value = 'width: ' + (progressMeter != null ? progressMeter.progress : 0) + '%';
						console.log('_value ' + _value);
			    		
						var _summary = progressMeter != null ? ('' + progressMeter.processed + '/' + progressMeter.total) : '-';
						console.log('_summary ' + _summary);
			    			
			    		$('#'+progressInfo.syncTableName+'_progress').attr('data-label', _progress);
			    		$('#'+progressInfo.syncTableName+'_value').attr('style', _value);
			    		$('#'+progressInfo.syncTableName+'_summary').html(_summary);
			    	});
		 	    	  
		    	return true;
		       },
		       error: function(data, textStatus, jqXHR){
		    	  //alert('Error ' + textStatus)
		       }
		    });
	}

</script>

<h2>
	<spring:message code="eptssync.config.header" />
</h2>

<br />
<br />
<span> 
	<spring:message code="${openmrs_msg}" text="${syncVm.statusMessage}" />
</span>

<form:form id="syncStatusForm" modelAttribute="syncVm" method="get" action="syncStatus.form">
	 
	<c:forEach items="${syncVm.activeConfiguration.operationsAsList}" var="item" varStatus="itemsRow">
		<input type="button" ${syncVm.isActivatedOperationTab(item) ? 'disabled' : ''} style="height: ${syncVm.isActivatedOperationTab(item) ? '60px' : '55px'}; width: ${syncVm.isActivatedOperationTab(item) ? '125px' : '120px'}" value='<spring:message code="eptssync.config.operation.${item.operationType}"/>' name="${item.operationType}" onclick="window.location='activeteOperationTab.form?tab=${item.operationType}'" />
	</c:forEach>
	
	<br/>
	
	<c:forEach items="${syncVm.operations}" var="item" varStatus="itemsRow">
		<c:set var="operation" value="${item}"/>
		<c:if test="${syncVm.isActivatedOperationTab(item)}">
			
			<fieldset>
				<legend>${operation.operationType}</legend>
			
				<table style="width: 100%">		
					<thead>
						<tr>
							<th style="width: 20%"><spring:message code="eptssync.config.table.tableName"/></th>
							<th style="width: 60%"><spring:message code="eptssync.sync.progress.bar" /></th>
							<th style="width: 20%"><spring:message code="eptssync.sync.progress.summary"/></th>
						</tr>
					</thead>
		
					<tbody>
						<c:forEach items="${operation.relatedSyncConfig.tablesConfigurations}" var="item" varStatus="itemsRow">
							<tr>
								<td>${item.tableName}</td>
								<td>
									<c:set var="progressInfo" value="${syncVm.retrieveProgressInfo(operation, item)}"/>
									<c:set var="progressMeter" value="${progressInfo.progressMeter}"/>
								
									<div id="${item.tableName}_progress" class="progress" data-label="${not empty progressMeter  ? progressMeter.progress : 'Not Started'}${not empty progressMeter ? '%' : ''}">
									  <span id="${item.tableName}_value" class="value" style="width:${not empty progressMeter ? progressMeter.progress : 0}%;"></span>
									</div>
								</td>
								<td><label id="${item.tableName}_summary">${progressMeter.processed}/${progressMeter.total}</label></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</fieldset>
			<input type="button" value="Refresh" name="btnRefreshStatus" onclick="window.location='syncStatus.form?'" />
			<input type="button" value="Refresh 1" name="btnRefreshStatusAjax" onclick="refreshStatus()" />
		</c:if>
	</c:forEach>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>

