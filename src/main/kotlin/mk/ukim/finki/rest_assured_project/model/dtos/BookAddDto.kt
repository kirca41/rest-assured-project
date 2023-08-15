package mk.ukim.finki.rest_assured_project.model.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import mk.ukim.finki.rest_assured_project.model.enums.Category
import mk.ukim.finki.rest_assured_project.web.validation.UniqueIsbn
import mk.ukim.finki.rest_assured_project.web.validation.ValidIsbn

@UniqueIsbn
data class BookAddDto(
    @field:NotBlank(message = "Book ISBN must not be blank!")
    @ValidIsbn
    val isbn: String,
    @field:NotBlank(message = "Book title must not be blank!")
    val title: String,
    @field:Positive(message = "Book price must not be negative!")
    val price: Double,
    @field:Positive(message = "Book quantity in stock must not be negative!")
    val quantityInStock: Int,
    val category: Category,
    val authorId: Long
)
