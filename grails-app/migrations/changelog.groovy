import grails.util.Holders
import org.grails.io.support.PathMatchingResourcePatternResolver
import org.grails.io.support.Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main changelog file for liquibase, will executes everything within "automated" folders in both current and next version folders
 * If you need more information, you can also check the table 'DATABASECHANGELOG' in the DB for infos about all migrations previously ran
 * ATTENTION!!!
 * Since sadly 'includeAll' is not working properly in the Liquibase version we are using on our servers (locally it worked fine) i implemented this workaround with generically adding 'include'
 * statements. Hopefully with Grails 6 and a newer version of Liquibase this will be fixed and we can use 'includeAll' again
 *
 * 1. Define paths for current and next version in which the hotfixes or feature migration scripts should be that need to be executed automatically
 * 2. iterate over both paths and try to find all .sql files in the folders
 * 3. add an 'include' statement for each found .sql file
 */

String appVersion = Holders.grailsApplication.config['info.app.version']
String[] versionParts = appVersion.split('\\.')
int major = Integer.valueOf(versionParts[0])
int minor = Integer.valueOf(versionParts[1])

// 1
List<String> migrationPaths = []
String currentPath = "${major}.${minor}/automated"  // hotfixes
String nextPath = "${major}.${minor + 1}/automated" // features
Logger logger = LoggerFactory.getLogger('liquibase')
// grails PathMatchingResourcePatternResolver was the only class with which i could successfully find all *.sql files in the folders
PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver()

// 2
[currentPath, nextPath].each { String rootPath ->
    if (!getClass().getResource(rootPath)) {
        logger.warn("changelog.groovy: Path '${rootPath}' does not exists, continue...")
        return
    }
    logger.info("changelog.groovy: Path '${rootPath}' exists, checking for migration files...")

    Resource[] resources = resolver.getResources("${rootPath}/*.sql")
    if (!resources) {
        logger.warn("changelog.groovy: No migration scripts found in '${rootPath}', continue...")
        return
    }

    resources.each {
        migrationPaths.add("${rootPath}/${it.filename}")
    }
}

if (!migrationPaths) {
    logger.warn("changelog.groovy: No migration scripts found in '${currentPath}' or '${nextPath}', continue...")
}

migrationPaths = migrationPaths.sort()

// 3
databaseChangeLog = {
    migrationPaths.each { String path ->
        logger.info("changelog.groovy: Adding migration script '${path}'")
        include file: path, relativeToChangelogFile: true, errorIfMissing: false
    }
}
