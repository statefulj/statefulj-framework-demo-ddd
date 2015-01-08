<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:url var="createSavingsAccountUrl" value="/accounts/savings" />
<c:url var="createCheckingAccountUrl" value="/accounts/checking" />
<c:url var="createLoanAccountUrl" value="/accounts/loan" />
<c:url var="deleteCustomerUrl" value="/customer/delete" />
<html>
	<head>
		<title>Customer Page</title>
	</head>
	<body>
		<script>
			$(document).ready(function() {

				var token = $("meta[name='_csrf']").attr("content");
				var header = $("meta[name='_csrf_header']").attr("content");
				$(document).ajaxSend(function(e, xhr, options) {
				  xhr.setRequestHeader(header, token);
				});
				  
		        $('.alert-dismissable button.close').click(function() {
					$.ajax({
						url : '/ajax/notifications/' + $(this).attr('notification'),
						type : 'DELETE'
					});
				});
			});
		</script>
		<div class="customer-header row">
			<div class="col-md-offset-1 col-md-10">
				<h2 class="pull-left">${customer.contactInfo.name.firstName} ${customer.contactInfo.name.lastName}</h2>
				<a href="${deleteCustomerUrl}" class="pull-right btn btn-default">Delete Customer</a>
			</div>
		</div>
		<div class="row">
			<div class="col-md-offset-1 col-md-10">
				<c:if test="${not empty notifications}">
					<div class="notifications">
						<c:forEach items="${notifications}" var="notification">
							<c:if test="${notification.type.equals('rejected')}">
								<c:set var="alertType" value="alert-warning"/>
							</c:if>
							<c:if test="${not notification.type.equals('rejected')}">
								<c:set var="alertType" value="alert-info"/>
							</c:if>
							<div class="alert alert-dismissable ${alertType}">
								<button notification="${notification.notificationId.id}" type="button" class="close" data-dismiss="alert">x</button>
								${notification.message}
							</div>
						</c:forEach>
					</div>
				</c:if>
				<h3>Accounts:</h3>
				<c:if test="${empty accounts}">
					<div class="alert alert-info">
						You need to set up some accounts
					</div>
				</c:if>
				<c:if test="${not empty accounts}">
					<table class="table table-striped table-hover ">
						<tr>
							<th>Type</th>
							<th>State</th>
							<th>Amount</th>
							<th></th>
						</tr>
						<c:forEach items="${accounts}" var="account">
						<c:url var="showAccountUrl" value="/accounts/${account.accountId.id}" />
						<tr>
							<td>${account.type}</td>
							<td>${account.state}</td>
							<td>${account.amount}</td>
							<td><a href="${showAccountUrl}">Details</a></td>
						</tr>
						</c:forEach>
					</table>
				</c:if>
				<div class="account-create">
					<a href="${createSavingsAccountUrl}" class="btn btn-default">+ Savings Account</a>
					<a href="${createCheckingAccountUrl}" class="btn btn-default">+ Checking Account</a>
					<a href="${createLoanAccountUrl}" class="btn btn-default">+ Loan Account</a>
				</div>
			</div>
		</div>
	</body>
</html>
