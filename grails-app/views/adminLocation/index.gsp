<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="legacy" />
        <g:set var="entityName" value="${message(code: 'location.label', default: 'Location')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#list-location" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="list-location" class="content scaffold-list" role="main">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:table collection="${locationList}" controller="adminLocation">
                <f:column property="id" title="ID"/>
                <f:column property="name" title="Name"/>
                <f:column property="address" title="Address"/>
                <f:column property="valid" title="Valid"/>
                <f:column title="Listings">
                    <g:if test="${it.listings}">
                        <g:each in="${it.listings}" var="listing" status="i">
                            <g:link controller="adminListing" action="show" id="${listing.id}">${listing.directory}</g:link><g:if test="${i < it.listings.size() - 1}">, </g:if>
                        </g:each>
                    </g:if>
                </f:column>
                <f:column title="Date Created">
                    <g:formatDate date="${it.dateCreated?.toDate()}" format="yyyy-MM-dd HH:mm:ss"/>
                </f:column>
                <f:column title="Last Updated">
                    <g:formatDate date="${it.lastUpdated}" format="yyyy-MM-dd HH:mm:ss"/>
                </f:column>
            </f:table>

            <div class="pagination">
                <g:paginate total="${locationCount ?: 0}" />
            </div>
        </div>
    </body>
</html>