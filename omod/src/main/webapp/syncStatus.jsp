<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/epts-etl/css/epts-etl.css" />
<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="module/epts-etl/config.form" />

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
  	window.setInterval(refreshStatus, 15000);

	function refreshStatus(){
		var data = new FormData();

			jQuery.ajax({
		       url: '${pageContext.request.contextPath}/module/epts-etl/refreshStatus.form',
		       type: 'GET',
		       dataType: 'json',
		       processData: false, 
		       contentType: false,
		       success: function(data) {

			    	data.itemsProgressInfo.forEach(function(progressInfo) {
			    		var progressMeter = progressInfo.progressMeter;

			    		var _progress = 'Not Started';
			    		var _value = '0';
			    		var _summary = '-';
			    		var _id = progressMeter != null ? progressMeter.id : 'Uknown';
			    		 
						if (progressMeter != null && progressMeter.status != 'NOT INITIALIZED'){
							_progress 	= '' + progressMeter.progress + '%';
							_value 		= 'width: ' + progressMeter.progress + '%';
							_summary	 =  progressMeter.processed + '/' + progressMeter.total;
						}
					
						if (progressMeter != null && progressMeter.finished && progressMeter.progress == 0) {
							_progress 	= '100%';
							_value 		= 'width: 100%';
							_summary	=  progressMeter.processed + '/' + progressMeter.total;
						}

						var show = '';

							show = 'id: ' + _id;

							show = show + ' [';

							show = show + '_summary:' + _summary;
							show = show + ',_value:' + _value;
							show = show + ',_progress: ' + _progress;

							show = show + ']';


						/*	
						var attrs = 'Before Update:\n';
						var node = $('#'+progressInfo.operationTable+'_progress');

						alert(JSON.stringify(node));

						
					    $.each(node.attributes, function ( index, attribute ) {
					        attrs += attribute.name + ' = ' + attribute.value;
					    } );

					    node = $('#'+progressInfo.operationTable+'_value');
					    alert(node);
						
					    $.each( node.attributes, function ( index, attribute ) {
					        attrs += '\n' + attribute.name + ' = ' + attribute.value;
					    } );

					    node = $('#'+progressInfo.operationTable+'_summary');
					    alert(node);
						
					    $.each( node.attributes, function ( index, attribute ) {
					        attrs += '\n' + attribute.name + ' = ' + attribute.value;
					    } );
							
						alert(attrs);
						*/
						
			    		$('#'+progressInfo.operationTable+'_progress').attr('data-label', _progress);
			    		$('#'+progressInfo.operationTable+'_value').attr('style', _value);
			    		$('#'+progressInfo.operationTable+'_summary').html(_summary);

			    		/*attrs = 'After Update:\n';

					    $.each( $('#'+progressInfo.operationTable+'_progress').attributes, function ( index, attribute ) {
					        attrs += attribute.name + ' = ' + attribute.value;
					    } );

					    $.each( $('#'+progressInfo.operationTable+'_value').attributes, function ( index, attribute ) {
					        attrs += '\n' + attribute.name + ' = ' + attribute.value;
					    } );

					    $.each( $('#'+progressInfo.operationTable+'_summary').attributes, function ( index, attribute ) {
					        attrs += '\n' + attribute.name + ' = ' + attribute.value;
					    } );
							
						alert(attrs);*/
			    	
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
	<spring:message code="epts.etl.config.header" />
</h2>

<br />
<br />
<span> 
	<spring:message code="${openmrs_msg}" text="${syncVm.statusMessage}" />
</span>

<form:form id="syncStatusForm" modelAttribute="syncVm" method="get" action="syncStatus.form">
	 
	<c:forEach items="${syncVm.activeConfiguration.operationsAsList}" var="item" varStatus="itemsRow">
		<input type="button" ${syncVm.isActivatedOperationTab(item) ? 'disabled' : ''} style="height: ${syncVm.isActivatedOperationTab(item) ? '60px' : '55px'}; width: ${syncVm.isActivatedOperationTab(item) ? '125px' : '120px'}" value='<spring:message code="epts.etl.config.operation.${item.operationType}"/>' name="${item.operationType}" onclick="window.location='activeteOperationTab.form?tab=${item.operationType}'" />
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
							<th style="width: 20%"><spring:message code="epts.etl.config.table.tableName"/></th>
							<th style="width: 60%"><spring:message code="epts.etl.sync.progress.bar" /></th>
							<th style="width: 20%"><spring:message code="epts.etl.sync.progress.summary"/></th>
						</tr>
					</thead>
		
					<tbody>
						<c:forEach items="${operation.relatedSyncConfig.tablesConfigurations}" var="item" varStatus="itemsRow">
							<c:set var="progressInfo" value="${syncVm.retrieveProgressInfo(operation, item)}"/>
							<c:set var="progressMeter" value="${progressInfo.progressMeter}"/>
							
							<tr>
								<td>${item.tableName}</td>
								<td>
									<c:set var="_progress" value="Not Started"/>
									<c:set var="_value" value="0"/>
									<c:set var="_summary" value="-"/>
									<c:set var="_id" value="${progressMeter != null ? progressMeter.id : 'Uknown'}"/>
									
									<c:choose>
									<c:when test="${progressMeter != null && progressMeter.finished && progressMeter.progress == 0}">
										<c:set var="_progress" value="100%"/>
										<c:set var="_value" value="width: 100%"/>
										<c:set var="_summary" value="${progressMeter.processed}/${progressMeter.total}"/>
									</c:when>
									<c:when test="${progressMeter != null && progressMeter.status != 'NOT INITIALIZED'}">
										<c:set var="_progress" value="${progressMeter.progress}%"/>
										<c:set var="_value" value="width: ${progressMeter.progress}%"/>
										<c:set var="_summary" value="${progressMeter.processed}/${progressMeter.total}"/>
									</c:when>
									
									</c:choose>				
									
									<div id="${item.tableName}_progress" class="progress" data-label="${_progress}">
									  <span id="${item.tableName}_value" class="value" style="${_value}"></span>
									</div>
								</td>
								<td><label id="${item.tableName}_summary">${_summary}</label></td>
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

