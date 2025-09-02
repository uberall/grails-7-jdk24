package uberall.api

//import com.uberall.commons.listing.ListingDto
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.commons.lang3.RandomStringUtils
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.core.util.StopWatch
import org.grails.datastore.gorm.GormValidateable
import org.hibernate.StaleStateException
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
//import uberall.Business
//import uberall.Country
//import uberall.FeatureService
import uberall.UberallHttpException
//import uberall.UserAccessService
import uberall.UserService
//import uberall.admin.Admin
import uberall.api.model.Response
import uberall.api.util.ResponseUtil
//import uberall.authentication.AdminAuthenticationService
//import uberall.authentication.AuthenticationService
//import uberall.conf.ConfigurationService
import uberall.listing.FileExtension
//import uberall.listing.Listing
//import uberall.listing.Location
//import uberall.listing.LocationStatus
//import uberall.listing.SearchData
//import uberall.listing.dto.ListingDtoDataService
//import uberall.location.LocationGroup
//import uberall.offer.Feature
//import uberall.offer.ProductPlan
//import uberall.salesPartner.SalesPartner
//import uberall.user.ApiAccessToken
//import uberall.user.BusinessStatus
//import uberall.user.User
//import uberall.user.UserRole
//import uberall.user.UserStatus
import uberall.utils.StringUtils

import javax.imageio.IIOException
import java.nio.file.Files
import java.util.regex.Pattern

abstract class AbstractApiController {

    //AuthenticationService authenticationService
    //AdminAuthenticationService adminAuthenticationService
    //ConfigurationService configurationService
    //FeatureService featureService
    //ListingDtoDataService listingDtoDataService
    //UserAccessService userAccessService
    //UserService userService

    private static final Pattern BOOLEAN_PATTERN = Pattern.compile(/^true$|^false$/)

    /**
     * Renders the given {@link Response} as JSON.
     *
     * @param resp the response to render
     */
    protected void renderJson(Response resp) {
        response.status = ResponseUtil.getStatusCode(resp.status)
        JSON json = resp as JSON
        render(contentType: 'application/json', text: json?.toString())
    }

    /**
     * Renders the given File
     */
    protected void renderFile(File file, String filename, String contentType) {
        response.setHeader('Content-disposition', "attachment; filename=$filename")
        response.contentType = contentType
        response.outputStream << file.bytes
    }

    protected void renderSuccess(Object returnObject, String message = null, List<String> warnings = null) {
        renderJson(ResponseUtil.getSuccess(returnObject, message, warnings))
    }

    /**
     * Renders a 400 response as JSON
     */
    protected void renderBadRequest(String message, Response.ErrorCode errorCode = Response.ErrorCode.INVALID_INPUT) {
        renderJson(ResponseUtil.getBadRequest(message, errorCode))
    }

    /**
     * Renders a forbidden response as JSON.
     */
    protected void renderForbidden(String msg = null) {
        renderJson(ResponseUtil.getForbidden(msg))
    }

    protected void renderForbiddenListOfMessages(String message, Map response = [:]) {
        renderJson(ResponseUtil.getForbidden(message, response))
    }

    protected void renderNotFound(String msg = null) {
        renderJson(ResponseUtil.getNotFound(msg))
    }

    //protected void renderForbiddenBusiness(Business business) {
    //    renderJson(ResponseUtil.getForbidden("You're not allowed to access Business#${business.id}"))
    //}

    // protected User getUser() {
    //     authenticationService.getCurrentUser()
    // }

    // protected Admin getAdmin() {
    //     adminAuthenticationService.currentUser
    // }

    /**
     * Max must be between 0..10_000, otherwise will be set to configurable default (50)
     * Offset must be positive Number, otherwise will be set to 0
     * @return Map with 'max' and 'offset'
     */
    protected Map<String, Integer> getListParams() {
        Integer max = params.getInt("max")
        if (max == null || max < 0) {
            max = configurationService.getInt('uberall.defaults.pageSize', 50)
        }
        if (max > 10_000) {
            max = 10
        }
        int offset = Math.max(params.getInt("offset") ?: 0, 0)
        [max: max, offset: offset]
    }

