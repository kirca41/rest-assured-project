package mk.ukim.finki.rest_assured_project.web.api

import mk.ukim.finki.rest_assured_project.model.exceptions.AuthorDoesNotExistException
import mk.ukim.finki.rest_assured_project.model.exceptions.BookDoesNotExistException
import mk.ukim.finki.rest_assured_project.model.exceptions.NotEnoughBooksAvailableInStockException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ResponseBody
@ControllerAdvice
class ErrorHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(
        AuthorDoesNotExistException::class,
        BookDoesNotExistException::class,
        NotEnoughBooksAvailableInStockException::class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onNotFoundException(exception: RuntimeException): Map<String, String> =
        mapOf("errors" to (exception.message ?: ""))

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        println(ex.allErrors)
        return ResponseEntity.badRequest().body(mapOf("errors" to ex.allErrors.flatMap { it.defaultMessage!!.split(";") }))
    }
}