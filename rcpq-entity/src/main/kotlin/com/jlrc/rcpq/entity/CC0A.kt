package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CC0A")
open class CC0A {
    @Id
    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "PHOTO")
    var photo: String? = null

}
