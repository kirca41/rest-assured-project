package mk.ukim.finki.rest_assured_project.web.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import mk.ukim.finki.rest_assured_project.model.dtos.BookAddDto
import mk.ukim.finki.rest_assured_project.model.dtos.BookEditDto
import mk.ukim.finki.rest_assured_project.repository.BookRepository

class UniqueIsbnValidator(
    private val bookRepository: BookRepository
) : ConstraintValidator<UniqueIsbn, Any> {

    override fun isValid(bookDto: Any, context: ConstraintValidatorContext): Boolean {
        return when (bookDto) {
            is BookAddDto -> {
                !this.bookRepository.existsByIsbn(bookDto.isbn)
            }
            is BookEditDto -> {
                this.bookRepository.findByIsbn(bookDto.isbn)?.let { it.id == bookDto.id } ?: true
            }
            else -> {
                false
            }
        }
    }
}