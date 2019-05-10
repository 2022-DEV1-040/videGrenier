<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE HTML>
<html>
	<jsp:include page="head.jsp"/>
	<body>
		<div id="page">
			<jsp:include page="menu2.jsp"/>
			<div id="fh5co-product">
				<div class="container">
					<div class="row animate-box">
						<div class="col-md-8 col-md-offset-2 text-center">
							<h2><spring:message code="product.products.productToSell"/></h2>
						</div>
					</div>
					<br/>
					<div class="container">
						<div class="col-lg-2 filter text-center">
							<h4><spring:message code="product.products.filter"/></h4>
							<br/>
							<form:form cssClass="form-horizontal" method="get" action="filter" commandName="filterForm" autocomplete="off">
								<form:checkboxes items = "${categories}" path = "categories" delimiter="<br/>"/>
								<br/>
								<spring:message code="common.city"/>
								<form:input type="text" path="city" cssClass=" form-control"/>
								<input type="submit" class="btn btn-primary" value="<spring:message code="product.products.filter"/>">
							</form:form>
						</div>
						<div class="col-lg-10">
							<c:forEach var="product" items="${products}" varStatus="loop">
								<c:if test="${loop.index%3 == 0}">
									<div class="row">
								</c:if>
								<div class="col-md-4 text-center animate-box" style="margin-top: 20px;">
									<a href="<c:url value="/profile?username=${product.seller.username}"/>">
										<div class="fh5co-staff" style="margin-bottom: 5px">
											<img src="data:image/jpg;base64,${product.seller.displayPicture()}" style="width: 70px"
												 alt="<c:url value="/resources/img/userProfile.png"/>">
											<span class="rate">
												<h4 style="display: inline-block">${product.seller.username}</h4>
                                                    <c:forEach begin="1" end="${product.seller.averageRatingSeller}">
														<i class="icon-star2" style="color:yellow"></i>
													</c:forEach>
                                                    <c:forEach begin="${product.seller.averageRatingSeller + 1}" end="5">
														<i class="icon-star"></i>
													</c:forEach>
                                                    <p style="display: inline-block">${product.seller.commentByBuyer.size()}</p>
                                                </span>
										</div>
									</a>
									<a href="<c:url value="/product/details?id=${product.id}"/>" style="margin-top: 100px">
										<h3>${product.name}</h3>
										<div class="product">
											<img src="data:image/jpg;base64,${product.displayPicture()}">
										</div>
										<div class="desc" style="margin-top: 10px;">
											<c:if test="${not product.auction}">
												<h4 style="color: #d1c286;"><spring:message code="common.directSale"/></h4>
												<span class="price">
													<spring:message code="common.price"/>
													${product.price}&euro;
												</span>
											</c:if>
											<c:if test="${product.auction}">
												<h4 style="color: #d1c286"><spring:message code="common.bid"/></h4>
												<span class="price">
													<spring:message code="product.details.actualPrice"/>
													${product.price}&euro;
												</span>
												<br/>
												<span class="price">
													<spring:message code="product.details.endDate"/>
													<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${product.endDate}"/>
												</span>
											</c:if>
											<br/>
											<span class="price">
													<spring:message code="common.city"/>
													${product.seller.city.country}, ${product.seller.city.name}
											</span>
											<br/>
											<span class="price">
													<spring:message code="product.products.added"/>
													<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${product.creationDate}"/>
											</span>
										</div>
									</a>
								</div>
								<c:if test="${loop.index%3 == 2}">
									</div>
								</c:if>
							</c:forEach>
							<c:if test="${products.size()%3 != 0}">
								</div>
							</c:if>
						</div>
					</div>
				</div>
				<jsp:include page="footer2.jsp"/>
			</div>
	</body>
</html>

