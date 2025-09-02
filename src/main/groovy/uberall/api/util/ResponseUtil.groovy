package uberall.api.util

import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import uberall.api.ConstraintError
import uberall.api.model.Response
import uberall.utils.StringUtils

@CompileStatic
/**
 * Be aware that *Utils are @CompileStatic for performance reasons.
 * Some groovy magic doesn't work and will fail during runtime!
 *
 * DON'T USE: params.public_key or grailsApplication.config.application.hostname
 * USE: params.get('public_key') or grailsApplication.config.getProperty('application.hostname')
 *
 * DON'T USE: map.each { k, v ->
 * USE: map.each { Map.Entry node ->
 */
class ResponseUtil {

    static Response getInvalidParameter(String name, String msg = "", String error = "") {
        Response response = new Response(status: Response.ResponseStatus.INVALID_PARAMETER, message: "Invalid $name provided. " + msg)
        if (error) {
            Map<String,String> responseMap = [:]
            responseMap.put(name, error)

            response.response = responseMap
        }

        response
    }

    static Response getValidationErrors(GrailsApplication grailsApplication, String objectName, List<FieldError> fieldErrors, String msg = null) {
        ValidationTagLib validationTagLib = (ValidationTagLib) grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ValidationTagLib')
        Response response = new Response(
                status: Response.ResponseStatus.INVALID_PARAMETER,
                message: "Validation errors occur for $objectName" + (msg ? ": $msg" : '')
        )

        Map responseMap = [
                errors: fieldErrors?.collect { FieldError error ->
                    String message = error.defaultMessage.matches(StringUtils.MESSAGE_KEY_FORMAT) ? validationTagLib.message(code: "validation.error.${error.defaultMessage}") : error.defaultMessage
                    return [field: error.field, code: error.code, message: message]
                }
        ]
        response.response = responseMap

        response
    }

    /**
     * Returns a 409 CONFLICT response.
     *
     * @param name the name of the conflicting field
     * @param errorCode the error code that describes why there is a conflict in the first place
     * @param existingFieldName the name of the key for the existing object
     * @param existingObject the existing object itself
     * @return a 409 CONFLICT response
     */
    static Response getConflictParameter(String name, Response.ErrorCode errorCode, String existingFieldName = "",
                                         def existingObject = null) {
        getConflictParameter(errorCode, "Invalid $name provided. Already in use", existingFieldName, existingObject)
    }

    /**
     * Returns a 409 CONFLICT response.
     *
     * @param errorCode the error code that describes why there is a conflict in the first place
     * @param existingFieldName the name of the key for the existing object
     * @param existingObject the existing object itself
     * @return a 409 CONFLICT response
     */
    static Response getConflictParameter(Response.ErrorCode errorCode, String message, String existingFieldName = "",
                                         def existingObject = null) {
        Response response = new Response(status: Response.ResponseStatus.CONFLICT, errorCode: errorCode, message: message)
        if (existingFieldName && existingObject) {
            Map<String,Object> responseMap = [:]
            responseMap.put(existingFieldName, existingObject)

            response.response = responseMap
        }

        response
    }

    static Response getConflictParameterWithDetails(String message, Response.ErrorCode errorCode = null, Map response = [:]) {
        if (errorCode) {
            response.error = errorCode
        }
        return new Response(status: Response.ResponseStatus.CONFLICT, errorCode: errorCode, response: response, message: message)
    }

    static Response getMissingParameter(String parameter, String msg = "") {
        return new Response(
                status: Response.ResponseStatus.MISSING_PARAMETER,
                message: "$parameter missing. " + msg,
                response: [(parameter): Response.ResponseStatus.MISSING_PARAMETER.toString()]
        )
    }

    static Response getWrongParameterType(String parameter, String expected) {
        return new Response(
                status: Response.ResponseStatus.WRONG_PARAMETER_TYPE,
                message: "$parameter is of the wrong type. Expected: $expected"
        )
    }

    static Response getError(String message, Response.ErrorCode errorCode = null, Map response = [:]) {
        if (errorCode) {
            response.error = errorCode
        }

        return new Response(status: Response.ResponseStatus.ERROR, errorCode: errorCode, response: response, message: message)
    }

    static Response getPartialError(String message, Map response = [:]) {
        return new Response(status: Response.ResponseStatus.PARTIAL_ERROR, response: response, message: message)
    }

    static Response getBadRequest(String message, Response.ErrorCode errorCode = Response.ErrorCode.UNKNOWN) {
        return new Response(status: Response.ResponseStatus.BAD_REQUEST, message: message, response: [error: errorCode])
    }