    @CompileStatic
    static <T> List<T> paginate(List<T> list, Map<String, Integer> paginationParams) {
        if (list.empty) {
            return []
        }

        return list[getRange(list.size(), paginationParams.offset, paginationParams.max)]
    }

    @CompileStatic
    private static Range<Integer> getRange(int size, int offset, int max) {
        int start = Math.max(0, offset)
        int end = Math.min(size, offset + max)
        return new IntRange(false, start, end)
    }

    /**
     * @param requiredParams List of all required params for the endpoint
     * @return true when all required params exist
     */
    protected boolean paramsSet(List<String> requiredParams, boolean trimValues = false) {
        List<String> missingParams = []
        for (String requiredParam : requiredParams) {
            def value = params[requiredParam]
            value = trimValues ? trimParams(value) : value
            if (!params.containsKey(requiredParam) || !value) {
                if (value != null && Boolean.FALSE == value) {
                    continue
                }
                missingParams << requiredParam
            }
        }
        if (missingParams) {
            renderJson(ResponseUtil.getMissingParameter(missingParams.join(", ")))
            return false
        }
        return true
    }

    private def trimParams(def value) {
        if (value == null) {
            return null
        } else if (value instanceof String) {
            return value.trim()
        } else if (value instanceof Collection) {
            return value.grep()
        }
        return value
    }

    protected void renderErrors(GormValidateable validatableObject) {
        renderJson(ResponseUtil.getErrors(validatableObject.errors.allErrors))
    }

    protected void renderErrors(List<DefaultMessageSourceResolvable> errors) {
        renderJson(ResponseUtil.getErrors(errors))
    }

    /**
     * Calls the given closure with a temporary file create from the file in the request with the given fileParam
     * <strong>and delete the file after the closure has been executed</strong>.
     * If the closure has 2 parameters, we also pass the original filename as second parameter to the closure.
     *
     * @param fileParam the param in which to look for the file, default: file
     * @param closure the closure to call
     */
    private static final long MAX_FILE_SIZE = 50000000

    protected void withFile(String fileParam = 'file', Object container = params, boolean delete = true, Closure closure) {
        File tmpFile
        try {
            Object fileParamValue = container."$fileParam"
            if (fileParamValue && fileParamValue instanceof String) {
                // there was a parameter for the file as a string within the json; decoding it and using it as a file
                String base64 = fileParamValue
                tmpFile = Files.createTempFile("withFile", null).toFile()
                OutputStream stream = new FileOutputStream(tmpFile)
                stream.write(base64.decodeBase64())
                stream.flush()

                if (tmpFile.length() > MAX_FILE_SIZE) {
                    renderJson(ResponseUtil.getError("file size ${tmpFile.length()} exceeds maxFileSize of $MAX_FILE_SIZE bytes"))
                    return
                }

                // calling the closure with the file decoded from the json body
                closure.call(tmpFile)
            } else if ((request instanceof MultipartHttpServletRequest)) {
                def f = ((MultipartHttpServletRequest) request)?.getFile(fileParam)
                if (!f || f.empty) {
                    renderJson(new Response(status: Response.ResponseStatus.MISSING_PARAMETER, message: "file missing. please attach a file in field: '${fileParam}'"))
                    return
                }

                tmpFile = Files.createTempFile("withFile", null).toFile()
                MultipartFile multipartFile = ((MultipartHttpServletRequest) request).getFile(fileParam)
                multipartFile.transferTo(tmpFile)

                if (tmpFile.length() > MAX_FILE_SIZE) {
                    renderJson(ResponseUtil.getError("file size ${tmpFile.length()} exceeds maxFileSize of $MAX_FILE_SIZE bytes"))
                    return
                }

                if (closure.maximumNumberOfParameters == 1) {
                    closure.call(tmpFile)
                } else {
                    closure.call(tmpFile, multipartFile.getOriginalFilename())
                }
            } else {
                renderJson(new Response(status: Response.ResponseStatus.INVALID_PARAMETER, message: 'Expecting file content encoded as base64 in JSON body or within multipart request.'))
            }
        } catch (IIOException e) {
            log.warn "error parsing image", e
            renderJson(ResponseUtil.getError("invalid file format"))
        } catch (UberallHttpException | ConcurrencyFailureException e) {
            // we want to rethrow exceptions that have a specific handler, like httpExceptionHandler method
            throw e
        } catch (Exception e) {
            log.error("Problems decoding + using the file content.", e)
            renderJson(ResponseUtil.getServerError("Problems decoding + using the file content."))
        } finally {
            if (delete) {
                tmpFile?.delete()
            } else {
                tmpFile?.deleteOnExit()
            }
        }
    }

