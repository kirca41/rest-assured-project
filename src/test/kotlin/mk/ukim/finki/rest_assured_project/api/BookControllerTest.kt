package mk.ukim.finki.rest_assured_project.api

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import mk.ukim.finki.rest_assured_project.model.Book
import mk.ukim.finki.rest_assured_project.model.dtos.BookAddDto
import mk.ukim.finki.rest_assured_project.model.dtos.BookEditDto
import mk.ukim.finki.rest_assured_project.model.enums.Category
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.math.ceil


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookControllerTest {

    private val baseUrl = "/api/books"
    private val BLANK_ISBN_ERROR_MESSAGE = "Book ISBN must not be blank!"
    private val BLANK_TITLE_ERROR_MESSAGE = "Book title must not be blank!"
    private val NONPOSITIVE_PRICE_ERROR_MESSAGE = "Book price must not be negative!"
    private val NONPOSITIVE_QUANTITY_ERROR_MESSAGE = "Book quantity in stock must not be negative!"
    private val INVALID_ISBN_ERROR_MESSAGE = "ISBN is not valid!"
    private val ISBN_NOT_UNIQUE_ERROR_MESSAGE = "A book with this ISBN already exists in the database!"

    @LocalServerPort
    private var port: Int = 0

    @BeforeAll
    fun setup() {
        RestAssured.port = port
        RestAssured.basePath = baseUrl
        RestAssured.filters(RequestLoggingFilter(), ResponseLoggingFilter())
    }

    @Test
    fun `should return a list of books when given default parameters`() {
        given()
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .body("content.size()", greaterThan(0))
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "2", "3", "10"])
    fun `should return the first page of books with the given size`(size: Int) {
        val page = 0
        val totalElements =
            given()
                .`when`()
                .get()
                .then()
                .extract()
                .path<Int>("totalElements")

        val totalPages = if (size >= totalElements) 1 else ceil(1.0 * totalElements / size).toInt()
        val numberOfElements = if (size >= totalElements) totalElements else size
        val last = size >= totalElements

        given()
            .param("page", page)
            .param("size", size)
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .body("content.size()", greaterThan(0))
            .body("first", equalTo(true))
            .body("last", equalTo(last))
            .body("totalElements", equalTo(totalElements))
            .body("totalPages", equalTo(totalPages))
            .body("numberOfElements", equalTo(numberOfElements))
    }

    // TODO: Should I add test for intermediary page?

    @ParameterizedTest
    @ValueSource(strings = ["1", "2", "3", "10"])
    fun `should return the last page of books with the given size`(size: Int) {
        val response =
            given()
                .`when`()
                .get()
                .then()
                .extract()
                .response()

        val totalElements = response.path<Int>("totalElements")
        val totalPages = if (size >= totalElements) 1 else ceil(1.0 * totalElements / size).toInt()
        val numberOfElements = if (totalElements % size == 0) size else totalElements % size
        val first = size >= totalElements

        given()
            .param("page", totalPages - 1)
            .param("size", size)
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .body("content.size()", greaterThan(0))
            .body("first", equalTo(first))
            .body("last", equalTo(true))
            .body("totalElements", equalTo(totalElements))
            .body("totalPages", equalTo(totalPages))
            .body("numberOfElements", equalTo(numberOfElements))
    }

    @Test
    fun `should return a list of books sorted by id in ascending order`() {
        val response = given()
            .`when`()
            .get()

        val content = response.jsonPath().getList("content", Book::class.java)
        val booksSortedByIdAscending = content.sortedBy { it.id }

        response.then()
            .statusCode(200)

        assertEquals(content, booksSortedByIdAscending)
    }

    @Test
    fun `should return a list of books sorted by title in descending order`() {
        val response = given()
            .param("sortDirection", "desc")
            .param("sortBy", "title")
            .`when`()
            .get()

        val content = response.jsonPath().getList("content", Book::class.java)
        val booksSortedByTitleDescending = content.sortedByDescending { it.title }

        response.then()
            .statusCode(200)

        assertEquals(content, booksSortedByTitleDescending)
    }

    @ParameterizedTest
    @EnumSource(Category::class)
    fun `should return a list of books filtered by the given category`(category: Category) {
        given()
            .param("category", category)
            .param("category", category)
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .body("content.every { it.category == '$category' }", equalTo(true))
    }

    @Test
    fun `should return details of a book with the given id`() {
        val id = 3

        given()
            .`when`()
            .get("/$id")
            .then()
            .statusCode(200)
            .body("title", not(emptyOrNullString()))
            .body("isbn", not(emptyOrNullString()))
            .body("price", greaterThan(0f))
            .body("quantityInStock", greaterThanOrEqualTo(0))
            .body("category", `in`(Category.values().map { it.name }))
            .body("author.id", greaterThan(0))
            .body("author.firstName", not(emptyOrNullString()))
            .body("author.lastName", not(emptyOrNullString()))
            .body("author.country", not(emptyOrNullString()))
    }

    @Test
    fun `should return details of a book with the given isbn`() {
        val isbn = "0618260307"

        given()
            .param("isbn", isbn)
            .`when`()
            .get("/search")
            .then()
            .statusCode(200)
            .body("title", not(emptyOrNullString()))
            .body("isbn", not(emptyOrNullString()))
            .body("price", greaterThan(0f))
            .body("quantityInStock", greaterThanOrEqualTo(0))
            .body("category", `in`(Category.values().map { it.name }))
            .body("author.id", greaterThan(0))
            .body("author.firstName", not(emptyOrNullString()))
            .body("author.lastName", not(emptyOrNullString()))
            .body("author.country", not(emptyOrNullString()))
    }

    @Test
    fun `should return not found and appropriate error message when searching for book with non-existant isbn`() {
        val isbn = "9780136006176"

        given()
            .param("isbn", isbn)
            .`when`()
            .get("/search")
            .then()
            .statusCode(404)
            .body("errors", equalTo("Book with isbn [$isbn] does not exist!"))
    }

    @Test
    fun `should add a new book, return it's location and retrieve it's details successfully by the returned id`() {
        val bookAddDto = BookAddDto(
            isbn = "6258327656",
            title = "Silmarillion",
            price = 12.50,
            quantityInStock = 10,
            category = Category.FANTASY,
            authorId = 3
        )

        val addedBookId = given()
            .contentType(ContentType.JSON)
            .body(bookAddDto)
            .`when`()
            .post("/add")
            .then()
            .statusCode(201)
            .header("Location", matchesPattern("$baseUrl/\\d+"))
            .extract()
            .header("Location")
            .split("/")
            .last()

        given()
            .`when`()
            .get("/$addedBookId")
            .then()
            .statusCode(200)
            .body("title", equalTo(bookAddDto.title))
            .body("isbn", equalTo(bookAddDto.isbn))
            .body("price", equalTo(bookAddDto.price.toFloat()))
            .body("quantityInStock", equalTo(bookAddDto.quantityInStock))
            .body("category", equalTo(bookAddDto.category.name))
            .body("author.id", equalTo(bookAddDto.authorId.toInt()))
            .body("author.firstName", not(emptyOrNullString()))
            .body("author.lastName", not(emptyOrNullString()))
            .body("author.country", not(emptyOrNullString()))
    }

    @Test
    fun `should return bad request and appropriate error messages for blank isbn and title`() {
        val bookAddDto = BookAddDto(
            isbn = "",
            title = "",
            price = 9.50,
            quantityInStock = 50,
            category = Category.FANTASY,
            authorId = 3
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookAddDto)
            .`when`()
            .post("/add")
            .then()
            .statusCode(400)
            .body("errors.size()", greaterThan(0))
            .body(
                "errors", hasItems(
                    BLANK_TITLE_ERROR_MESSAGE,
                    BLANK_ISBN_ERROR_MESSAGE
                )
            )
    }

    @Test
    fun `should return bad request and appropriate error messages for nonpositive-valued fields`() {
        val bookAddDto = BookAddDto(
            isbn = "0306406152",
            title = "Error-Correction Coding for Digital Communications",
            price = -18.00,
            quantityInStock = -150,
            category = Category.FANTASY,
            authorId = 3
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookAddDto)
            .`when`()
            .post("/add")
            .then()
            .statusCode(400)
            .body("errors.size()", greaterThan(0))
            .body(
                "errors", hasItems(
                    NONPOSITIVE_PRICE_ERROR_MESSAGE,
                    NONPOSITIVE_QUANTITY_ERROR_MESSAGE
                )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "1234567890", "987654321X", "abcdefghij", "0112112425", "0000100000", // length 10
            "1234567890123", "987654321012X", "abcdefghij456", "5555555555555", "0000000050000", // length 13
            "123456", "98765432101", "fjkhakjs4567", "99999999999999", "invalid_isbn" // arbitrary length
        ]
    )
    fun `should return bad request and appropriate error messages for invalid isbn values`(isbn: String) {
        val bookAddDto = BookAddDto(
            isbn = isbn,
            title = "Invalid Book",
            price = 5.50,
            quantityInStock = 100,
            category = Category.FANTASY,
            authorId = 3
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookAddDto)
            .`when`()
            .post("/add")
            .then()
            .statusCode(400)
            .body("errors.size()", greaterThan(0))
            .body("errors", hasItem(INVALID_ISBN_ERROR_MESSAGE))
    }

    @Test
    fun `should return bad request and appropriate error message when adding a book with id that already exists in the database`() {
        val bookAddDto = BookAddDto(
            isbn = "0618260307",
            title = "The Lost Symbol",
            price = 20.00,
            quantityInStock = 20,
            category = Category.FANTASY,
            authorId = 1
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookAddDto)
            .`when`()
            .post("/add")
            .then()
            .statusCode(400)
            .body("errors.size()", greaterThan(0))
            .body(
                "errors", hasItem(
                    ISBN_NOT_UNIQUE_ERROR_MESSAGE
                )
            )
    }

    @Test
    fun `should return not found and appropriate error message when adding a book with invalid author id`() {
        val bookAddDto = BookAddDto(
            isbn = "9780553801477",
            title = "A Dance with Dragons (A Song of Ice and Fire)",
            price = 15.00,
            quantityInStock = 2,
            category = Category.FANTASY,
            authorId = 777
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookAddDto)
            .`when`()
            .post("/add")
            .then()
            .statusCode(404)
            .body("errors.size()", greaterThan(0))
            .body("errors", equalTo("Author with id [${bookAddDto.authorId}] does not exist!"))
    }

    @Test
    fun `should update an existing book's price and return its details`() {
        val bookEditDto = BookEditDto(
            id = 1,
            title = "The Lost Symbol",
            isbn = "0385504225",
            price = 8.50,
            quantityInStock = 10,
            category = Category.THRILLER,
            authorId = 1
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookEditDto)
            .`when`()
            .put("/edit")
            .then()
            .statusCode(200)
            .body("price", equalTo(bookEditDto.price.toFloat()))
    }

    @Test
    fun `should return not found and appropriate error message when updating a book with invalid id`() {
        val bookEditDto = BookEditDto(
            id = 999,
            title = "The Lost Symbol",
            isbn = "9780141033570",
            price = 8.50,
            quantityInStock = 10,
            category = Category.THRILLER,
            authorId = 1
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookEditDto)
            .`when`()
            .put("/edit")
            .then()
            .statusCode(404)
            .body("errors", equalTo("Book with id [${bookEditDto.id}] does not exist!"))
    }

    @Test
    fun `should return not found and appropriate error message when updating a book with invalid author id`() {
        val bookEditDto = BookEditDto(
            id = 1,
            title = "The Lost Symbol",
            isbn = "0385504225",
            price = 8.50,
            quantityInStock = 10,
            category = Category.THRILLER,
            authorId = 888
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookEditDto)
            .`when`()
            .put("/edit")
            .then()
            .statusCode(404)
            .body("errors", equalTo("Author with id [${bookEditDto.authorId}] does not exist!"))
    }


    @Test
    fun `should return bad request and appropriate error messages when updating a book's isbn with an existing book's isbn`() {
        val bookEditDto = BookEditDto(
            id = 1,
            title = "The Lost Symbol",
            isbn = "9780385514231", // Another book's ISBN
            price = 8.50,
            quantityInStock = 10,
            category = Category.THRILLER,
            authorId = 1
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookEditDto)
            .`when`()
            .put("/edit")
            .then()
            .statusCode(400)
            .body("errors", hasItem(ISBN_NOT_UNIQUE_ERROR_MESSAGE))
    }

    @Test
    fun `should delete an existing book by its id`() {
        val id = 1

        given()
            .`when`()
            .delete("/delete/$id")
            .then()
            .statusCode(200)

        given()
            .`when`()
            .get("/$id")
            .then()
            .statusCode(404)
            .body("errors", equalTo("Book with id [$id] does not exist!"))
    }

    @Test
    fun `should return not found and appropriate error message when deleting a book with invalid id`() {
        val id = 555

        given()
            .`when`()
            .delete("/delete/$id")
            .then()
            .statusCode(404)
            .body("errors", equalTo("Book with id [$id] does not exist!"))
    }

    @Test
    fun `should reduce the quantity in stock of the given book and return it's updated details`() {
        val id = 3
        val amount = 2

        val book = given()
            .`when`()
            .get("/$id")
            .then()
            .statusCode(200)
            .extract()
            .`as`(Book::class.java)

        given()
            .param("amount", amount)
            .`when`()
            .post("/$id/buy")
            .then()
            .statusCode(200)
            .body("quantityInStock", equalTo(book.quantityInStock - amount))

        given()
            .`when`()
            .get("/$id")
            .then()
            .statusCode(200)
            .body("quantityInStock", equalTo(book.quantityInStock - amount))
    }

    @Test
    fun `should return not found and appropriate error message when buying a book with invalid id`() {
        val id = 12345

        given()
            .param("amount", 1)
            .`when`()
            .post("/$id/buy")
            .then()
            .statusCode(404)
            .body("errors", equalTo("Book with id [$id] does not exist!"))
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -10])
    fun `should return bad request and appropriate error message for nonpositive amounts when buying a book`(amount: Int) {
        val id = 1

        given()
            .param("amount", amount)
            .`when`()
            .post("/$id/buy")
            .then()
            .statusCode(400)
            .body("errors", equalTo("Book amount to buy must be positive!"))
    }

    @Test
    fun `should return bad request and appropriate error message when buying amount greater than available quantity in stock`() {
        val id = 3
        val amount = 11

        given()
            .param("amount", amount)
            .`when`()
            .post("/$id/buy")
            .then()
            .statusCode(400)
            .body("errors", equalTo("Book with id [$id] has less than [$amount] copies left in stock!"))
    }

}