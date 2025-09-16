package uberall.api.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * The Object returned by all API calls on the uberall Sales Partner API.
 * It always has a status which determines whether this response indicates a successful API call.
 * Furthermore it can hold some additional information what was wrong with the request or what went wrong on the server.
 * Finally on successful API calls it always holds a payload.
 */
@Schema(description = 'Formatted Uberall Response')
class Response {

    /**
     * status returned indicating what happened
     */
    @Schema
    ResponseStatus status

    /**
     * Optional
     * can be used for further output e.g. if error == MISSING_PARAMETER -> number of parameters expected
     */
    @Schema(description = '(optional) Holds further information about the response')
    String message

    @Schema
    ErrorCode errorCode

    /**
     * Optional
     * Can be used to return some additional "warning" messages, even though the request is succeeding.
     * E.g.: missing permission on a Facebook token that doesn't block the current request.
     */
    @Schema(description = '(optional) Holds further warnings')
    List<String> warnings

    /**
     * Required IF status == OK
     * The payload e.g. the requested Object...
     */
    @Schema(description = 'The actual response object of the response, optional for non 200 responses')
    def response = [:]

    static enum ResponseStatus {

        /**
         * Indicates that the request was successful
         */
        SUCCESS,

        /**
         * indicates that the quota limit was reached
         */
        QUOTA_LIMIT_EXCEED,

        /**
         * Indicates that the logged in user is not authorized to do what he tries to
         */
        NOT_AUTHORIZED,

        /**
         * Indicates that the client is not allowed to do what he tried to
         */
        FORBIDDEN,

        /**
         * Indicates that the given access token is invalid (session timed out or else)
         */
        BAD_ACCESS_TOKEN,

        /**
         * Indicates that the given private key is invalid
         */
        BAD_PRIVATE_KEY,

        /**
         * Indicates that the given public key is invalid
         */
        BAD_PUBLIC_KEY,

        /**
         * Indicates that a parameter was missing from the Request
         */
        MISSING_PARAMETER,

        /**
         * Indicates that a parameter is invalid
         */
        INVALID_PARAMETER,

        /**
         * Indicates that a parameter is of the wrong type
         */
        WRONG_PARAMETER_TYPE,

        /**
         * Indicates a Conflict (e.g. duplicate identifier or concurrent access on one of the involved domain objects)
         */
        CONFLICT,

        /**
         * Indicates a concurrency issue on an object
         */
        RESOURCE_LOCKED,

        /**
         * Indicates that something went wrong on the Server. This should clearly be a bug and you should fill a bug report for that
         */
        SERVER_ERROR,

        /**
         * Indicates an error due to the user's request. The application will have handled it and it's not a bug.
         */
        ERROR,

        /**
         * Indicates that the client requested a resources that we do not know
         */
        NOT_FOUND,

        /**
         * Indicates that the client requested a invalid request
         */
        BAD_REQUEST,

        /**
         * Indicates that the user requested a invalid request
         */
        USER_ERROR,

        /**
         * Indicates that were some success and errors
         */
        PARTIAL_ERROR
    }

    static enum ErrorCode {

        /**
         * Indicates that something could not be normalized
         */
        NORMALIZATION_FAILED,

        /**
         * Indicates that some data used was corrupted in any way ( e.g. SearchData lost it's reference data (should never happen))
         */
        DATA_CORRUPTED,

        /**
         * Indicates that the data provided as input is not corrupted, but is not valid either (e.g.: user uploaded a CSV containing locations without a street)
         */
        INVALID_INPUT,

        /**
         * Indicates that a location is not available for sync
         */
        NOT_SYNCABLE,

        /**
         * Indicates that payment has failed, e.g. payone did not accept the payment data
         */
        PAYMENT_FAILED,

        /**
         * Indicates that the sales partner has a free tier and the creation of a new location is not possible
         */
        FREE_TIER_REACHED,

        /**
         * User limit for a feature like ADVANCED_ANALYTICS_2_0 reached
         */
        LIMIT_REACHED,

        /**
         * Indicates that the requested resource is currently not Active (e.g. a user that has no active locations but itself is not inactive...)
         */
        INACTIVE,

        /**
         * Indicated that something went wrong but we don't want the USER to know what (this should be used with when a "real" exception occurs)
         */
        UNKNOWN,

        /**
         * Indicates that the passed unique identifier is already in use
         */
        IDENTIFIER_NOT_UNIQUE,

        /**
         * Google Account has been connected for business, but the matching has not happened yet
         */
        ACCOUNT_WAITING_FOR_AUTO_PAGE_SELECT,

        /**
         * e.g. in ApiGoogleController when there is no Google DirectoryUserAccount
         */
        NO_ACCOUNT_CONNECTED,

        /**
         * e.g. in ApiGoogleController when there is no GMP profile selected
         */
        NO_PAGE_SELECTED,
        /**
         * e.g. in ApiGoogleController when the GMB page is still in creation (should really never happen as we synchronously create pages that's why this is an error)
         */
        PAGE_NOT_CREATED,
        /**
         * e.g. in ApiGoogleController when the listing is in needs review
         */
        PAGE_IN_REVIEW,
        /**
         * e.g. in ApiGoogleController when the GMB page is already claimed by someone,
         * and the user has to connect if he is admin, or request the admin rights with the `requestAdminRightsUrl`.
         */
        PAGE_CLAIMED_BY_OTHERS,
        /**
         * e.g. in ApiSocialPostController when the proposed target overlaps an existing one
         */
        OVERLAPPING_SOCIALPOST,
        /**
         * Indicates that the directories threshold for replies to reviews has been reached.
         */
        TOO_MANY_REPLIES,
        /**
         * Indicates that the reply exceed the directory limitation.
         */
        REPLY_TOO_LONG,
        /**
         * Indicates that the action requested is deprecated/no longer supported
         */
        DEPRECATED

    }

}
