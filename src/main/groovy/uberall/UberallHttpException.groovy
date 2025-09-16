package uberall

import uberall.api.model.Response

/**
 * Exception that is thrown by *ControllerServices.
 * Has a {@link Response} attached which is used to provide feedback for errors that occured during a particular API call.
 */
class UberallHttpException extends RuntimeException {

    Response response

    UberallHttpException(Response response) {
        this.response = response
    }

    String getMessage() {
        "Status:${response.status}, Message:$response.message Response:$response.response"
    }

    @Override
    synchronized Throwable fillInStackTrace() {
        return this
    }

}
