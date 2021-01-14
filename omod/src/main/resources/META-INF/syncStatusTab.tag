<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt" %>

<%@ attribute name="active" required="true"%>
<%@ attribute name="operation" required="true" type="org.openmrs.module.eptssync.controller.conf.SyncOperationConfig"%>

<c:if test="${active}">
	${operation.operationType}
</c:if>
