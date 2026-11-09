package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AC05")
open class AC05 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AC05_ID")
    @SequenceGenerator(name = "SEQ_AC05_ID", sequenceName = "SEQ_AC05_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "TYPE")
    var type: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAC051")
    var aac051: String? = null

    @Column(name = "AAC052")
    var aac052: String? = null

    @Column(name = "AAC053")
    var aac053: String? = null

    @Column(name = "AAC054")
    var aac054: LocalDate? = null

    @Column(name = "AAC055")
    var aac055: LocalDate? = null

    @Column(name = "AAC056")
    var aac056: LocalDate? = null

    @Column(name = "AAC057")
    var aac057: Long? = null

    @Column(name = "AAC058")
    var aac058: Long? = null

    @Column(name = "AAC059")
    var aac059: Long? = null

}