    protected void withFileName(String fileName = params.fileName, Closure closure) {
        if (!fileName) {
            renderJson(ResponseUtil.getMissingParameter('fileName'))
            return
        }

        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()).toLowerCase()
        FileExtension fileExtensionEnum = getFileExtension(fileExtension)
        if (!(fileExtensionEnum)) {
            renderJson(ResponseUtil.getInvalidParameter('fileName', "File is missing a valid extension. Supported extensions: $supportedFileExtensions"))
            return
        }

        String newFileName = RandomStringUtils.randomAlphanumeric(12) + ".$fileExtension"

        if (closure.maximumNumberOfParameters == 2) {
            closure.call(newFileName, fileExtension)
        } else {
            closure.call(newFileName)
        }
    }

    protected FileExtension getFileExtension(String fileExtension) {
        FileExtension.findByExtension(fileExtension)
    }

    protected List<String> getSupportedFileExtensions() {
        FileExtension.values()*.extension
    }

    /**
    protected void withParentSalesPartner(Closure closure) {
        User apiAdmin = authenticationService.currentUser

        if (!apiAdmin) {
            renderJson(ResponseUtil.getError('Error occurred while trying to authenticate privateKey.'))
            return
        }
        if (!apiAdmin.salesPartner.isParentSalesPartner()) {
            renderJson(ResponseUtil.getForbidden('Given private key does not belong to a parent sales partner.'))
            return
        }

        closure.call(apiAdmin.salesPartner)
    }

    protected void withSalesPartner(salesPartnerId = params.id, boolean checkApiAccess = true, Closure closure) {
        if (!salesPartnerId) {
            renderJson(ResponseUtil.getMissingParameter('id', 'No SalesPartner id given.'))
            return
        }
        if (salesPartnerId instanceof String && !salesPartnerId.isNumber()) {
            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
            return
        }

        User uberAdmin = authenticationService.uberUser
        User apiAdmin = authenticationService.currentUser
        SalesPartner salesPartner = SalesPartner.get(salesPartnerId as Long)

        if (!apiAdmin) {
            renderJson(ResponseUtil.getError('Error occurred while trying to authenticate privateKey. Valid SalesPartner privateKey is needed.'))
            return
        }
        if (!salesPartner) {
            renderJson(ResponseUtil.getNotFound())
            return
        }
        if (checkApiAccess && !ApiSalesPartnerController.hasApiAccess(salesPartner, uberAdmin, apiAdmin)) {
            renderJson(ResponseUtil.getForbidden('Given privateKey is not allowed to manage this SalesPartner via API.'))
            return
        }

        closure.call(salesPartner, uberAdmin ?: apiAdmin)
    }

    protected void withSalesPartnerId(salesPartnerId = params.id, Closure closure) {
        if (!salesPartnerId) {
            renderJson(ResponseUtil.getMissingParameter('id', 'No SalesPartner id given.'))
            return
        }
        if (salesPartnerId instanceof String && !salesPartnerId.isNumber()) {
            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
            return
        }

        SalesPartner salesPartner = SalesPartner.get(salesPartnerId as Long)

        if (!salesPartner) {
            renderJson(ResponseUtil.getNotFound())
            return
        }
        closure.call(salesPartner)
    }

    protected void withBusiness(businessId = params.businessId, Boolean checkSalesPartnerOnly = false, Closure closure) {
        def targetBusinessId
        if (!businessId && params.id) {
            targetBusinessId = params.id
        } else {
            targetBusinessId = businessId
        }
        if (!targetBusinessId) {
            renderJson(ResponseUtil.getMissingParameter('id (business)'))
        } else if (targetBusinessId in String && !targetBusinessId.isNumber()) {
            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
        } else {
            Business business = Business.get(targetBusinessId as Long)
            if (!business) {
                renderJson(ResponseUtil.getInvalidParameter('business'))
            } else if (business.status == BusinessStatus.DELETED) {
                renderJson(ResponseUtil.notFound)
            } else if ((checkSalesPartnerOnly && business.salesPartnerId != user.salesPartnerId) || !userAccessService.canAccessBusiness(user, business)) {
                renderForbiddenBusiness(business)
            } else {
                closure.call(business)
            }
        }
    }

    protected void withBusinessId(businessId = params.id, Closure closure) {
        if (!businessId) {
            log.error("TPTestRandom: Couldnt find business")
            renderJson(ResponseUtil.getMissingParameter('id', 'No Business id given.'))
            return
        }
        if (businessId instanceof String && !businessId.isNumber()) {
            log.error("TPTestRandom: Couldnt find business")
            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
            return
        }

        Business business = Business.get(businessId as Long)

        if (!business) {
            log.error("TPTestRandom: Couldnt find business")
            renderJson(ResponseUtil.getNotFound())
            return
        }
        closure.call(business)
    }

    protected void withProductPlan(productPlanId = params.productPlanId, Closure closure) {
        def targetProductPlanId
        if (!productPlanId && params.id) {
            targetProductPlanId = params.id
        } else {
            targetProductPlanId = productPlanId
        }

        if (!targetProductPlanId) {
            renderJson(ResponseUtil.getMissingParameter('id (product Plan)'))
        } else if (targetProductPlanId in String && !targetProductPlanId.isNumber()) {
            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
        } else {
            ProductPlan productPlan = ProductPlan.get(targetProductPlanId as Long)
            if (!productPlan) {
                renderJson(ResponseUtil.getInvalidParameter('productPlan'))
            } else if (productPlan.salesPartner != user.salesPartner) {
                renderForbidden()
            } else {
                closure.call(productPlan)
            }
        }
    }

    protected void withSearchData(def id = params.id, String token = params.token, Closure closure) {
        if (!id) {
            renderJson(ResponseUtil.getMissingParameter('id'))
        } else if (id in String && !id.isNumber()) {
            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
        } else if (!token) {
            renderJson(ResponseUtil.getMissingParameter('token'))
        } else {
            SearchData searchData = SearchData.findByIdAndToken(id, token)
            if (searchData) {
                closure.call searchData
            } else {
                renderJson(ResponseUtil.getInvalidParameter('token'))
            }
        }
    }

    protected void withLocation(locationId = params.id, boolean readOnly = true, List<Feature> requiredFeatures = [], Closure closure) {
        if (!locationId) {
            renderJson(ResponseUtil.getMissingParameter('id (location)'))
            return
        }

        if (locationId in String && !locationId.isNumber()) {
            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
            return
        }

        Location location = readOnly ? Location.read(locationId) : Location.get(locationId)
        if (!location || !location.business) {
            renderForbidden()
            return
        }

        if (location.status == LocationStatus.DELETED) {
            renderJson(ResponseUtil.notFound)
            return
        }

        // user has access through managedBusinesses/managedLocations
        if (!userAccessService.canAccessLocationId(user, location.id)) {
            renderForbidden()
            return
        }

        if (requiredFeatures) {
            List<Feature> locationFeatures = featureService.getProductPlanFeaturesFromLocation(location.id)
            if (!locationFeatures.containsAll(requiredFeatures)) {
                renderForbidden()
                return
            }
        }

        closure.call(location)
    }

    protected void withLocation(List<Feature> requiredFeatures, Closure closure) {
        this.withLocation(params.id, true, requiredFeatures, closure)
    }

    /**
     * Makes all accessible locationIds for the current user available in the closure
     * @param requiredFeature (optional) only locationIds with these Features are returned
     */
