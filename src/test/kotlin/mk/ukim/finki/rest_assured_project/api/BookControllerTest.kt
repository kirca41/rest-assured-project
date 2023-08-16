package mk.ukim.finki.rest_assured_project.api

import io.restassured.RestAssured
import io.restassured.RestAssured.given
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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookControllerTest {

    private val baseUrl = "/api/books"
    @LocalServerPort
    private var port: Int = 0

    @BeforeAll
    fun setup() {
        RestAssured.port = port
    }

    @Test
    fun `it should a list of books with default parameters`() {
        given()
            .`when`()
            .log().all()
            .get(baseUrl)
            .then()
            .log().all()
            .statusCode(200)
            .body("content.size()", greaterThan(0))
    }

    @Test
    fun `it should return a list of books sorted by id in ascending order`() {
        val response = given()
            .log().all()
            .`when`()
            .get(baseUrl)

        val content = response.jsonPath().getList("content", Book::class.java)
        val booksSortedByIdAscending = content.sortedBy { it.id }

        response.then()
            .log().all()
            .statusCode(200)

        assertEquals(content, booksSortedByIdAscending)
    }

    @Test
    fun `it should return a list of books sorted by title in descending order`() {
        val response = given()
            .param("sortDirection", "desc")
            .param("sortBy", "title")
            .log().all()
            .`when`()
            .get(baseUrl)

        val content = response.jsonPath().getList("content", Book::class.java)
        val booksSortedByTitleDescending = content.sortedByDescending { it.title }

        response.then()
            .log().all()
            .statusCode(200)

        assertEquals(content, booksSortedByTitleDescending)
    }

    @Test
    fun `it should return a list of books filtered by category`() {
        val category = Category.THRILLER

        given()
            .param("category", category)
            .log().all()
            .`when`()
            .get("$baseUrl?category=${category}")
            .then()
            .log().all()
            .statusCode(200)
            .body("content.every { it.category == '$category' }", equalTo(true))
    }

    @Test
    fun `it should return details of a book with the given id`() {
        val id = 1

        given()
            .log().all()
            .`when`()
            .get("$baseUrl/$id")
            .then()
            .log().all()
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
    fun `it should add a new book and return its location and details`() {
        val bookAddDto = BookAddDto(
            isbn = "6258327656",
            title = "Silmarillion",
            price = 12.50,
            quantityInStock = 10,
            category = Category.FANTASY,
            authorId = 3
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookAddDto)
            .log().all()
            .`when`()
            .post("$baseUrl/add")
            .then()
            .log().all()
            .statusCode(201)
            .header("Location", matchesPattern("$baseUrl/\\d+"))
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
    fun `it should update an existing book's price and return its details`() {
        val bookEditDto = BookEditDto(
            id = 1,
            title = "The Lost Symbol",
            isbn = "0385504225",
            price = 8.50,
            quantityInStock =  10,
            category = Category.THRILLER,
            authorId = 1
        )

        given()
            .contentType(ContentType.JSON)
            .body(bookEditDto)
            .log().all()
            .`when`()
            .put("$baseUrl/edit")
            .then()
            .log().all()
            .statusCode(200)
            .body("price", equalTo(bookEditDto.price.toFloat()))
    }

    @Test
    fun `it should delete an existing book by its id`() {
        val id = 1

        given()
            .log().all()
            .`when`()
            .delete("$baseUrl/delete/$id")
            .then()
            .log().all()
            .statusCode(200)

        given()
            .log().all()
            .`when`()
            .get("$baseUrl/$id")
            .then()
            .log().all()
            .statusCode(404)
            .body("error", equalTo("Book with id [$id] does not exist!"))
    }

    @Test
    fun `it should reduce the quantity in stock of the given book and returns it's updated details`() {
        val id = 3
        val amount = 2

        val book = given()
            .log().all()
            .`when`()
            .get("$baseUrl/$id")
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .`as`(Book::class.java)

        given()
            .log().all()
            .param("amount", amount)
            .`when`()
            .post("$baseUrl/$id/buy")
            .then()
            .log().all()
            .statusCode(200)
            .body("quantityInStock", equalTo(book.quantityInStock - amount))

        given()
            .log().all()
            .`when`()
            .get("$baseUrl/$id")
            .then()
            .log().all()
            .statusCode(200)
            .body("quantityInStock", equalTo(book.quantityInStock - amount))
    }

}