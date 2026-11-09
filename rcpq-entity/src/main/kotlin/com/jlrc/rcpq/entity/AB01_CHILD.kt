package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AB01_CHILD")
open class AB01_CHILD {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AB01_CHILD_SEQ")
    @SequenceGenerator(name = "SEQ_AB01_CHILD_SEQ", sequenceName = "SEQ_AB01_CHILD_SEQ", allocationSize = 1)
    @Column(name = "SEQ")
    var seq: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ID")
    var id: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "LV")
    var lv: String? = null

    @Column(name = "ROOT")
    var root: String? = null

}