//    protected void withLocationIds(List<Feature> requiredFeatures = [], String logPrefix = null, Closure closure) {
//        boolean isLogEnabled = configurationService.getBoolean("uberall.homepage.logging", false)
//        String logEndpoint = configurationService.getStr("uberall.homepage.logging.endpoint", "all")
//        StopWatch stopWatch = (isLogEnabled && logPrefix && (logEndpoint == logPrefix || logEndpoint == "all")) ? new StopWatch(logPrefix) : null
//
//        stopWatch?.start("userService.getLocationIds")
//        List<Long> locationIds = userService.getLocationIds(user, true)
//        stopWatch?.stop()
//
//        if (!locationIds) {
//            stopWatch?.start("featureService.getProductPlanDefaultFeaturesForUser")
//            List<Feature> defaultProductPlanFeatures = featureService.getProductPlanDefaultFeaturesForUser(user)
//            stopWatch?.stop()
//
//            if (defaultProductPlanFeatures.containsAll(requiredFeatures)) {
//                if (stopWatch) {
//                    closure.call([], stopWatch)
//                } else {
//                    closure.call([])
//                }
//                return
//            }
//            renderForbidden()
//            return
//        }
//
//        if (logPrefix in ["todosMultiLocation", "customerFeedbackMultiLocation", "insightsDataMultiLocation", "searchRanksMultiLocation", "headerMultiLocation"]) {
//            int max = configurationService.getInt('uberall.homepage.maxLocationsCount', 2000)
//
//            if (locationIds.size() > max) {
//                List<Long> heavySalesPartners = configurationService.getListOfLong('uberall.homepage.heavySalesPartners', [])
//
//                if (user.salesPartner.id in heavySalesPartners) {
//                    locationIds = locationIds.take(max)
//                }
//            }
//        }
//
//        List<Long> locationIdsWithFeatures = []
//
//        if (requiredFeatures) {
//            stopWatch?.start("featureService.filterLocationIdsWithAllFeatures")
//            locationIdsWithFeatures = featureService.filterLocationIdsWithAllFeatures(locationIds, requiredFeatures)
//            stopWatch?.stop()
//            if (!locationIdsWithFeatures) {
//                renderForbidden()
//                return
//            }
//        }
//
//        if (stopWatch) {
//            closure.call(locationIdsWithFeatures ?: locationIds, stopWatch)
//        } else {
//            closure.call(locationIdsWithFeatures ?: locationIds)
//        }
//    }
//
//    protected void withLocationId(Closure closure) {
//        Long id
//        try {
//            id = params.id as Long
//            if (!id) {
//                renderJson(ResponseUtil.getMissingParameter('id (location)'))
//                return
//            }
//        } catch (NumberFormatException ignore) {
//            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
//            return
//        }
//
//        if (!userAccessService.canAccessLocationId(user, id)) {
//            renderJson(ResponseUtil.notFound)
//            return
//        }
//
//        def locationStatus = Location.createCriteria().get {
//            eq('id', id)
//            projections { property('status') }
//        } as LocationStatus
//
//        // TODO: remove the deleted check in UB-37261
//        if (locationStatus == LocationStatus.DELETED) {
//            renderJson(ResponseUtil.notFound)
//            return
//        }
//
//        closure.call(id)
//    }
//
//    @Deprecated // use withListingDto instead
//    protected void withListing(listingId = params.id, Closure closure) {
//        if (!listingId) {
//            renderJson(ResponseUtil.getMissingParameter('id (listing)'))
//        } else if (listingId in String && !listingId.isNumber()) {
//            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
//        } else {
//            Listing listing = Listing.get(listingId)
//            if (!listing || !listing.location.business) {
//                renderForbidden()
//            } else if (userAccessService.canAccessLocationId(user, listing.location.id)) {
//                // salesPartner or user has access through his managedBusinesses / managedLocations
//                closure.call(listing)
//            } else {
//                renderForbidden()
//            }
//        }
//    }
//
//    protected void withListingDto(listingId = params.id, Closure closure) {
//        if (!listingId) {
//            renderJson(ResponseUtil.getMissingParameter('id (listing)'))
//        } else if (listingId in String && !listingId.isNumber()) {
//            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
//        } else {
//            ListingDto listing = listingDtoDataService.getListing(Long.valueOf(listingId as String))
//            if (!listing) {
//                renderForbidden()
//            } else if (userAccessService.canAccessLocationId(user, listing.locationId)) {
//                // salesPartner or user has access through his managedBusinesses / managedLocations
//                closure.call(listing)
//            } else {
//                renderForbidden()
//            }
//        }
//    }
//
//    /**
//     * Helper function making sure we have a user object, which can be accessed by the current sales partner.
//     *
//     * @param otherUserId id of the user that shall be accessed
//     * @param closure which will be executed, if user belongs to sales partner
//     */
//    protected withUser(otherUserId = params.id, Closure closure) {
//        if (!otherUserId) {
//            renderJson(ResponseUtil.getMissingParameter('id'))
//            return
//        }
//
//        if (otherUserId in String && !otherUserId.isNumber()) {
//            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
//            return
//        }
//
//        User callingUser = user
//        User otherUser = User.findByIdAndSalesPartnerAndStatusNotEqual(otherUserId as long, callingUser.salesPartner, UserStatus.DELETED)
//        if (!userAccessService.canAccessUser(callingUser, otherUser)) {
//            renderJson(ResponseUtil.getError("User does not exist or you don't have access rights"))
//            return
//        }
//
//        closure.call(otherUser)
//    }
//
//    protected withSameUser(String userId = params.id, Closure closure) {
//        if (!userId) {
//            renderJson(ResponseUtil.getMissingParameter('id'))
//            return
//        }
//
//        if (!userId.isLong()) {
//            renderJson(ResponseUtil.getWrongParameterType('id', 'Long'))
//            return
//        }
//
//        User callingUser = user
//        User foundUser = User.get(userId)
//        if (!foundUser || foundUser.status == UserStatus.DELETED || callingUser.id != foundUser.id) {
//            renderJson(ResponseUtil.getError('User does not exist or you don\'t have access rights'))
//            return
//        }
//
//        closure.call(foundUser)
//    }
//
//    protected withLocationGroup(Long locationGroupId = params.getLong('id'), Closure closure) {
//        LocationGroup locationGroup = LocationGroup.get(locationGroupId)
//        User apiUser = user
//
//        if (!locationGroup || locationGroup.status == LocationGroup.Status.DELETED) {
//            renderNotFound()
//            return
//        }
//
//        if ((apiUser.hasAdminRole() && locationGroup.salesPartner.id != apiUser.salesPartner.id) ||
//                (!apiUser.hasAdminRole() && !apiUser.getLocationGroups(true).contains(locationGroup))) {
//            renderForbidden()
//            return
//        }
//
//        closure.call(locationGroup)
//    }

    /**
     * Convert the request.params to a filter to apply to GORM list()
     */
    protected withGormListFilter(Closure closure) {
        Map filterParams = [
                offset: params.getInt("offset") ?: 0,
                max   : params.getInt("max") ?: configurationService.getInt('uberall.defaults.pageSize'),
                sort  : params.getProperty('sort') ?: 'id',
                order : params.getProperty('order') ?: 'desc',
        ]
        if (!(filterParams.order.toLowerCase() in ["asc", "desc"])) {
            renderJson(ResponseUtil.getInvalidParameter('order', 'Order must be either asc or desc.'))
            return
        }
        closure.call(filterParams)
    }

    /**
     * return true if the parameter (specified by the given key) is Boolean or string that has value "true" or "false"
     */
    protected boolean isBoolean(String paramKey) {
        Object o = params.get(paramKey)
        if (o instanceof Boolean) {
            return true
        }
        if (o instanceof String) {
            return o ==~ BOOLEAN_PATTERN
        }
        false
    }

    /**
     * Handles {@link UberallHttpException}s thrown by Api*Controller actions.
     */
    def httpExceptionHandler(UberallHttpException exception) {
        SalesPartner.withNewTransaction([readOnly: true]) {
            Response resp = exception.response
            // we have to re-attach domain objects since they've been discarded due to the transaction rollback
            // currently we're only sending domain objects on 409 CONFLICT's (e.g. duplicate identifier)
            if (resp.status == Response.ResponseStatus.CONFLICT) {
                resp.response.values().each { entry ->
                    if (entry instanceof Collection) {
                        entry.each {
                            if (DomainClassArtefactHandler.isDomainClass(it.class)) {
                                it.refresh()
                            }
                        }
                    } else if (DomainClassArtefactHandler.isDomainClass(entry.class)) {
                        entry.refresh()
                    }
                }
            }
            log.warn("${resp as JSON}")
            renderJson(resp)
        }
    }

    /**
     * Handles DuplicateKeyExceptions on identifier and returns the conflicting object in such a case.
     * @param identifier the duplicated identifier
     * @param object on which object.save() raised the exception
     * @parma object that in couple with identifier violates the constraint (ex. identifier-business or identifier-salesPartner)
     */
    def identifierDuplicateKeyExceptionHandler(def identifier, def raisingExceptionObj, def conflictingObjForIdentifier) {
        raisingExceptionObj.withNewTransaction(readOnly: true) {
            def fromObjectName = raisingExceptionObj.class.simpleName
            def classNameToUnderscoreAndId = "${StringUtils.camelCaseToUnderscore(conflictingObjForIdentifier.class.simpleName)}_id"

            String query = "FROM $fromObjectName " +
                    "WHERE identifier = '$identifier' " +
                    "AND $classNameToUnderscoreAndId = ${conflictingObjForIdentifier.id}"
            def conflictingObject = raisingExceptionObj.executeQuery(query)
            def fieldName = conflictingObject.find() ? conflictingObject.find().class.simpleName : null
            throw new UberallHttpException(ResponseUtil.getConflictParameter('identifier',
                    Response.ErrorCode.IDENTIFIER_NOT_UNIQUE,
                    fieldName,
                    conflictingObject.find()))
        }
    }

    /**
     * Handles {@link ConcurrencyFailureException}s and returns a 503 Service Unavailable in such a case.
     * This covers all optimistic and pessimistic exceptions thrown by spring (usually wrapping a hibernate exception).
     * The caller is supposed to retry the request.
     * @see org.springframework.dao.OptimisticLockingFailureException
     * @see org.springframework.dao.PessimisticLockingFailureException
     */
    def concurrencyFailureHandler(ConcurrencyFailureException exception) {
        log.warn "ConcurrencyFailureException in $actionUri $request.forwardURI", exception

        renderJson(new Response(
                status: Response.ResponseStatus.RESOURCE_LOCKED,
                message: "resource is currently in use, retry operation in a bit"
        ))
    }

    /**
     * Handles {@link StaleStateException}s and returns a 503 Service Unavailable in such a case.
     * This covers optimistic lock exceptions thrown by hibernate.
     * The caller is supposed to retry the request.
     * @see org.hibernate.StaleObjectStateException
     */
    def staleStateExceptionHandler(StaleStateException exception) {
        log.warn "StaleStateException in $actionUri $request.forwardURI", exception

        renderJson(new Response(
                status: Response.ResponseStatus.RESOURCE_LOCKED,
                message: "resource is currently in use, retry operation in a bit"
        ))
    }

    /**
     * if creator is API_ADMIN get the owner by the userId parameter; otherwise use the current user as owner
     */
