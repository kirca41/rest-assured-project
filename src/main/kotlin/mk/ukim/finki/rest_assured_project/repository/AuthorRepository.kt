package mk.ukim.finki.rest_assured_project.repository

import mk.ukim.finki.rest_assured_project.model.Author
import org.springframework.data.jpa.repository.JpaRepository

interface AuthorRepository : JpaRepository<Author, Long> {
}