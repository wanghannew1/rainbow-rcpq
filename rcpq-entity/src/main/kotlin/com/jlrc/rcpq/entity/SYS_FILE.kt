package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "SYS_FILE")
open class SYS_FILE {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SYS_FILE_ID")
    @SequenceGenerator(name = "SEQ_SYS_FILE_ID", sequenceName = "SEQ_SYS_FILE_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

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

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAE200")
    var aae200: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "FLID")
    var flid: Long? = null

    @Column(name = "TABLEID")
    var tableid: String? = null

    @Column(name = "TABLEKEY")
    var tablekey: String? = null

    @Column(name = "KEYVALUE")
    var keyvalue: Long? = null

    @Column(name = "FILENAME")
    var filename: String? = null

    @Column(name = "FILESTYLE")
    var filestyle: String? = null

    @Column(name = "FILEVALUE")
    var filevalue: String? = null

    @Column(name = "FILEUP")
    var fileup: String? = null

    @Column(name = "FILEHZM")
    var filehzm: String? = null

    @Column(name = "TABLENAME")
    var tablename: String? = null

}
