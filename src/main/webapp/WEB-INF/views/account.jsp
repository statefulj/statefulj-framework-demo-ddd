<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:url value="/customer" var="customerUrl" />
<html>
	<head>
		<title>Account ${account.accountId.id} Page</title>
	</head>
	<body>
		<div class="row">
			<div class="col-md-offset-1 col-md-10">
				account.id=${account.accountId.id}<br /> <a href="${customerUrl}">back &lt;&lt;</a>
			</div>
		</div>
	</body>
</html>