    static Response getServerError(String message, Response.ErrorCode errorCode = Response.ErrorCode.UNKNOWN) {
        return new Response(status: Response.ResponseStatus.SERVER_ERROR, message: message, response: [error: errorCode])
    }

    static Response getForbidden(String message = null, Map response = [:]) {
        Response resultResponse = new Response(status: Response.ResponseStatus.FORBIDDEN, message: message ?: 'You are not allowed to do this.')
        if (response) {
            resultResponse.response = response
        }
        return resultResponse
    }

    static Response getSuccess(def response, String msg = null, List<String> warnings = null) {
        return new Response(status: Response.ResponseStatus.SUCCESS, response: response, message: msg, warnings: warnings)
    }

    static Response getQuotaLimitExceedResponse() {
        return new Response(
                status: Response.ResponseStatus.QUOTA_LIMIT_EXCEED,
                message: 'you have reached your quota limit. contact customer support for further information.'
        )
    }

    static Response getNormalizationFailed() {
        return new Response(
                status: Response.ResponseStatus.INVALID_PARAMETER,
                message: "The given data could not be normalized",
                errorCode: Response.ErrorCode.NORMALIZATION_FAILED,
                response: [error: Response.ErrorCode.NORMALIZATION_FAILED]
        )
    }

    static Response getNotFound(String msg = null) {
        return new Response(
                status: Response.ResponseStatus.NOT_FOUND,
                message: msg ?: "The requested resource was not found",
                response: [:]
        )
    }

    static Response getNotFoundWithDetails(String message, Response.ErrorCode errorCode = null, Map response = [:]) {
        if (errorCode) {
            response.error = errorCode
        }
        return new Response(status: Response.ResponseStatus.NOT_FOUND, errorCode: errorCode, response: response, message: message)
    }

    static Response getErrors(List<DefaultMessageSourceResolvable> errors) {
        List<String> fields = []
        Response response = new Response(status: Response.ResponseStatus.MISSING_PARAMETER)
        Map errorFields = [:]
        errors.each {
            if (it instanceof FieldError && !it.rejectedValue) {
                String error = 'NOT_NULLABLE'
                if (it.code != 'nullable') {
                    response.status = Response.ResponseStatus.INVALID_PARAMETER
                    String code = ConstraintError.getErrorCategory(it.code)
                    error = code
                    response.message = it.defaultMessage
                }
                errorFields.put(it.field, error)
            } else if (it instanceof FieldError) {
                response.status = Response.ResponseStatus.INVALID_PARAMETER
                response.message = it.defaultMessage
                if (it.field) {
                    String code = ConstraintError.getErrorCategory(it.code)
                    errorFields.put(it.field, code)
                }
            } else {
                response.status = Response.ResponseStatus.INVALID_PARAMETER
                fields << it.code
                response.message = it.defaultMessage
            }
        }

        response.response = errorFields
        return response
    }

    static int getStatusCode(Response.ResponseStatus status) {
        switch (status) {
            case Response.ResponseStatus.SUCCESS: return HttpStatus.OK.value()
            case Response.ResponseStatus.FORBIDDEN: return HttpStatus.FORBIDDEN.value()
            case Response.ResponseStatus.QUOTA_LIMIT_EXCEED: return HttpStatus.FORBIDDEN.value()
            case Response.ResponseStatus.NOT_AUTHORIZED: return HttpStatus.UNAUTHORIZED.value()
            case Response.ResponseStatus.BAD_ACCESS_TOKEN: return HttpStatus.UNAUTHORIZED.value()
            case Response.ResponseStatus.BAD_PRIVATE_KEY: return HttpStatus.UNAUTHORIZED.value()
            case Response.ResponseStatus.BAD_PUBLIC_KEY: return HttpStatus.UNAUTHORIZED.value()
            case Response.ResponseStatus.MISSING_PARAMETER: return HttpStatus.BAD_REQUEST.value()
            case Response.ResponseStatus.INVALID_PARAMETER: return HttpStatus.BAD_REQUEST.value()
            case Response.ResponseStatus.WRONG_PARAMETER_TYPE: return HttpStatus.BAD_REQUEST.value()
            case Response.ResponseStatus.CONFLICT: return HttpStatus.CONFLICT.value()
            case Response.ResponseStatus.RESOURCE_LOCKED: return HttpStatus.SERVICE_UNAVAILABLE.value()
            case Response.ResponseStatus.SERVER_ERROR: return HttpStatus.INTERNAL_SERVER_ERROR.value()
            case Response.ResponseStatus.ERROR: return HttpStatus.BAD_REQUEST.value()
            case Response.ResponseStatus.NOT_FOUND: return HttpStatus.NOT_FOUND.value()
            default: return HttpStatus.BAD_REQUEST.value()
        }
    }

}
