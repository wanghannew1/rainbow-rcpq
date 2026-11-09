package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8N")
open class TC8N {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8N_ATC8N0")
    @SequenceGenerator(name = "SEQ_TC8N_ATC8N0", sequenceName = "SEQ_TC8N_ATC8N0", allocationSize = 1)
    @Column(name = "ATC8N0")
    var atc8n0: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC8N1")
    var atc8n1: BigDecimal? = null

    @Column(name = "ATC8N2")
    var atc8n2: BigDecimal? = null

    @Column(name = "ATC8N3")
    var atc8n3: String? = null

    @Column(name = "ATC8N4")
    var atc8n4: BigDecimal? = null

    @Column(name = "ATC8N5")
    var atc8n5: BigDecimal? = null

    @Column(name = "ATC8N6")
    var atc8n6: BigDecimal? = null

    @Column(name = "ATC8N7")
    var atc8n7: BigDecimal? = null

    @Column(name = "ATC8N8")
    var atc8n8: BigDecimal? = null

    @Column(name = "ISLOCK")
    var islock: String? = null

    @Column(name = "ATC8N9")
    var atc8n9: BigDecimal? = null

    @Column(name = "ATC8NA")
    var atc8na: BigDecimal? = null

    @Column(name = "AAA941")
    var aaa941: String? = null

}
