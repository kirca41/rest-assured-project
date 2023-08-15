package mk.ukim.finki.rest_assured_project.repository

import mk.ukim.finki.rest_assured_project.model.Book
import mk.ukim.finki.rest_assured_project.model.enums.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface BookRepository : JpaRepository<Book, Long> {

    fun findByIsbn(isbn: String): Book?

    fun existsByIsbn(isbn: String): Boolean

    fun findAllByCategory(category: Category, pageable: Pageable): Page<Book>
}