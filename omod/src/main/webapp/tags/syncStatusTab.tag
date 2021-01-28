<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt" %>

<%@ attribute name="active" required="true"%>
<%@ attribute name="operation" required="true" type="org.openmrs.module.eptssync.controller.conf.SyncOperationConfig"%>

<%@ taglib prefix="eptssync" uri="taglibs/eptssync.tld"%>

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

<c:if test="${active}">
	
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
							<c:set var="progressInfo" value="${vm.retrieveProgressInfo(operation, item)}"/>
							<c:set var="progressMeter" value="${progressInfo.progressMeter}"/>
						
							<div class="progress" data-label="${not empty progressMeter  ? progressMeter.progress : 'Not Started'}${not empty progressMeter ? '%' : ''}">
							  <span class="value" style="width:${not empty progressMeter ? progressMeter.progress : 0}%;"></span>
							</div>
						</td>
						<td>${progressInfo.progressMeter.processed}/${progressInfo.progressMeter.total}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</fieldset>
	<input type="button" value="Refresh" name="btnRefreshStatus" onclick="window.location='syncStatus.form?'" />
	<input type="button" value="Refresh" name="btnRefreshStatusAjax" onclick="refreshStatus()" />
</c:if>

<script type="text/javascript">
	//window.setInterval(refreshStatus, 10000);

	function refreshStatus(){
	    /*
		$.ajax({
		       url: './module/eptssync/syncStatus',
		       type: 'GET',
		       data: data,
		       dataType: 'json',
		       processData: false, 
		       contentType: false,
		       success: function(data) {
		    	  alert('Refreshed');
		    		
		    	   return true;
		       },
		       error: function(data, textStatus, jqXHR){
		    	  alert('Error ' + textStatus)
		       }
		    });*/
	}
</script>
