package uberall

ruleset {
    description '''
        A Sample Groovy RuleSet containing all CodeNarc Rules, grouped by category.
        You can use this as a template for your own custom RuleSet.
        Just delete the rules that you don't want to include.
        '''

    // rulesets/basic.xml
    AssertWithinFinallyBlock
    AssignmentInConditional
    BigDecimalInstantiation
    BitwiseOperatorInConditional
    BlockStartsWithBlankLine
    BlockEndsWithBlankLine
    BooleanGetBoolean
    BrokenNullCheck
    BrokenOddnessCheck
    ClassForName
    ComparisonOfTwoConstants {
        doNotApplyToFileNames = "AdminBackendTagLibSpec.groovy" // codenarc is plain wrong here
    }
//    ComparisonWithSelf // FLN
    ConstantAssertExpression
    ConstantIfExpression
    ConstantTernaryExpression
    DeadCode
    DoubleNegative
    DuplicateCaseStatement
    DuplicateMapKey
    DuplicateSetValue
    EmptyCatchBlock
    EmptyClass {
        doNotApplyToFileNames = "UberListingsEnums.groovy"
    }
    EmptyElseBlock
    EmptyFinallyBlock
    EmptyForStatement
    EmptyIfStatement
    EmptyInstanceInitializer
//    EmptyMethod
    EmptyStaticInitializer
    EmptySwitchStatement
    EmptySynchronizedStatement
    EmptyTryBlock
    EmptyWhileStatement
    EqualsAndHashCode
    EqualsOverloaded
    ExplicitGarbageCollection
    ForLoopShouldBeWhileLoop
    HardCodedWindowsFileSeparator
    HardCodedWindowsRootDirectory
    IntegerGetInteger
    MultipleUnaryOperators
    RandomDoubleCoercedToZero
    RemoveAllOnSelf
    ReturnFromFinallyBlock
    ThrowExceptionFromFinallyBlock

    // rulesets/braces.xml
    ForStatementBraces
    IfStatementBraces
    ElseBlockBraces
    WhileStatementBraces
    // rulesets/concurrency.xml
    BusyWait
    DoubleCheckedLocking
    InconsistentPropertyLocking
    InconsistentPropertySynchronization
    NestedSynchronization
    StaticCalendarField
    StaticConnection
    StaticDateFormatField
    StaticMatcherField
    StaticSimpleDateFormatField
    SynchronizedMethod {
        doNotApplyToFileNames = "BrowserService.groovy,FacebookApiException.groovy,EventService.groovy,UberallHttpException.groovy,DirectoryService.groovy"
    }
    SynchronizedOnBoxedPrimitive
    SynchronizedOnGetClass
    SynchronizedOnReentrantLock
    SynchronizedOnString
    SynchronizedOnThis
    SynchronizedReadObjectMethod
    SystemRunFinalizersOnExit
    ThisReferenceEscapesConstructor
    ThreadGroup
    ThreadLocalNotStaticFinal
    ThreadYield
    UseOfNotifyMethod
    VolatileArrayField
    VolatileLongOrDoubleField
    WaitOutsideOfWhileLoop

    // rulesets/convention.xml
    ConfusingTernary
    CouldBeElvis {
        doNotApplyToFileNames = "FieldMissingTodoItemService.groovy,ProfileCompletenessService.groovy"
    }
    HashtableIsObsolete
    IfStatementCouldBeTernary
    InvertedIfElse
    LongLiteralWithLowerCaseL
//    NoDef
    ParameterReassignment
    TernaryCouldBeElvis
    VectorIsObsolete

    // rulesets/design.xml
    AbstractClassWithPublicConstructor
//    AbstractClassWithoutAbstractMethod
    AssignmentToStaticFieldFromInstanceMethod {
        doNotApplyToFileNames = "MessageService.groovy,CookieService.groovy" // we are doing it in init and destroy which is fine!
    }
    BooleanMethodReturnsNull {
        doNotApplyToFileNames = "FoursquareUpdateService.groovy" // codenarc is confused here, the return is not in a boolean method but in a closure
    }
//    BuilderMethodWithSideEffects
    CloneableWithoutClone
    CloseWithoutCloseable
    CompareToWithoutComparable
    ConstantsOnlyInterface
    EmptyMethodInAbstractClass {
        doNotApplyToFileNames = "UberListingService.groovy,DirectoryService.groovy,DirectoryUpdateService.groovy," +
                "UberListingUpdateService.groovy,AbstractLocationDataService.groovy,DirectoryDataPointService.groovy,DirectoryExportService.groovy" +
                ",OfflineDirectoryService.groovy, IntegratedExportService.groovy"
    }
    FinalClassWithProtectedMember
    ImplementationAsType
//    Instanceof
    LocaleSetDefault // FLN
    PrivateFieldCouldBeFinal
    PublicInstanceField {
        doNotApplyToFileNames = "*InvoiceInfo.groovy" // jasper file, must have public members
    }
    ReturnsNullInsteadOfEmptyArray
    ReturnsNullInsteadOfEmptyCollection {
        doNotApplyToFileNames = "InteractionsHomepageService.groovy"
    }
    SimpleDateFormatMissingLocale
    StatelessSingleton
    ToStringReturnsNull {
        doNotApplyToFileNames = "MfaRequest.groovy"
    }

    // rulesets/dry.xml
//    DuplicateListLiteral
//    DuplicateMapLiteral
//    DuplicateNumberLiteral
//    DuplicateStringLiteral

//     rulesets/enhanced.xml
//    CloneWithoutCloneable
//    JUnitAssertEqualsConstantActualValue
//    UnsafeImplementationAsMap

    // rulesets/exceptions.xml
    CatchArrayIndexOutOfBoundsException
    CatchError
//    CatchException
    CatchIllegalMonitorStateException
    CatchIndexOutOfBoundsException
    CatchNullPointerException
//    CatchRuntimeException  // shall we reactivate this? we have lots of violations, but seems with good reason
//    CatchThrowable // shall we reactivate this? we have lots of violations, but seems with good reason
    ConfusingClassNamedException {
        doNotApplyToFileNames = "RetryOnException.groovy"
    }
    ExceptionExtendsError
    ExceptionExtendsThrowable
    ExceptionNotThrown
    MissingNewInThrowStatement
    ReturnNullFromCatchBlock {
        doNotApplyToFileNames = "*Controller.groovy,DirectoryService.groovy" // in controllers it can make sense to return null after redirect
    }
    SwallowThreadDeath
    ThrowError
    ThrowException
    ThrowNullPointerException
//    ThrowRuntimeException
    ThrowThrowable

    // rulesets/formatting.xml
    BlankLineBeforePackage
    BracesForClass
    BracesForForLoop
    BracesForIfElse
    BracesForMethod
    BracesForTryCatchFinally
//    ClassJavadoc // FLN enable later
    ClosureStatementOnOpeningLineOfMultipleLineClosure
    ConsecutiveBlankLines
    FileEndsWithoutNewline
    LineLength {
        length = 200
        doNotApplyToFileNames = "*Spec.groovy, TestStartupUtil.groovy, GeometryUtils.groovy"
    } // FLN reduce over time
    MissingBlankLineAfterImports
    MissingBlankLineAfterPackage
    SpaceAfterCatch
    SpaceAfterClosingBrace
    SpaceAfterComma
    SpaceAfterFor
    SpaceAfterIf
    SpaceAfterOpeningBrace
    SpaceAfterSemicolon
    SpaceAfterSwitch
    SpaceAfterWhile
    SpaceAroundClosureArrow
//    SpaceAroundMapEntryColon
    SpaceAroundOperator
    SpaceBeforeClosingBrace
    SpaceBeforeOpeningBrace
    TrailingWhitespace

    // rulesets/generic.xml
    IllegalClassMember
    IllegalClassReference
    IllegalPackageReference
    IllegalRegex
    IllegalString
    IllegalSubclass
    RequiredRegex
    RequiredString
    StatelessClass

    // rulesets/grails.xml
//    GrailsDomainHasEquals
//    GrailsDomainHasToString
//    GrailsDomainReservedSqlKeywordName
    GrailsDomainWithServiceReference
    GrailsDuplicateConstraint
    GrailsDuplicateMapping
    GrailsMassAssignment
    GrailsPublicControllerMethod
    GrailsServletContextReference
    GrailsStatelessService {
        doNotApplyToFileNames = '''
                DirectoryUpdateService.groovy,
                NormalizationService.groovy,
                MessageService.groovy,
                CategoryService.groovy,
                AbstractReminderEmailService.groovy,
                QuickSightService.groovy,
                SqsQueueProducerService.groovy,
                HomepageTodoService.groovy,
                ConnectDirectoryTodoItemService.groovy,
                LocationConnectService.groovy
        '''
        addToIgnoreFieldNames = '''
                swagger,
                parameterResolverUtil,
                applicationContext,
                groovyPageRenderer,
                grailsApplication,
                *QueueConsumer,
                *QueueProducer,
                *QueueConfig,
                *Holder,
                *Helper,
                *Dao
        '''
    }

    // rulesets/groovyism.xml
    AssignCollectionSort {
        doNotApplyToFileNames = "OAuthUtils.groovy"
    }
    AssignCollectionUnique
    ClosureAsLastMethodParameter {
        doNotApplyToFileNames = "RerunFailedAutoSyncsJobIntegrationSpec.groovy,ApiWidgetsControllerIntegrationSpec.groovy"
    }
    CollectAllIsDeprecated
    ConfusingMultipleReturns
    ExplicitArrayListInstantiation
    ExplicitCallToAndMethod
    ExplicitCallToCompareToMethod
    ExplicitCallToDivMethod
    ExplicitCallToEqualsMethod
    ExplicitCallToGetAtMethod {
        doNotApplyToFileNames = "ApiTokenlessAuthenticationInterceptor.groovy"
    }
    ExplicitCallToLeftShiftMethod
    ExplicitCallToMinusMethod
    ExplicitCallToModMethod
    ExplicitCallToMultiplyMethod
    ExplicitCallToOrMethod
    ExplicitCallToPlusMethod
    ExplicitCallToPowerMethod
    ExplicitCallToRightShiftMethod
    ExplicitCallToXorMethod
    ExplicitHashMapInstantiation
    ExplicitHashSetInstantiation
    ExplicitLinkedHashMapInstantiation
    ExplicitLinkedListInstantiation
    ExplicitStackInstantiation
    ExplicitTreeSetInstantiation
//    GStringAsMapKey
    GStringExpressionWithinString
//    GetterMethodCouldBeProperty // FLN check later
    GroovyLangImmutable
//    UseCollectMany
    UseCollectNested

    // rulesets/imports.xml
    DuplicateImport
    ImportFromSamePackage
    ImportFromSunPackages
//    MisorderedStaticImports
//    NoWildcardImports
    UnnecessaryGroovyImport
    UnusedImport

    // rulesets/jdbc.xml
    DirectConnectionManagement
//    JdbcConnectionReference
    JdbcResultSetReference
    JdbcStatementReference

    // rulesets/junit.xml
    ChainedTest
    CoupledTestCase
//    JUnitAssertAlwaysFails
//    JUnitAssertAlwaysSucceeds
//    JUnitFailWithoutMessage
//    JUnitLostTest
//    JUnitPublicField
//    JUnitPublicNonTestMethod
//    JUnitPublicProperty
//    JUnitSetUpCallsSuper
//    JUnitStyleAssertions
//    JUnitTearDownCallsSuper
//    JUnitTestMethodWithoutAssert
//    JUnitUnnecessarySetUp
//    JUnitUnnecessaryTearDown
//    JUnitUnnecessaryThrowsException
    SpockIgnoreRestUsed
    UnnecessaryFail
    UseAssertEqualsInsteadOfAssertTrue
    UseAssertFalseInsteadOfNegation
    UseAssertNullInsteadOfAssertEquals
    UseAssertSameInsteadOfAssertTrue
    UseAssertTrueInsteadOfAssertEquals
    UseAssertTrueInsteadOfNegation

    // rulesets/logging.xml
    LoggerForDifferentClass
    LoggerWithWrongModifiers
    LoggingSwallowsStacktrace
    MultipleLoggers
    PrintStackTrace
    Println
    SystemErrPrint
    SystemOutPrint

    // rulesets/naming.xml
    AbstractClassName
    ClassName
    ClassNameSameAsFilename
    ClassNameSameAsSuperclass
    ConfusingMethodName
//    FactoryMethodName
    FieldName
    InterfaceName
    InterfaceNameSameAsSuperInterface
    MethodName {
        regex = /[a-zA-Z0-9\s(),#:_=-]*/
        doNotApplyToFileNames = '*Spec.groovy'
    }
    ObjectOverrideMisspelledMethodName
//    PackageName
    PackageNameMatchesFilePath
    ParameterName
    PropertyName {
        ignorePropertyNames = "serialVersionUID,private_key,access_token,ub_trk,wt_cookie_id,ub_u_id,ub_ref,OAuthService,OAuthServiceMock"
    }
    VariableName

    // rulesets/security.xml
    FileCreateTempFile
//    InsecureRandom // we need Random!
//    JavaIoPackageAccess
    NonFinalPublicField {
        doNotApplyToFileNames = "*InvoiceInfo.groovy" // jasper file, must have public members
    }
    NonFinalSubclassOfSensitiveInterface
    ObjectFinalize
    PublicFinalizeMethod
    SystemExit
    UnsafeArrayDeclaration

    // rulesets/serialization.xml
    EnumCustomSerializationIgnored
    SerialPersistentFields
//    SerialVersionUID
    SerializableClassMustDefineSerialVersionUID

    // rulesets/size.xml
//    AbcComplexity   // DEPRECATED: Use the AbcMetric rule instead. Requires the GMetrics jar
//    AbcMetric   // Requires the GMetrics jar
//    ClassSize
//    CrapMetric   // Requires the GMetrics jar and a Cobertura coverage file
//    CyclomaticComplexity   // Requires the GMetrics jar
//    MethodCount // FLN add later
//    MethodSize // FLN add later
//    NestedBlockDepth
//    ParameterCount

    // rulesets/unnecessary.xml
    AddEmptyString
    ConsecutiveLiteralAppends
    ConsecutiveStringConcatenation
    UnnecessaryBigDecimalInstantiation
    UnnecessaryBigIntegerInstantiation
    UnnecessaryBooleanExpression
    UnnecessaryBooleanInstantiation
    UnnecessaryCallForLastElement
    UnnecessaryCallToSubstring
    UnnecessaryCast
    UnnecessaryCatchBlock
    UnnecessaryCollectCall
    UnnecessaryCollectionCall
    UnnecessaryConstructor
    UnnecessaryDefInFieldDeclaration
//    UnnecessaryDefInMethodDeclaration
    UnnecessaryDefInVariableDeclaration
    UnnecessaryDotClass
    UnnecessaryDoubleInstantiation
    UnnecessaryElseStatement
    UnnecessaryFinalOnPrivateMethod
    UnnecessaryFloatInstantiation
//    UnnecessaryGString
//    UnnecessaryGetter
    UnnecessaryIfStatement
    UnnecessaryInstanceOfCheck
    UnnecessaryInstantiationToGetClass
    UnnecessaryIntegerInstantiation
    UnnecessaryLongInstantiation
    UnnecessaryModOne
    UnnecessaryNullCheck
    UnnecessaryNullCheckBeforeInstanceOf
//    UnnecessaryObjectReferences // FLN maybe later
    UnnecessaryOverridingMethod {
        // used for documentation annotations
        doNotApplyToFileNames = "ApiProductController.groovy, ApiPersonController.groovy, ApiMenuItemController.groovy, ApiCustomItemController.groovy," +
                "ApiEventController.groovy, ApiServiceItemController.groovy"
    }
    UnnecessaryPackageReference {
        doNotApplyToFileNames = "NeedsAdminRole.groovy, NeedsUserRole.groovy, NeedsUserFeature.groovy, DataPointReadOnlyAction.groovy, Currency.groovy"
    }
    UnnecessaryParenthesesForMethodCallWithClosure
    UnnecessaryPublicModifier {
        doNotApplyToFileNames = "*InvoiceInfo.groovy" // jasper file, must have public members
    }
//    UnnecessaryReturnKeyword
    UnnecessarySafeNavigationOperator
    UnnecessarySelfAssignment
    UnnecessarySemicolon
    UnnecessaryStringInstantiation
//    UnnecessarySubstring
    UnnecessaryTernaryExpression
    UnnecessaryToString
    UnnecessaryTransientModifier

    // rulesets/unused.xml
    UnusedArray
    // UnusedMethodParameter // deactivate because the alternativ is to add @SuppressWarning("UnusedMethodParameter") all over the code
    UnusedObject
    UnusedPrivateField
    UnusedPrivateMethod {
        doNotApplyToFileNames = "ScoringService.groovy"
    }
    UnusedPrivateMethodParameter {
        doNotApplyToFileNames = "ScoringService.groovy"
    }
    UnusedVariable

    // new rules 1.0
//    CouldBeSwitchStatement
//    UnnecessarySetter

    // new rules 1.1
//    MissingOverrideAnnotation
//    Indentation
    InvertedCondition
//    MethodReturnTypeRequired
//    MethodParameterTypeRequired
//    FieldTypeRequired
//    VariableTypeRequired
}
