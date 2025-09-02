package uberall.listing

enum FileExtension {

    // Video file extensions
    ASF('asf'),
    AVI('avi'),
    DAT('dat'),
    DIVX('divx'),
    DV('dv'),
    F4V('f4v'),
    FLV('flv'),
    M2TS('m2ts'),
    M4V('m4v'),
    MKV('mkv'),
    MOD('mod'),
    MOV('mov'),
    MP4('mp4'),
    MPE('mpe'),
    MPEG('mpeg'),
    MPEG4('mpeg4'),
    MPG('mpg'),
    MTS('mts'),
    NSV('nsv'),
    OGM('ogm'),
    OGV('ogv'),
    QT('qt'),
    THREE_G2('3g2'),
    THREE_GP('3gp'),
    THREE_GPP('3gpp'),
    TOD('tod'),
    TS('ts'),
    VOB('vob'),
    WMV('wmv'),

    // Image file extensions
    JPG("jpg"),
    JPEG("jpeg"),
    TIFF("tiff"),
    PNG("png"),
    BMP("bmp"),

    // Shared file extensions
    GIF('gif')

    private final String extension

    private FileExtension(String extension) {
        this.extension = extension
    }

    String getExtension() {
        this.extension
    }

    static List<FileExtension> getLocationVideoExtensions() {
        [MP4]
    }

    static List<FileExtension> getImageFileExtensions() {
        [JPG, JPEG, TIFF, GIF, PNG, BMP]
    }

    static FileExtension findByExtension(String extension) {
        values().find { it.extension == extension }
    }
}
