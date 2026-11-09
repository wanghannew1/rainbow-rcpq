package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CBJ_BP")
open class CBJ_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CBJ_ID")
    @SequenceGenerator(name = "SEQ_CBJ_ID", sequenceName = "SEQ_CBJ_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATB930")
    var atb930: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAE001")
    var aae001: String? = null

    @Column(name = "ATB90Z_1")
    var atb90z_1: String? = null

    @Column(name = "ATB90Z_2")
    var atb90z_2: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

}
