package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "JFSJ01")
open class JFSJ01 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JFSJ01_ID")
    @SequenceGenerator(name = "SEQ_JFSJ01_ID", sequenceName = "SEQ_JFSJ01_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "JFLX")
    var jflx: String? = null

    @Column(name = "JFSJ0201")
    var jfsj0201: BigDecimal? = null

    @Column(name = "JFSJ0202")
    var jfsj0202: BigDecimal? = null

    @Column(name = "JFSJ0203")
    var jfsj0203: BigDecimal? = null

    @Column(name = "JFSJ0204")
    var jfsj0204: BigDecimal? = null

    @Column(name = "JFSJ0205")
    var jfsj0205: BigDecimal? = null

    @Column(name = "JFSJ0206")
    var jfsj0206: BigDecimal? = null

    @Column(name = "JFSJ0207")
    var jfsj0207: BigDecimal? = null

    @Column(name = "JFSJ0208")
    var jfsj0208: BigDecimal? = null

    @Column(name = "JFSJ0209")
    var jfsj0209: BigDecimal? = null

    @Column(name = "JFSJ020A")
    var jfsj020a: BigDecimal? = null

    @Column(name = "JFSJ020B")
    var jfsj020b: BigDecimal? = null

    @Column(name = "JFSJ020C")
    var jfsj020c: BigDecimal? = null

    @Column(name = "JFSJ020D")
    var jfsj020d: BigDecimal? = null

}
