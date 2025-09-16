package uberall.api

import uberall.utils.StringUtils

/**
 * This enum is a wrapper between the currently used API return codes and the 'new' error codes used for the internal
 * batch upload endpoint. Each enum contains an `errorCode` which will be used for batch API responses.
 * For the regular API responses, we lookup the code in the map.
 * @see uberall.api.ApiBatchController
 */
enum ConstraintError {
    BUSINESS_NOT_CREATED("business.not.created"),
    CANNOT_COEXIST("cannot.coexist"),
    CONTAINS_CARRIAGE_RETURN("contains.carriageReturn"),
    CONTAINS_COMMA("contains.comma"),
    CONTAINS_COMPARATOR("contains.comparator"),
    CONTAINS_EMAIL("contains.email"),
    CONTAINS_HASHTAG("contains.hashtag"),
    CONTAINS_PHONE("contains.phone"),
    CONTAINS_SPACE("contains.space"),
    CONTAINS_SPECIAL_CHAR("contains.specialChar"),
    CONTAINS_URL("contains.url"),
    DUPLICATE("duplicate"),
    EMPTY("empty"),
    EQUAL_CELLPHONE("equal.cellphone"),
    EQUAL_PHONE("equal.phone"),
    FORBIDDEN("forbidden"),
    INVALID("invalid"),
    INVALID_STATUS_TRANSITION("stateTransitionInvalid"),
    INVALID_TIME_PERIOD("timePeriod"),
    INVALID_TIME_PERIOD_FRIDAY("timePeriod.Friday"),
    INVALID_TIME_PERIOD_MONDAY("timePeriod.Monday"),
    INVALID_TIME_PERIOD_SATURDAY("timePeriod.Saturday"),
    INVALID_TIME_PERIOD_SUNDAY("timePeriod.Sunday"),
    INVALID_TIME_PERIOD_THURSDAY("timePeriod.Thursday"),
    INVALID_TIME_PERIOD_TUESDAY("timePeriod.Tuesday"),
    INVALID_TIME_PERIOD_WEDNESDAY("timePeriod.Wednesday"),
    IS_BLACKLISTED("blacklisted"),
    MISMATCH_BUSINESS_PRODUCT_PLAN("mismatch.businessProductPlan"),
    MISSING("missing"),
    MISSING_TITLE("missing.title"),
    MISSING_CANCELLATION_DATE("missing.cancellationDate"),
    MISSING_PRODUCT_PLAN_FEATURE("missingProductPlanFeature"),
    MISSING_WEBSITE("missingWebsite"),
    MISSING_PHONE("missingPhone"),
    MORE_HOURS_WITHOUT_OPENING_HOURS('standaloneMoreHours'),
    NOT_CONFIRMED_BUSINESS("notConfirmed.business"),
    NOT_FOUND("notFound"),
    NOT_UNIQUE("notUnique"),
    NOT_SUPPORTED_COUNTRY("notSupported.country"),
    PARSING_FAILED("parsingFailed"),
    PHONE_REPEAT("phoneRepeat"),
    RETIRED_CATEGORY('retired.category'),
    SPECIAL_OPENING_HOURS_WITHOUT_OPENING_HOURS('standaloneSpecialOpeningHours'),
    TOO_BIG("toobig"),
    TOO_LONG("toolong"),
    TOO_MANY("toomany"),
    TOO_MANY_TIME_PERIODS("timePeriods.toomany"),
    TOO_SHORT("tooshort"),
    TOO_SMALL("toosmall"),
    TYPE_MISMATCH("typeMismatch"),
    UNSELECTABLE_CATEGORY("category.unselectable"),
    VALIDATION_FAILED("validationFailed"),
    VERIFICATION_TEMPORARILY_UNAVAILABLE("verificationTemporarilyUnavailable"),
    CONTRACT_VIOLATION("contractViolation"),
    UNSUPPORTED_VALUE("unsupportedValue"),

    final String errorCode
    private static final Map ERROR_MAP = [
            (VERIFICATION_TEMPORARILY_UNAVAILABLE): "VERIFICATION_TEMPORARILY_UNAVAILABLE",
            (FORBIDDEN)                           : "FORBIDDEN",
            (INVALID_STATUS_TRANSITION)           : "INVALID_STATUS_TRANSITION",
            (MISSING)                             : "MISSING_PARAMETER",
            (MISSING_CANCELLATION_DATE)           : "MISSING_PARAMETER",
            (MISSING_PRODUCT_PLAN_FEATURE)        : "MISSING_PARAMETER",
            (MISSING_WEBSITE)                     : "MISSING_PARAMETER",
            (MISSING_PHONE)                       : "MISSING_PARAMETER",
    ]

    ConstraintError(String errorCode) {
        this.errorCode = errorCode
    }

    static String getErrorCode(String code) {
        ConstraintError currentConstraintError = StringUtils.getEnumValue(ConstraintError, code)
        if (currentConstraintError) {
            currentConstraintError.errorCode
        } else {
            code
        }
    }

    static String getErrorCategory(String code) {
        ConstraintError currentConstraintError = StringUtils.getEnumValue(ConstraintError, code)
        if (currentConstraintError) {
            ERROR_MAP.get(currentConstraintError) ?: 'INVALID_PARAMETER'
        } else {
            code
        }
    }

}
