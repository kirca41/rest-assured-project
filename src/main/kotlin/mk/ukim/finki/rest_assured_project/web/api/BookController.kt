package mk.ukim.finki.rest_assured_project.web.api

import mk.ukim.finki.rest_assured_project.model.Book
import mk.ukim.finki.rest_assured_project.model.dtos.BookAddDto
import mk.ukim.finki.rest_assured_project.model.dtos.BookEditDto
import mk.ukim.finki.rest_assured_project.model.enums.Category
import mk.ukim.finki.rest_assured_project.repository.BookRepository
import mk.ukim.finki.rest_assured_project.service.BookService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/books")
class BookController(
    val bookService: BookService
) {

    @GetMapping
    fun getBooks(
        @RequestParam(required = false) category: Category?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "asc") sortDirection: String,
        @RequestParam(defaultValue = "id") sortBy: String
    ): ResponseEntity<Page<Book>> {
        val sortingCriteria = Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        val pageable = PageRequest.of(page, size, sortingCriteria)

        return if (category != null) {
            this.bookService.getAllByCategory(category, pageable)
        } else {
            this.bookService.getAll(pageable)
        }.let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Book> =
        this.bookService.getBookById(id).let { ResponseEntity.ok(it) }

    @GetMapping("/search")
    fun getByIsbn(@RequestParam isbn: String): ResponseEntity<Book> =
        this.bookService.getBookByIsbn(isbn).let { ResponseEntity.ok(it) }

    @PostMapping("/add")
    fun addNewBook(@RequestBody @Validated bookAddDto: BookAddDto): ResponseEntity<Book> =
        this.bookService.createBook(bookAddDto)
            .let { ResponseEntity.created(URI("/api/books/${it.id}")).body(it) }

    @PutMapping("/edit")
    fun editBook(@RequestBody @Validated bookDto: BookEditDto): ResponseEntity<Book> =
        this.bookService.updateBook(bookDto).let { ResponseEntity.ok(it) }

    @DeleteMapping("/delete/{id}")
    fun deleteBook(@PathVariable id: Long): ResponseEntity<Unit> =
        this.bookService.deleteBook(id).let { ResponseEntity.ok().build() }

    @PostMapping("/{id}/buy")
    fun buyBooks(
        @PathVariable id: Long,
        @RequestParam amount: Int
    ): ResponseEntity<*> =
        if (amount <= 0) {
            ResponseEntity.badRequest().body(mapOf("errors" to "Book amount to buy must be positive!"))
        } else {
            this.bookService.buyBooks(id, amount).let { ResponseEntity.ok(it) }
        }

}