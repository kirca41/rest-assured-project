package mk.ukim.finki.rest_assured_project.service.impl

import mk.ukim.finki.rest_assured_project.model.Author
import mk.ukim.finki.rest_assured_project.model.Book
import mk.ukim.finki.rest_assured_project.model.dtos.BookAddDto
import mk.ukim.finki.rest_assured_project.model.dtos.BookEditDto
import mk.ukim.finki.rest_assured_project.model.exceptions.AuthorDoesNotExistException
import mk.ukim.finki.rest_assured_project.model.exceptions.BookDoesNotExistException
import mk.ukim.finki.rest_assured_project.model.exceptions.NotEnoughBooksAvailableInStockException
import mk.ukim.finki.rest_assured_project.repository.AuthorRepository
import mk.ukim.finki.rest_assured_project.repository.BookRepository
import mk.ukim.finki.rest_assured_project.service.BookService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BookServiceImpl(
    val bookRepository: BookRepository,
    val authorRepository: AuthorRepository
) : BookService {

    override fun getBookById(id: Long): Book {
        return this.bookRepository.findByIdOrNull(id)
            ?: throw BookDoesNotExistException("Book with id [$id] does not exist!")
    }

    override fun getBookByIsbn(isbn: String): Book {
        return this.bookRepository.findByIsbn(isbn)
            ?: throw BookDoesNotExistException("Book with isbn [$isbn] does not exist!")
    }

    private fun getAuthor(id: Long): Author {
        return this.authorRepository.findByIdOrNull(id)
            ?: throw AuthorDoesNotExistException("Author with id [$id] does not exist!")
    }


    override fun createBook(bookAddDto: BookAddDto): Book {
        val author = this.getAuthor(bookAddDto.authorId)

        return this.bookRepository.save(
            with(bookAddDto) {
                Book(
                    id = 0,
                    isbn = isbn,
                    title = title,
                    price = price,
                    quantityInStock = quantityInStock,
                    category = category,
                    author = author
                )
            }
        )
    }

    override fun updateBook(bookDto: BookEditDto): Book {
        val book = this.getBookById(bookDto.id)
        val author = this.getAuthor(bookDto.authorId)

        return this.bookRepository.save(
            with(bookDto) {
                book.copy(
                    isbn = isbn,
                    title = title,
                    price = price,
                    quantityInStock = quantityInStock,
                    category = category,
                    author = author
                )
            }
        )
    }

    override fun deleteBook(id: Long) {
        val book = this.getBookById(id)

        this.bookRepository.delete(book)
    }

    override fun buyBooks(id: Long, amount: Int): Book {
        val book = this.getBookById(id)
        if (book.quantityInStock < amount) {
            throw NotEnoughBooksAvailableInStockException("Book with id [$id] has less than [$amount] copies left in stock!")
        }

        return this.bookRepository.save(
            book.copy(
                quantityInStock = book.quantityInStock - amount
            )
        )
    }
}