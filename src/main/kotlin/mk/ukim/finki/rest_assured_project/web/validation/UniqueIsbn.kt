package mk.ukim.finki.rest_assured_project.web.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [UniqueIsbnValidator::class])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class UniqueIsbn(
    val message: String = "A book with this ISBN already exists in the database!",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
