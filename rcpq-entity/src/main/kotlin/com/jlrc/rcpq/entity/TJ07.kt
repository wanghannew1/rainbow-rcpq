package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ07")
open class TJ07 {
    @Id
    @Column(name = "ATJ070")
    var atj070: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATC93G")
    var atc93g: String? = null

    @Column(name = "ISLOCK")
    var islock: String? = null

    @Column(name = "ATC8U9")
    var atc8u9: String? = null

    @Column(name = "ATC8U7")
    var atc8u7: String? = null

    @Column(name = "ATC8G4")
    var atc8g4: String? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

}
