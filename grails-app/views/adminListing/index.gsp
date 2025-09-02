<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="legacy" />
        <g:set var="entityName" value="${message(code: 'listing.label', default: 'Listing')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#list-listing" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="list-listing" class="content scaffold-list" role="main">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:table collection="${listingList}" controller="adminListing">
                <f:column property="id" title="ID"/>
                <f:column property="directory" title="Directory"/>
                <f:column property="status" title="Status"/>
                <f:column title="Location">
                    <g:link controller="adminLocation" action="show" id="${it.location?.id}">${it.location}</g:link>
                </f:column>
                <f:column title="Date Created">
                    <g:formatDate date="${it.dateCreated?.toDate()}" format="yyyy-MM-dd HH:mm:ss"/>
                </f:column>
                <f:column title="Last Updated">
                    <g:formatDate date="${it.lastUpdated}" format="yyyy-MM-dd HH:mm:ss"/>
                </f:column>
            </f:table>

            <div class="pagination">
                <g:paginate total="${listingCount ?: 0}" />
            </div>
        </div>
    </body>
</html>