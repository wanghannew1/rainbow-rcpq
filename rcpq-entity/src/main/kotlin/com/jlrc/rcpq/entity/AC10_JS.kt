package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AC10_JS")
open class AC10_JS {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AC10_JS_ID")
    @SequenceGenerator(name = "SEQ_AC10_JS_ID", sequenceName = "SEQ_AC10_JS_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "SXNY")
    var sxny: Long? = null

    @Column(name = "TYPE")
    var type: String? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "AAA304")
    var aaa304: BigDecimal? = null

    @Column(name = "AAA305")
    var aaa305: BigDecimal? = null

    @Column(name = "AAA306")
    var aaa306: BigDecimal? = null

    @Column(name = "AAE100")
    var aae100: String? = null

}
