package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC13_PHOTO")
open class TC13_PHOTO {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC13_PHOTO_ID")
    @SequenceGenerator(name = "SEQ_TC13_PHOTO_ID", sequenceName = "SEQ_TC13_PHOTO_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "PHOTO")
    var photo: String? = null

}