//    protected User identifyOwner(boolean returnErrorMessages = true) {
//        User owner
//
//        if (user.isRole(UserRole.API_ADMIN)) {
//            if (!params.getLong('userId')) {
//                if (returnErrorMessages) {
//                    renderJson(ResponseUtil.getMissingParameter('userId'))
//                }
//                return null
//            }
//
//            owner = User.findByIdAndSalesPartnerAndStatusNotEqual(params.getLong('userId'), user.salesPartner, UserStatus.DELETED)
//
//            if (!owner || owner.isHidden()) {
//                if (returnErrorMessages) {
//                    renderForbidden()
//                }
//                return null
//            }
//        } else {
//            owner = user
//        }
//
//        owner
//    }

    protected String getLanguage() {
        String result = "en"
        if (params.language && params.language in Country.values().locale.language) {
            result = params.language
        } else if (params.country && params.country in (Country.values()*.toString() + 'GB')) {
            result = Country.fromString(params.country).locale.language
        } else if (user?.salesPartner) {
            result = user.salesPartner.country.locale.language
        } else if (user?.managedBusinesses) {
            result = user.managedBusinesses.first().country.locale.language
        }

        return result
    }

    /**
     * Method current being used to get user id for the connect flows in the application
     * where the user id is being tried to derive
     *  - directly from params
     *  - via access token
     */
