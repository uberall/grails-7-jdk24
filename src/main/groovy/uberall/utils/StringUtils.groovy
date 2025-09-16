package uberall.utils

//import com.uberall.commons.listing.DirectoryField
import com.vdurmont.emoji.EmojiManager
import com.vdurmont.emoji.EmojiParser
import grails.converters.JSON
import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.text.StringEscapeUtils

import javax.annotation.Nullable
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder
import java.nio.charset.CoderResult
import java.nio.charset.CodingErrorAction
import java.text.Normalizer
import java.util.function.Function
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@Slf4j
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
class StringUtils {

    private static final String NUMERIC = "0-9"
    static final String UNICODE_LETTERS = /\p{L}/
    static final String UNICODE_LETTERS_DIGITS = /[\p{L}\d]/
    private static final String BRACKETS = /\(\)\[\]/
    private static final String REGEX_RESERVED_CHARS = /[.+*?^$()\[\]{}|\\]/
    private static final String REGEX_UUID_PASSWORD_PLACEHOLDER = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
    private static final String UNICODE_COMBINING_CHARACTERS = /\u0300-\u036F/ // DM-4948 - Main combining character Unicode block
    private static final String TH_SPECIAL_CHARS = /\u0E00-\u0E7F/ // UB-31480
    private static final String JP_SPECIAL_CHARS = /\u30FB\u25CF\u25B2\u203B\u2015\u2026\u201C\u201D\u3000-\u303F\uFF00-\uFFEF/ // UB-31626
    private static final String LANGUAGE_SPECIAL_CHARACTERS = /${JP_SPECIAL_CHARS}${TH_SPECIAL_CHARS}/
    // Note: if you edit this regex, you also have to adapt the regex in this message key: `uberdoc.object.location.descriptionlong.description`
    private static final String ALLOWED_SPECIAL_CHARS = / \|\?:;\/!\\,\.\-%\\&=\r\n\t_\*#§²³`´·"'\+¡¿@°€£\•\^\u0024\u266A/
    private static final String SPECIAL_CHAR_REGEXP = /[^${NUMERIC}${UNICODE_LETTERS}${BRACKETS}${UNICODE_COMBINING_CHARACTERS}${ALLOWED_SPECIAL_CHARS}${LANGUAGE_SPECIAL_CHARACTERS}]+/
    private static final Map<String, String> REPLACEMENT_MAP = [
            "\u2013": "-",  // special hyphen from word
            "\u2014": "-",  // special hyphen from word
            "\u2212": "-",  // special hyphen
            "\u2026": "...", // three dots
            "\u00AD": "-", // soft hyphen
            "\u00A0": " ", // the non-breaking space
            "\u0009": " ", // tab
            "\u201C": '"', // quote
            "\u201D": '"', // quote
            "\u201E": '"', // quote
            "с"     : "c", // a weird 'c' with ASCII code 1089
            "\u2019": "'", // RIGHT SINGLE QUOTATION MARK
            "\u2018": "'", // LEFT SINGLE QUOTATION MARK
            "\u2028": "\n",// LINE SEPARATOR
            "&#10;": "\n", // line-feed  (hex)-> LINE SEPARATOR
            "&#13;": "\n", // carriage-return (hex) -> LINE SEPARATOR
            "&#xa;": "\n", // line-feed (hex) -> LINE SEPARATOR
            "&#xa0;": " ", // non-breaking-space (hex) -> SPACE
            "\r":     "", // \r is not supported
    ]

    private static final String BLANK_CHARS_REGEX = /[\u0000-\u001F\u007F\u00A0\u180E\u2000\u200A\u202F\u205F\u3000\uFEFF]/

    final static String MESSAGE_KEY_FORMAT = /\w+(\.\w+)+/

    /**
     * Regex to match emoji variation selectors (`U+FE0F`).
     *
     * https://emojipedia.org/variation-selector-16/
     * https://github.com/vdurmont/emoji-java/issues/186#issuecomment-1211710823
     */
    private static final String EMOJI_VARIATION_SELECTOR_REGEX = /[️️]/

    /**
     * Regex to detect one or more consecutive non-standard Unicode characters, primarily emojis.
     *
     * Limitations:
     * - The `com.vdurmont:emoji-java` library (latest: v5.1.1) only supports Unicode 11 emojis.
     * - Emojis introduced in Unicode 12+ may not be detected by this library.
     * - This pattern does **not** detect emojis introduced **before Unicode 7.0** to avoid false positives (e.g., special characters like `°`).
     * - This regex should be used **in conjunction with** `com.vdurmont:emoji-java` for more accurate emoji detection
     *
     * Matches characters **not** belonging to:
     * - `\p{L}` → Letters (all languages)
     * - `\p{M}` → Combining marks (e.g., diacritics)
     * - `\p{N}` → Numbers (digits and numeric symbols)
     * - `\p{P}` → Punctuation marks
     * - `\p{Z}` → Space separators
     * - `\p{S}` → Symbols (currency, math, etc.)
     * - `\p{C}` → Other characters (control, format, etc.)
     */
    private static final  String EMOJI_DETECTION_REGEX = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{S}\\p{C}]+"

    /**
     * Perform a soft cleanup of the strings (eg. trimming and replacing different unicode whitespaces with a consistent one).
     * It's not meant to be considered normalization. This method modifies the object.
     */
