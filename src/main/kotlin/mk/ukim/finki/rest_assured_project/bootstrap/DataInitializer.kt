package mk.ukim.finki.rest_assured_project.bootstrap

import jakarta.annotation.PostConstruct
import mk.ukim.finki.rest_assured_project.model.Author
import mk.ukim.finki.rest_assured_project.model.Book
import mk.ukim.finki.rest_assured_project.model.enums.Category
import mk.ukim.finki.rest_assured_project.repository.AuthorRepository
import mk.ukim.finki.rest_assured_project.repository.BookRepository
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository
) {

    @PostConstruct
    fun init() {
        val danBrown = Author(0, "Dan", "Brown", "USA")
        val stephenKing = Author(0, "Stephen", "King", "USA")
        val tolkien = Author(0, "J. R. R.", "Tolkien", "England")
        val authors = listOf(
            danBrown,
            stephenKing,
            tolkien
        )

        this.authorRepository.saveAll(authors)

        val books = listOf(
            Book(0, "The Lost Symbol", "0385504225", 10.50, 10, Category.THRILLER, danBrown),
            Book(0, "Origin", "9780385514231", 11.00, 5, Category.THRILLER, danBrown),
            Book(0, "The Hobbit", "0618260307", 9.99, 3, Category.FANTASY, tolkien)
        )

        this.bookRepository.saveAll(books)
    }
}