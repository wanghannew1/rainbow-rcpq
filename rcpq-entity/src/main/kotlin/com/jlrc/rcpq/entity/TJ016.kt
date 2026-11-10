package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ016")
open class TJ016 {
    @Id
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATJ0160")
    var atj0160: Long? = null

    @Column(name = "ATJ0161")
    var atj0161: Long? = null

    @Column(name = "ATJ0162")
    var atj0162: Long? = null

    @Column(name = "ATJ0163")
    var atj0163: Long? = null

    @Column(name = "ATJ0164")
    var atj0164: Long? = null

    @Column(name = "ATJ0165")
    var atj0165: Long? = null

    @Column(name = "ATJ0166")
    var atj0166: Long? = null

    @Column(name = "ATJ0167")
    var atj0167: Long? = null

    @Column(name = "ATJ0168")
    var atj0168: Long? = null

    @Column(name = "ATJ0169")
    var atj0169: Long? = null

    @Column(name = "ATJ016A")
    var atj016a: Long? = null

    @Column(name = "ATJ016B")
    var atj016b: Long? = null

    @Column(name = "ATJ016C")
    var atj016c: Long? = null

    @Column(name = "ATJ016D")
    var atj016d: Long? = null

    @Column(name = "ATJ016E")
    var atj016e: Long? = null

    @Column(name = "ATJ016F")
    var atj016f: Long? = null

    @Column(name = "AAE011")
    var aae011: String? = null
}
