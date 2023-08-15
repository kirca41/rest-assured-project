package mk.ukim.finki.rest_assured_project.web.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class IsbnValidator : ConstraintValidator<ValidIsbn, String> {

    override fun isValid(isbn: String, context: ConstraintValidatorContext): Boolean {
        return isValidIsbnFormat(isbn) && isValidIsbnChecksum(isbn)
    }

    private fun isValidIsbnFormat(isbn: String): Boolean {
        val isbn10Pattern = """^(?:\d{9}[\dXx])$""".toRegex()
        val isbn13Pattern = """^(?:\d{13})$""".toRegex()

        return isbn.matches(isbn10Pattern) || isbn.matches(isbn13Pattern)
    }

    private fun isValidIsbnChecksum(isbn: String): Boolean {
        if (isbn.length == 10) {
            return isValidIsbn10Checksum(isbn)
        } else if (isbn.length == 13) {
            return isValidIsbn13Checksum(isbn)
        }
        return false
    }

    private fun isValidIsbn10Checksum(isbn: String): Boolean {
        var sum = 0
        isbn.asSequence().forEachIndexed { idx, char ->
            if (char == 'X' || char == 'x') {
                sum += (idx + 1) * 10
            } else {
                sum += (idx + 1) * (char - '0')
            }
        }

        return sum % 11 == 0;
    }

    private fun isValidIsbn13Checksum(isbn: String): Boolean {
        var sum = 0
        isbn.asSequence().forEachIndexed { idx, char ->
            if (idx % 2 == 0) {
                sum += char - '0'
            } else {
                sum += (char - '0') * 3
            }
        }

        return sum % 10 == 0;
    }
}