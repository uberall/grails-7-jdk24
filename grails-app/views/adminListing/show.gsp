<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="legacy" />
        <g:set var="entityName" value="${message(code: 'listing.label', default: 'Listing')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#show-listing" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="show-listing" class="content scaffold-show" role="main">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <ol class="property-list adminListing">
            <li class="fieldcontain">
                <span class="property-label">Directory</span>
                <div class="property-value">${listing.directory}</div>
            </li>
            <li class="fieldcontain">
                <span class="property-label">Status</span>
                <div class="property-value">${listing.status}</div>
            </li>
            <li class="fieldcontain">
                <span class="property-label">Location</span>
                <div class="property-value">${listing.location}</div>
            </li>
            <li class="fieldcontain">
                <span class="property-label">Date Created</span>
                <div class="property-value"><g:formatDate date="${listing.dateCreated?.toDate()}" format="yyyy-MM-dd HH:mm:ss"/></div>
            </li>
            <li class="fieldcontain">
                <span class="property-label">Last Updated</span>
                <div class="property-value"><g:formatDate date="${listing.lastUpdated}" format="yyyy-MM-dd HH:mm:ss"/></div>
            </li>
        </ol>
            <g:form controller="adminListing" action="delete" id="${this.listing.id}" method="DELETE">
                <fieldset class="buttons">
                    <g:link class="edit" action="edit" controller="adminListing" id="${this.listing.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
                    <input class="delete" type="submit" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>

