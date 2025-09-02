databaseChangeLog = {
    include file: 'changelog/000_create_tables.sql', relativeToChangelogFile: true, errorIfMissing: false
    include file: 'changelog/001_insert_locations.sql', relativeToChangelogFile: true, errorIfMissing: false
    include file: 'changelog/002_insert_listings.sql', relativeToChangelogFile: true, errorIfMissing: false
}
