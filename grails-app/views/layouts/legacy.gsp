<!doctype html>
<html lang="en" class="no-js">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title><g:layoutTitle default="Grails"/></title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <asset:stylesheet src="application.css"/>
        <style>
            /* Minimal inlined styles to improve legacy look without external deps */
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; margin: 0; color: #222; }
            .legacy-nav { background:#2b2f3a; color:#fff; }
            .legacy-nav .container { display:flex; align-items:center; gap:16px; padding:10px 16px; max-width:1100px; margin:0 auto; }
            .brand { display:flex; align-items:center; gap:8px; font-weight:600; color:#fff; text-decoration:none; }
            .brand img { height:28px; width:auto; display:block; }
            .nav-links { display:flex; gap:12px; margin-left:auto; }
            .nav-links a { color:#cfd3dc; text-decoration:none; padding:6px 8px; border-radius:4px; }
            .nav-links a[aria-current="page"], .nav-links a:hover { color:#fff; background:rgba(255,255,255,0.08); }
            .container-main { max-width:1100px; margin:16px auto; padding:0 16px; }
            .footer { border-top:1px solid #e5e7eb; margin-top:32px; }
            .footer .container { max-width:1100px; margin:0 auto; padding:12px 16px; color:#6b7280; font-size:14px; }
            .flash { padding:10px 12px; border-radius:6px; margin:12px 0; }
            .flash-success { background:#ecfdf5; color:#065f46; border:1px solid #a7f3d0; }
            .flash-error { background:#fef2f2; color:#991b1b; border:1px solid #fecaca; }
        </style>
        <g:layoutHead/>
    </head>
    <body>
        <header class="legacy-nav" role="banner">
            <div class="container">
                <g:link uri="/" class="brand">
                    <asset:image src="grails.svg" alt="Grails"/>
                    <span><g:meta name="app.name" default="Grails App"/></span>
                </g:link>
                <nav class="nav-links" role="navigation" aria-label="Main Navigation">
                    <g:link uri="/" aria-current="${request.forwardURI == '/' ? 'page' : ''}">Home</g:link>
                    <g:link controller="adminLocation" action="index" aria-current="${controllerName=='location' ? 'page' : ''}">Locations</g:link>
                    <g:link controller="adminListing" action="index" aria-current="${controllerName=='listing' ? 'page' : ''}">Listings</g:link>
                </nav>
            </div>
        </header>

        <main class="container-main" role="main">
            <g:if test="${flash.message}">
                <div class="flash flash-success" role="status">${flash.message}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="flash flash-error" role="alert">${flash.error}</div>
            </g:if>
            <g:layoutBody/>
        </main>

        <div class="footer" role="contentinfo">
            <div class="container">
                <span>&copy; <g:formatDate date="${new Date()}" format="yyyy"/> <g:meta name="app.name" default="Grails App"/>.</span>
            </div>
        </div>
        <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
        <asset:javascript src="application.js"/>
    </body>
</html>
