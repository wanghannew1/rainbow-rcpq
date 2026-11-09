package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC94_LZ")
open class TC94_LZ {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC94_LZ_ID")
    @SequenceGenerator(name = "SEQ_TC94_LZ_ID", sequenceName = "SEQ_TC94_LZ_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "TC93_ID")
    var tc93_id: Long? = null

    @Column(name = "AAA901")
    var aaa901: String? = null

    @Column(name = "AAA902")
    var aaa902: String? = null

    @Column(name = "ATC941")
    var atc941: BigDecimal? = null

}