//    static void clear(obj) {
//        // list of directory fields that should be cleared
//        // for now we don't want to clear descriptions, as they may contain multiple whitespaces and \n on purpose
//        def toClear = DirectoryField.stringProperties() - [DirectoryField.DESCRIPTION_SHORT, DirectoryField.DESCRIPTION_LONG, DirectoryField.IMPRINT, DirectoryField.OPENINGHOURS_NOTES]
//
//        toClear.each {
//            if (obj[it.propertyName] != null) {
//                // has property and its value is not null
//                obj[it.propertyName] = removeExtraWhitespaces(obj[it.propertyName] as String)
//                obj[it.propertyName] = obj[it.propertyName] ?: null // resetting empty strings to null
//            }
//        }
//    }

    /**
     * Removes the extra whitespaces
     * "Location  Name " -> "Location Name"
     *
     * '\p{Z}' matches ANY kind of whitespace character, but no newlines/carriage return
     * '\s' matches newline/carriage return
     * '[\p{Z}\s]+' will match one or more consecutive whitespace characters and newline/carriage return
     *
     * Unicode has different codes for normal whitespaces and non-breaking whitespaces (eg. '&nbsp;')
     * usually found when scraping content (http://www.regular-expressions.info/unicode.html)
     */
    static String removeExtraWhitespaces(String s) {
        s ? s.replaceAll(/[\p{Z}\s]+/, ' ').trim() : s
    }

    /**
     * Removes everything that is not an unicode char, a space or a dot from the specified String.
     *
     * @param string the input string
     * @return a String that contains unicode letters and spaces only
     */
    static String removeNonUnicodeChar(String string) {
        return string ? string.replaceAll(/[^\p{L}\s.]/, "").trim() : string
    }

    /**
     * Removes all emojis from the given String.
     *
     * @param string the input string
     * @return a String that does not contain any emoji
     */
    static String removeEmojis(String string) {
        if (!string) {
            return string
        }
        String nonEmojiString = EmojiParser.removeAllEmojis(string).replaceAll(EMOJI_VARIATION_SELECTOR_REGEX, "")
        // Extra check as this Emoji library does not find every emoji especially if they are from newer version of unicode.
        // e.g. 'Star-Struck' (Codepoints: U+1F929).
        return nonEmojiString.replaceAll(EMOJI_DETECTION_REGEX, "").trim()
    }

    /**
     * Replacing consecutive whitespaces with single one.
     *
     * @param string the input string
     * @return a String that does not contain any consecutive whitespaces or else empty string
     */
    static String removeConsecutiveWhitespaces(String string) {
        return string ? string.trim().replaceAll(" +", " ") : ""
    }

    /**
     * - replaces characters like "\n", "\t" to simple whitespace " "
     * - removes whitespaces at the end or start of the text
     * - removes consecutive whitespaces in the text
     */
    protected static String replaceWhitespaces(String text) {
        text ? text.trim().replaceAll("\\s+", " ") : text
    }

    static boolean isUUID(String text) {
        if (!text) {
            return false
        }
        Matcher matcher = text =~ /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
        return matcher.matches()
    }

    /**
     * Tokenize the given String.
     *
     * "Favorite Product GmBH" -> ['favorite', 'product', 'gmbh']
     * "Favorite-Product GmBH" -> ['favorite', 'product', 'gmbh']
     * "Favorite/Product GmBH" -> ['favorite', 'product', 'gmbh']
     * "" -> []
     * null -> []
     *
     * @param string
     * @return list of string tokens
     */
    static List<String> getTokens(String s) {
        if (!s) {
            return []
        }

        s.replaceAll(/(?i)[\p{S}\p{P}\p{Z}]/, ' ').replaceAll(/\s{2,}/, ' ').trim().split(' ')*.toLowerCase()
    }

    @CompileDynamic
    static List<String> parseCommaSeparatedValues(def object) {
        List<String> list = []
        if (object in List || object?.class?.isArray() || object in Set) {
            list = object as List
        } else if (object in String && !object.isEmpty()) {
            if (object.contains(',')) {
                list = object.split(',')
            } else {
                list = object.split(';')
            }
        }

        if (list) {
            list = getCleanedupList(list)
        }
        list
    }

    /**
     * Used for location lists like keywords, services etc to make sure they don't have duplicates
     * assert getCleanedupList(["Foo", "FOO", "Foo "]) == ["Foo"]
     */
    @CompileDynamic
    protected static List<String> getCleanedupList(Collection<String> strings) {
        List<String> tempResults = []
        strings?.grep()?.each { String keyword ->
            // as we might got a list of list (happens for some SP's) we are splitting the single keywords again, this could be extended in the future
            List<String> newWords = keyword.split(',') as List<String>
            // take everything that is not null or empty from the list, trim it and put it into the temp list
            tempResults.addAll(newWords.grep()*.trim() as List)
        }

        // make sure we don't have duplicate keywords with different cases in the set and return the result
        return tempResults.unique { String a, String b -> a.toLowerCase() == b.toLowerCase() ? 0 : 1 }
    }

    /**
     * Splits the given String by space while taking care of quotes.
     * @param s the input string
     * @return a list of Strings
     */
    @CompileDynamic
    static List<String> split(String s) {
        def list = (s =~ /[^\s\p{Z}"]+|"[^"]*"/)
        list*.replaceAll('"', '')
    }

    /**
     * Café --> cafe, Ärzte --> arzte etc
     * @param text
     * @return
     */
    static String clearSymbols(String text, boolean useUmlauts = false) {
        if (!text) {
            return text
        }

        String baseText = Normalizer.normalize(text, Normalizer.Form.NFD)
        // https://www.regular-expressions.info/unicode.html - regex symbol groups doc
        def regex = '''(?xi)    # extended mode, allows regexp to be written this way and have comments
                       [        # match everything that is:
                        \\p{M}  # a mark (diacritics...)
                        \\p{S}  # a symbol (currencies...)
                       ]
                    '''

        if (useUmlauts) {
            baseText = text
        }

        return baseText.toLowerCase().replaceAll(regex, '').replaceAll("\\p{P}", ' ').replaceAll('ß', 'ss').replaceAll('  ', ' ').toString().trim()
    }

    /**
     * Ignores case.
     *
     * @param found
     * @param reference
     * @return
     */
    static boolean compareSimple(String found, String reference) {
        if (!found && !reference) {
            return true // both not present, which is fine
        }
        return found && reference ? cleanBlankChars(found.toLowerCase()) == cleanBlankChars(reference.toLowerCase()) : false
    }

    /**
     * Does not ignore case.
     *
     * @param found
     * @param reference
     * @return
     */
    static boolean compareSimpleCaseSensitive(String found, String reference) {
        if (!found && !reference) {
            return true // both not present, which is fine
        }
        return found && reference ? found.trim() == reference.trim() : false
    }

    /**
     *
     * @param found
     * @param reference
     * @return
     */
    static boolean compareSimpleIgnoreWhitespace(String found, String reference) {
        if (!found && !reference) {
            return true // both not present, which is fine
        }
        return found && reference ?
                found.replaceAll(' ', '').toLowerCase().contains(reference.replaceAll(' ', '').toLowerCase()) ||
                        reference.replaceAll(' ', '').toLowerCase().contains(found.replaceAll(' ', '').toLowerCase()) : false
    }

    /**
     * Compare case sensitive but ignore whitespace
     *
     * @param found
     * @param reference
     * @return
     */
    static boolean compareSimpleCaseSensitiveIgnoreWhitespace(String found, String reference) {
        if (!found && !reference) {
            return true // both not present, which is fine
        }
        return found && reference ? found.replaceAll(' ', '').contains(reference.replaceAll(' ', '')) ||
                reference.replaceAll(' ', '').contains(found.replaceAll(' ', '')) : false
    }

    /**
     *
     * @param found
     * @param reference
     * @return
     */
    static boolean compareSimpleIgnoreWhitespaceAndComma(String found, String reference) {
        if (!found && !reference) {
            return true // both not present, which is fine
        }

        String foundCleaned = found?.replaceAll(' ', '')?.replaceAll(',', '')?.toLowerCase()
        String referenceCleaned = reference?.replaceAll(' ', '')?.replaceAll(',', '')?.toLowerCase()

        return (found && reference) ? (foundCleaned.contains(referenceCleaned) || referenceCleaned.contains(foundCleaned)) : false
    }

    static boolean compareIgnoreNonLetterChars(String found, String reference) {
        if (!found && !reference) {
            return true // both not present, which is fine
        }
        String foundCleaned = cleanNonLetterChars(found)
        String referenceCleaned = cleanNonLetterChars(reference)

        return (found && reference) ? foundCleaned == referenceCleaned : false
    }

    static String cleanNonLetterChars(String input) {
        return input?.replaceAll("[^a-zA-Z]", "")?.toLowerCase()
    }

    /**
     *
     * @param s
     * @return
     */
    static String convertGermanChars(String s) {
        s ? s.replaceAll('ä', 'ae').replaceAll('ö', 'oe').replaceAll('ü', 'ue').replaceAll('ß', 'ss') : s
    }

    /**
     * Keeps emojis. Replaces specialChars with known ones. Removes all other special chars
     * @param description
     * @return
     */
    static String cleanSpecialCharsForDescription(String description, List<String> charsToRemove = []) {
        if (!description) { return description }

        List<String> descriptionWords = description.split(" ") as List
        descriptionWords = descriptionWords.collect {
            if (containsEmoji(it)) { it }
            else {
                it = replaceKnownSpecialChars(it)
                Matcher matcher = Pattern.compile(SPECIAL_CHAR_REGEXP).matcher(it)
                it = matcher.replaceAll("")
                it
            }
        }

        String result = descriptionWords.join(" ")

        charsToRemove.each { String charToRemove ->
            result = result.replaceAll(charToRemove, '')
        }

        return result
    }

    private static boolean containsEmoji(String input) {
        return EmojiManager.containsEmoji(input) ||
                input.matches(EMOJI_DETECTION_REGEX) ||
                input.matches(EMOJI_VARIATION_SELECTOR_REGEX)
    }

    /**
     * Replace known special chars with expected ones ${REPLACEMENT_MAP}
     * Cleans up all the rest. Deletes everything that is not in ${SPECIAL_CHAR_REGEXP}
     *
     * NOTE: Doesn't support Emojis. This method will remove them!
     */
    static String cleanSpecialChars(String text) {
        if (!text) {
            return text
        }

        String cleanText = replaceKnownSpecialChars(text)

        // removing the rest
        Matcher matcher = Pattern.compile(SPECIAL_CHAR_REGEXP).matcher(cleanText)
        cleanText = matcher.replaceAll("")

        return cleanText
    }

    /**
     * Replace known special chars with expected ones ${REPLACEMENT_MAP}
     */
    static String replaceKnownSpecialChars(String text) {
        if (!text) {
            return text
        }

        String cleanText = text
        REPLACEMENT_MAP.each { specialChar, replacement ->
            cleanText = cleanText.replaceAll(specialChar, replacement)
        }

        return cleanText
    }

    static List<String> cleanTrimStringCollection(Iterable<String> values) {
        values.collect { String value -> removeExtraWhitespaces(cleanSpecialChars(value)) }
    }

    static String removeRegexReservedChars(String text) {
        if (!text) {
            return text
        }

        // removing regex reserved characters
        return text.replaceAll(REGEX_RESERVED_CHARS, '')
    }

    static String cleanBlankChars(String text) {
        if (!text) {
            return text
        }
        // remove blank special characters and trim whitespaces
        text.replaceAll(BLANK_CHARS_REGEX, '').trim()
    }

    /**
     * Removes all html elements from the given string , e.g. "<div>foobar</div>" --> foobar
     * @param data
     * @return
     */
    static String removeHtmlElements(String html) {
        String result = html
        if (result) {
            result = result.replaceAll("<[^>]*>", "")
            result = result.replaceAll("\\u00a0", " ")  // change nbsp to normal space
            result = result.replaceAll("&apos;", "'")
            // use commons-text implementation (unescapeHtml4) instead of deprecated commons-lang3 variant
            result = StringEscapeUtils.unescapeHtml4(result)
        }
        return result?.trim()
    }

    /**
     * Removes all Brackets from a String, e.g. (030) {123} [456] --> 030 123 456
     * @return
     */
    static String removeBrackets(String text) {
        String textWithoutBrackets = text
        if (textWithoutBrackets) {
            "()[]{}".each {
                textWithoutBrackets = textWithoutBrackets - it
            }
        }
        textWithoutBrackets
    }

    /**
     * Removes all diacritics from a String, e.g. "éôüêùéàçÍËÇ" --> "eoueueacIEC"
     * @param text the string to remove diacritics from
     * @return string without diacritics
     */
    static String removeDiacritics(String text) {
        if (!text) {
            return ''
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll(/\p{InCombiningDiacriticalMarks}+/, '')
    }

    static String sanitizePossibleZalgoString(String text) {
        if (text.matches(".*\\p{Mn}{4}.*")) {
            return removeDiacritics(text)
        }
        return text
    }

    /**
     * returns the containing special character if the given text contains one, null otherwise
     * @param text
     * @return
     */
    static String extractSpecialChars(String text) {
        if (!text) {
            return null
        }
        Matcher matcher = Pattern.compile(SPECIAL_CHAR_REGEXP).matcher(text)
        if (matcher.find()) {
            String group = matcher.group(0)
            return group.size() > 1 ? group.substring(0, 1) : group
        }
        return null
    }

    /**
     *
     * @param text
     * @return
     */
    static List<String> extractAllSpecialChars(String text) {
        if (!text) {
            return []
        }

        def allowedPatterns = [
                Pattern.compile("[0-9]+", Pattern.CASE_INSENSITIVE), // same as NUMERIC
                Pattern.compile(UNICODE_LETTERS, Pattern.CASE_INSENSITIVE),
                Pattern.compile("[]\\(\\)\\[\\]]+", Pattern.CASE_INSENSITIVE), // same as BRACKETS
                Pattern.compile("[]\\?:;\\/!\\\\,\\.\\-%\\\\&\\r\\n\\t_\\*§²`´·\"'\\+¡¿@]+", Pattern.CASE_INSENSITIVE) // same as ALLOWED_SPECIAL_CHARS
        ]

        String cleanText = text
        allowedPatterns.each {
            Matcher matcher = it.matcher(cleanText)
            cleanText = matcher.replaceAll("")
            // replace any characters within text that matches the (allowed) pattern for blank. whatever is left, is a non-allowed char
        }

        return (cleanText.split("") as List<String>).findAll { String el ->
            el.trim() && !(el.trim() in [" ", ",", "."])
        }
    }

    static String keepOnlyAlphanumericChars(String text) {
        text.replaceAll('[^a-zA-Z0-9]', '')
    }

    /**
     *
     * @param input
     * @return
     */
    static String decapitalize(String input) {
        if (!input) {
            return input
        }

        return new StringBuffer(input.size())
                    .append(Character.toLowerCase(input.charAt(0)))
                    .append(input.substring(1))
                    .toString()
    }

    static String capitalize(String input) {
        org.apache.commons.lang3.StringUtils.capitalize(input)
    }

    /**
     * Returns string with words exceeding maxLength eliminated
     *
     * assert cutExcessWords("one two three", 10) == "one two"
     * assert cutExcessWords("one two three", 20) == "one two three"
     * assert cutExcessWords("one,two,three", 10, ",") == "one,two"
     * assert cutExcessWords(null, 5) == ''
     * assert cutExcessWords(null, 5) == ''
     *
     * @param string
     * @param maxLength
     * @return
     */
    static String cutExcessWords(String string, int maxLength, String splitChar = ' ') {
        if (!string) {
            return ''
        } else if (string.length() <= maxLength) {
            return string
        }

        if (Character.toString(string.charAt(maxLength)) == splitChar) {
            return string.substring(0, maxLength)
        }

        def lastIndexOfSpace = string.substring(0, maxLength).lastIndexOf(splitChar)
        lastIndexOfSpace = lastIndexOfSpace > 0 ? lastIndexOfSpace : maxLength

        return string.substring(0, lastIndexOfSpace)
    }

    /**
     *
     * @param s
     * @return
     */
    static Boolean isNotBlank(String s) {
        org.apache.commons.lang3.StringUtils.isNotBlank(s)
    }

    /**
     *
     * @param s
     * @return
     */
    static Boolean isBlank(String s) {
        org.apache.commons.lang3.StringUtils.isBlank(s)
    }

    /**
     * Removes the underscore in a string, capitalizes non-first words, adds an s
     *
     * assert underscoreToListProperty("FOO_BAR") == "fooBars"
     *
     * @param underscore
     * @return
     */
    @CompileDynamic
    static String underscoreToListProperty(String underscore) {
        underscoreToCamelCase(underscore, false) + 's'
    }

    /**
     * Removes the underscore in a string and capitalize the first letter of each word.
     * Capitalization of first char is optional
     *
     * assert underscoreToCamelCase("FOO_BAR") == "FooBar"
     * assert underscoreToCamelCase("foo_bar", false) == "fooBar"
     *
     * @param underscore
     * @return
     */
    @CompileDynamic
    static String underscoreToCamelCase(String underscore, boolean capitalize = true) {
        if (!underscore || underscore.isAllWhitespace()) {
            return null
        }

        String result = underscore.toLowerCase().replaceAll(/_\w/) { it[1].toUpperCase() }

        if (capitalize) {
            return result.capitalize()
        }

        return result
    }

    /**
     * Splits a String on Capital letters, introduces an underscore between them and
     * returns the string in lower cases per default
     *
     * assert camelCaseToUnderscore("FooBar") == "foo_bar"
     * assert camelCaseToUnderscore("FooBar", false) == "FOO_BAR"
     *
     * @param A non empty String
     * @return
     */
    static String camelCaseToUnderscore(String input, boolean lowerCased = true) {
        if (!input || input.isAllWhitespace()) {
            return null
        }

        String result = input.split("(?=\\p{Upper})").join("_")
        if (lowerCased) {
            return result.toLowerCase()
        }

        return result.toUpperCase()
    }

    /**
     * Accepts inputs with spaces, underscores and camelCase
     * @return the input string in snake-case
     */
    static String toSnakeCase(String input) {
        if (input.contains(' ') || input.contains('_')) {
            return input.replaceAll(/([a-zA-Z])(\s|_)([a-zA-Z])/, '$1-$3').toLowerCase()
        }

        return input.replaceAll(/([A-Z])/, /-$1/).toLowerCase().replaceAll(/^-/, '')
    }

    /**
     * Parses a String and returns an enum of the given type that matches the parsed string.
     * If the value is null or no matching enum exists, null is returned.
     *
     * @param enumType the enum class
     * @param value the value to parse
     * @return an enum type or null if the value cannot be parsed
     */
    static <T extends Enum<T>> T getEnumValue(Class<T> enumType, value, T defaultValue = null) {
        if (!value || value == 'null') {
            // don't go any further if the value smells like null
            return defaultValue
        }
        if (value.class == enumType) {
            // already the right type, no need to convert
            return (T) value
        }

        try {
            return Enum.valueOf(enumType, value.toString().trim().toUpperCase())
        } catch (IllegalArgumentException ignore) {
            return defaultValue
        }
    }

    /**
     * Parses a String and returns an enum of the given type that matches the parsed string.
     * If the value is null or no matching enum exists, null is returned.
     *
     * Note: This is alternative version of getEnumValue that does not convert the value to uppercase.
     * Some enumerations are defined by external APIs and specifications contain  lowercase values
     * (e.g. 'IEC_60309_2_single_16'). The existing getEnumValue method already contains an optional
     * parameter making it difficult to modify the original method without verifying 500+ existing
     * usages.
     *
     * @param enumType the enum class
     * @param value the value to parse
     * @return an enum type or null if the value cannot be parsed
     */
    static <T extends Enum<T>> T getEnumValueCaseSensitive(Class<T> enumType, value, T defaultValue = null) {
        if (!value || value == 'null') {
            // don't go any further if the value smells like null
            return defaultValue
        }
        if (value.class == enumType) {
            // already the right type, no need to convert
            return (T) value
        }

        try {
            return Enum.valueOf(enumType, value.toString().trim())
        } catch (IllegalArgumentException ignore) {
            return defaultValue
        }
    }

    /**
     * Parses a List of String and returns an enum of the given type that matches the parsed string.
     *
     * @param enumType the enum class
     * @param values the values to parse
     * @return an enum type or null if the value cannot be parsed
     */
    static <T extends Enum<T>> List<T> getEnumValues(Class<T> enumType, values, T defaultValue = null) {
        if (!values || values == 'null') {
            // don't go any further if the value smells like null
            return []
        }
        return values.collect { getEnumValue(enumType, it, defaultValue) }
    }

    static List removeDuplicatesNotConsideringDiacritics(List<String> list) {
        if (!list) {
            return list
        }

        //Removing duplicates like Cafe and Café
        List<String> listWithoutDiacritics = []
        List<String> listToRemove = []
        String keyword

        list.each { String el ->
            keyword = removeDiacritics(el)
            if (listWithoutDiacritics && listWithoutDiacritics.contains(keyword)) {
                listToRemove.add(el)
            } else {
                listWithoutDiacritics.add(keyword)
            }
        }

        if (listToRemove) {
            list.removeAll(listToRemove)
        }

        return list
    }

    static String replaceLast(String text, String regex, String replacement) {
        if (text == null || regex == null || replacement == null) {
            return null
        }
        return text.replaceFirst('(?s)(.*)' + regex.replace("+", "\\+").replace(")", "\\)"), '$1' + replacement)
    }

    static String extractWordsAndNumbers(String text) {
        if (!text) {
            return text
        }

        text.split("[^A-Za-z0-9]").join(' ')
    }

    static String convertToISO88591(String text) {
        if (text == null) {
            return null
        }

        CharsetEncoder encoder = Charset.forName('ISO-8859-1').newEncoder()
        encoder.onMalformedInput(CodingErrorAction.REPORT)
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT)

        CharBuffer temp = CharBuffer.wrap(text.toCharArray())
        // let's allocate more than we need, UTF-8 encoding can use up to 4 bytes per character.
        // while normalizing the letters, a single character might end up being converted to 3, so in the worst case char length can be increased up to 3 times
        ByteBuffer outBuffer = ByteBuffer.allocate(temp.size() * 12)
        char badCharacter

        boolean endOfInput = false
        String encodedText = text
        while (!endOfInput) {
            CoderResult result = encoder.encode(temp, outBuffer, endOfInput)
            if (result.isUnmappable()) {
                badCharacter = temp.get()
                outBuffer.put(clearSymbols(badCharacter.toString()).bytes)
            } else if (result.isUnderflow()) {
                endOfInput = true
                encoder.encode(temp, outBuffer, endOfInput)
                encoder.flush(outBuffer)
                encodedText = new String(outBuffer.array(), Charset.forName('ISO-8859-1'))
            } else {
                // Irrecoverable error Malformed or Overflow problem: http://docs.oracle.com/javase/6/docs/api/java/nio/charset/CharsetEncoder.html
                endOfInput = true
            }
        }

        return encodedText?.replaceAll('\u0000', '')
    }

    static String convertSecondsToHumanReadable(long seconds) {
        if (seconds == 0L) {
            return "0"
        }
        if (seconds < 0) {
            return "N/A"
        }
        Map<String, Integer> intervals = [
                days   : ((int) (seconds / 60 / 60 / 24)),
                hours  : ((int) (seconds / 60 / 60)) % 24,
                minutes: ((int) (seconds / 60)) % 60,
                seconds: ((int) (seconds)) % 60
        ]
        intervals.collect { k, v -> v ? "$v $k" : null }.grep().join(", ")
    }

    @CompileDynamic
    static String remove4ByteChars(String text) {
        text?.replaceAll(/./) {
            char c = it.toString().charAt(0)
            Character.isHighSurrogate(c) || Character.isLowSurrogate(c) ? '' : c
        }
    }

    static String removeOneLetterWords(String text) {
        if (text == null) {
            return null
        }
        Collection<String> words = org.apache.commons.lang3.StringUtils.split(text) as List
        words.removeAll { it.size() == 1 }
        words.join(" ")
    }

    /**
     * Replaces all occurrences of the Bash strong quote ' by '\''.
     * Returns {@code null} if {@code string} is {@code null}.
     */
    static String escapeStrongBashQuotes(String string) {
        string?.replaceAll("'", "'\\\\''")
    }

    /**
     * Returns true if the given string contains at least one alphabetic character or digit.
     * Conversely, returns false if the string consists entirely of other symbols.
     */
    static boolean containsNotOnlySymbols(String s) {
        if (!s) {
            return false
        }
        // The Groovy .every method returns strings and not characters for 'it'.
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetterOrDigit((int) s.charAt(i))) {
                return true
            }
        }
        return false
    }

    /**
     * assert extractNameFromGoogleMailAddress("user+business@uberall.com") == "user"
     *
     * @param email the full email address
     * @return only the first part before any + and @
     */
    static String extractNameFromGoogleMailAddress(String email) {
        if (!email) {
            return null
        }
        int indexBeforeSigns = email.findIndexOf { it =~ /(\+|@)/ } - 1
        String name = email[0..indexBeforeSigns]
        return name
    }

    /**
     * Zip, and then base64 encode a string
     * @param s String to compress
     * @return base64 encoded, zipped version of string
     */
    static String zip(String s) {
        if (!s) {
            return
        }

        ByteArrayOutputStream targetStream = new ByteArrayOutputStream()
        GZIPOutputStream zipStream = new GZIPOutputStream(targetStream)
        zipStream.write(s.bytes)
        zipStream.close()

        byte[] bytes = targetStream.toByteArray()
        targetStream.close()

        return bytes.encodeBase64().toString()
    }

    /**
     * Decode a base64 encoded, zipped string
     * @param base64 encoded string
     * @return original string
     */
    static String unzip(String encoded) {
        byte[] zipped = encoded.decodeBase64()

        if (!zipped.length) {
            return
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(zipped)
        GZIPInputStream zipStream = new GZIPInputStream(inputStream)
        BufferedReader reader = zipStream.newReader()

        return reader?.text
    }

    /**
     * @param haystack string that will be searched
     * @param needle string to search for
     * @param caseMatch true means only correct case will match
     * @return Returns true only if the needle in a haystack is an exact match.
     */
    static boolean containsWholeWord(String haystack, String needle, boolean caseMatch = false) {
        if (!haystack || !needle) {
            return false
        }
        String checkCase = caseMatch ? "" : "(?i)"
        return haystack =~ /$checkCase(?<=_|\b)$needle(?=_|\b)/
    }

    /**
     * @param haystack string that will be searched
     * @param needles string to search for
     * @param caseMatch true means only correct case will match
     * @return Returns true only if the needle in a haystack is an exact match.
     */
    static boolean containsAllWholeWords(String haystack, List<String> needles, boolean caseMatch = false) {
        if (!haystack || !needles) {
            return false
        }

        boolean foundAll = true

        needles.each {
            if (foundAll && !containsWholeWord(haystack, it, caseMatch)) {
                foundAll = false
            }
        }

        return foundAll
    }

    /**
     * Escapes predefined entities such as (<,>,',",&) with escape strings
     * e.g. '&' -> '&amp;'
     * @param text
     * @return returns given string with escaped special chars (if chars can be found within string), otherwise the not touched string
     */
    static String escapePredefinedEntitiesForXml(String text) {
        String escapedText = text
        if (!escapedText) {
            return escapedText
        }

        // we need to escape the & sign first, otherwise we will escape it even within the replacements e.g. &apos; -> &amp;apos;
        escapedText = escapedText.replaceAll('&', '&amp;')
        escapedText = escapedText.replaceAll('"', '&quot;')
        escapedText = escapedText.replaceAll('\'', '&apos;')
        escapedText = escapedText.replaceAll('<', '&lt;')
        escapedText = escapedText.replaceAll('>', '&gt;')

        escapedText
    }

    /**
     * For a String in the format "McDonald's" will return the following tokens:
     * ["McDonald's","McDonalds","McDonald","s"]
     * Input Strings will be split on any non-word character (\W is the character class used to split)
     */
    static Set<String> getDistinctSpellingPermutations(List<String> strings) {
        strings.inject([] as Set<String>) { Set<String> results, String string ->
            results.add(string)
            def tokens = string.split("\\W").grep()
            if (tokens.size() > 1) {
                results.add(tokens.join(""))
                results.addAll(tokens)
            }
            results
        } as Set<String>
    }

    /**
     * Tells if the given string is Japanese
     * We consider a String as such if more than the given percentage of letters (after removing numbers and special chars e.g. space) of the chars are within the CJ characters
     * WARNING: this script is not sufficient to differentiate Chinese and Japanese, since the majority of characters overlap
     * Empty String or String with just numbers will be considered as not japanese
     * @param s
     * @param matchPercentage represents the minimum percentage at which caller of method deems the given String to be japanese (e.g. 0.6 for 60%)
     * @return
     */
    static boolean isJapanese(String s, Double matchPercentage) {
        if (!s) {
            return false
        }

        String cleaned = s.replaceAll('\\d|[\\- ,]', '') //first remove all normal numbers and 'special' chars, so ideally we have just letters/japanese characters left
        if (!cleaned) {
            return false
        }

        // Found at https://gist.github.com/ryanmcgrath/982242 and https://www.localizingjapan.com/blog/2012/01/20/regular-expressions-for-japanese-text/
        String regex = '[\\u3000-\\u303F]|' + // Japanese Symbols and Punctuation
                '[\\u3040-\\u309F]|' +  // Hiragana
                '[\\u30A0-\\u30FF]|' +  // Katakana
                '[\\uFF00-\\uFFEF]|' +  // Full-width roman + half-width katakana
                '[\\u4E00-\\u9FAF]|' +  // Common and uncommon kanji
                '[\\u2605-\\u2606]|' +  // Stars
                '[\\u2190-\\u2195]|' +  // Arrows
                '[\\u31F0-\\u31FF\\u3220-\\u3243\\u3280-\\u337F]|' + // Miscellaneous Japanese Symbols and Characters
                '[\\u2E80-\\u2FD5]|' +  // Kanji Radicals
                '\\u203B'  // Weird asterisk thing
        int japaneseCharCount = cleaned.toList().findAll { String singleChar -> singleChar.matches(regex) }?.size() ?: 0

        (japaneseCharCount / cleaned.size()) > matchPercentage
    }

    static boolean isHebrew(String s, Double matchPercentage) {
        if (!s) {
            return false
        }

        String cleaned = s.replaceAll('\\d|[\\- ,]', '')
        //first remove all normal numbers and 'special' chars, so ideally we have just letters/hebrew characters left
        if (!cleaned) {
            return false
        }

        String regex = Pattern.compile("\\p{InHebrew}", Pattern.UNICODE_CASE)
        int hebrewCharCount = cleaned.toList().findAll { String singleChar -> singleChar.matches(regex) }?.size() ?: 0

        (hebrewCharCount / cleaned.size()) >= matchPercentage
    }

    static boolean isLatin(String input) {
        if (!input) {
            return false
        }
        return Charset.forName("US-ASCII").newEncoder().canEncode(input)
    }

    /**
     * Replace multiple Strings with a single replacement String.
     * Does not repeat the replacement String more than once.
     *
     * @param input a String with signs you don't want
     * @param stringsToReplace a list of Strings to replace
     * @param replacement the String to replace
     * @return the String with replacements
     */
    static String replaceStringsWithoutRepetition(String input, List<String> stringsToReplace, String replacement) {
        if (!input) {
            return ""
        }

        // remove 'nothing' as that breaks the RegEx in case it's the only element
        stringsToReplace?.remove("")

        if (!stringsToReplace || !replacement) {
            return input
        }
        return input.replaceAll(/[${stringsToReplace.join("")}]/, "_").replaceAll("(_)\\1+", "_")
    }

    static String sanitizeIdentifier(String identifier) {
        if (identifier == null) {
            return null
        }
        StringUtils.cleanBlankChars(identifier.replaceAll('&#10;|&#13;|&#xa;|&#xa0;|\r|\n', ''))
    }

    static boolean isPlaceholderPassword(String password) {
        password ==~ REGEX_UUID_PASSWORD_PLACEHOLDER
    }

    static String generateEncodedBase64StringFromMap(Map map) {
        if (map == null) {
            return null
        }
        String json = (map as JSON)
        return Base64.encoder.encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8))
    }

    /**
     * Can be used for transforming single comma separated list to a list of a certain object via a specified method
     * Examples
     * collectFromSingleString("1,2,3", User.&get) == [User#1, User#2, User#3]
     * collectFromSingleString("1,2,3", Long.&parseLong) == [1, 2, 3]
     * collectFromSingleString("LOCATION_READ,LOCATION_WRITE", Feature.&valueOf) == [Feature.LOCATION_READ, Feature.LOCATION_WRITE]
     * collectFromSingleString('1,2,3') { it to Long } == [1, 2, 3]
     * @param rawString comma separated list
     * @param converter specifies to which object (and how) the rawString should be converted to
     * @return
     */
    static <T> List<T> collectFromSingleString(String rawString, Function<String, T> converter) {
        return rawString?.split(",")?.collect { T element ->
            try {
                return converter.apply(element.toString())
            } catch (ignore) { }

            return null
        }?.findAll() as List<T>
    }

    static boolean isMessageKey(String message) {
        message.matches(MESSAGE_KEY_FORMAT)
    }

    /**
     * Remove extra line breaks from the xml string without affecting element(s) textual values which have valid line breaks / carriage returns
     */
    static String removeExtraLineBreaksFromXmlString(String xmlString) {
        xmlString ? xmlString.replaceAll(Pattern.compile("(?<=\\>)(\\n+)|(\\n+)(?=\\<)"), "") : xmlString
    }

    static String convertAllFullwidthToHalfwidth(String input) {
        if (!input) {
            return ""
        }
        String output = ''

        for (char c : input.chars) {
            if (c >= 65281 && c <= 65374) {
                int halfwidthChar = (int) c - 65281 + 33
                output += (char) halfwidthChar
            } else {
                output += c
            }
        }

        return output
    }

    @Nullable
    static Class getDomainClass(String domainClassName) {
        try {
            return Holders.grailsApplication.getArtefacts('Domain').find { it.shortName == domainClassName }?.getClazz()
        } catch (e) {
            log.error("Could not find domain class $domainClassName", e)
        }

        return null
    }
}