//    Long getUserIdFromRequest() {
//        // userId would only be present in params
//        // a) when flow is initiated via passing of connect url to the client
//        // b) when flow is invoked via Admin in ABE
//        Long userId = params.userId?.toString()?.isNumber() ? params.userId as long : user?.id
//
//        //  If its still null it means the request is being directed from uberall api - lets try to devise it via access token
//        if (!userId && params.get('access_token')) {
//            ApiAccessToken token = authenticationService.getLatestToken(params.get('access_token').toString())
//            User user = authenticationService.authenticateToRequest(token)
//            userId = user?.id
//        }
//        return userId
//    }

    /**
     * Implements a generic pagination check and parsing for 'max' & 'offset' (if params are present)
     * Additionally if 'total' is provided will also return paginated params: 'to' & 'nextOffset'
     * @param API params
     * @applyMax should override 'max' with default value
     * @return Map with either 'success: true' & pagination params extracted or else values for 'parameter' & 'expected' that can be used for ResponseUtil.getWrongParameterType
     */
    protected Map parsePaginationParams(Map params, boolean applyMax = true) {
        Map args = [success: true]

        // parse max
        if (params.max) {
            Integer max = params.getInt('max')
            if (max == null) {
                return [success: false, parameter: "max", expected: "number"]
            }

            if (max <= 0) {
                return [success: false, parameter: "max", expected: "Integer greater than 0"]
            }
            args.max = max
        }

        if (applyMax) {
            int defaultMax = configurationService.getInt('uberall.defaults.pageSize', 50)
            if (!args.max || (args.max > defaultMax)) {
                args.max = defaultMax
            }
        }

        // parse offset
        if (params.offset) {
            Integer offset = params.getInt('offset')
            if (offset == null) {
                return [success: false, parameter: "offset", expected: "number"]
            }

            if (offset < 0) {
                return [success: false, parameter: "offset", expected: "Integer greater than 0"]
            }

            args.offset = offset
        }

        // parse to, nextOffset
        if (params.total) {
            Integer total = params.getInt('total') ?: 0
            Integer max = args.max  ?: 0
            Integer offset = args.offset  ?: 0

            if (!total || offset >= total) {
                return args + [offset: 0, to: 0, nextOffset: 0]
            }

            if (!max && !offset) {
                // no pagination should be applied
                args + [offset: 0, to: total, nextOffset: 0]
            }

            int to = ((max + offset >= total) || !max) ? total : (max + offset)
            int nextOffset = (total > offset + max) ? (offset + max) : 0

            return args + [offset: offset, to: to, nextOffset: nextOffset]
        }

        return args
    }
}
