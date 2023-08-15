package mk.ukim.finki.rest_assured_project.web.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [IsbnValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidIsbn(
    val message: String = "ISBN is not valid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)