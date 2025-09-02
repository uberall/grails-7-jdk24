<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="legacy" />
        <g:set var="entityName" value="${message(code: 'location.label', default: 'Location')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#show-location" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="show-location" class="content scaffold-show" role="main">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <ol class="property-list location">
                <li class="fieldcontain">
                    <span class="property-label">Name</span>
                    <div class="property-value">${location.name}</div>
                </li>
                <li class="fieldcontain">
                    <span class="property-label">Address</span>
                    <div class="property-value">${location.address}</div>
                </li>
                <li class="fieldcontain">
                    <span class="property-label">Valid</span>
                    <div class="property-value">${location.valid}</div>
                </li>
                <li class="fieldcontain">
                    <span class="property-label">Date Created</span>
                    <div class="property-value"><g:formatDate date="${location.dateCreated}" format="yyyy-MM-dd HH:mm:ss"/></div>
                </li>
                <li class="fieldcontain">
                    <span class="property-label">Last Updated</span>
                    <div class="property-value"><g:formatDate date="${location.lastUpdated?.toDate()}" format="yyyy-MM-dd HH:mm:ss"/></div>
                </li>
            </ol>
            <g:form controller="adminLocation" action="delete" id="${this.location.id}" method="DELETE">
                <fieldset class="buttons">
                    <g:link class="edit" action="edit" controller="adminLocation" id="${this.location.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
                    <input class="delete" type="submit" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
