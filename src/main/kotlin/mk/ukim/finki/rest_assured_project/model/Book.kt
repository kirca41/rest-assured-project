package mk.ukim.finki.rest_assured_project.model

import jakarta.persistence.*
import mk.ukim.finki.rest_assured_project.model.enums.Category

@Entity
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val title: String,
    val isbn: String,
    val price: Double,
    @Column(name = "quantity_in_stock")
    val quantityInStock: Int,
    @Enumerated(value = EnumType.STRING)
    val category: Category,
    @ManyToOne
    @JoinColumn(name = "author_id")
    val author: Author
)
