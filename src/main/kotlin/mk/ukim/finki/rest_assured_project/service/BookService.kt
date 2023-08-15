package mk.ukim.finki.rest_assured_project.service

import mk.ukim.finki.rest_assured_project.model.Book
import mk.ukim.finki.rest_assured_project.model.dtos.BookAddDto
import mk.ukim.finki.rest_assured_project.model.dtos.BookEditDto

interface BookService {

    fun getBookById(id: Long): Book

    fun getBookByIsbn(isbn: String): Book

    fun createBook(bookAddDto: BookAddDto): Book

    fun updateBook(bookDto: BookEditDto): Book

    fun deleteBook(id: Long)

    fun buyBooks(id: Long, amount: Int): Book
}