package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "SYS_QUESTION")
open class SYS_QUESTION {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SYS_QUESTION_QSEQ")
    @SequenceGenerator(name = "SEQ_SYS_QUESTION_QSEQ", sequenceName = "SEQ_SYS_QUESTION_QSEQ", allocationSize = 1)
    @Column(name = "QSEQ")
    var qseq: Long? = null

    @Column(name = "QID")
    var qid: String? = null

    @Column(name = "QPROJECT")
    var qproject: String? = null

    @Column(name = "QPHASE")
    var qphase: String? = null

    @Column(name = "QGRADE")
    var qgrade: String? = null

    @Column(name = "QSOURCE")
    var qsource: String? = null

    @Column(name = "QOPER")
    var qoper: String? = null

    @Column(name = "QMODULE")
    var qmodule: String? = null

    @Column(name = "QDESCRIBE")
    var qdescribe: String? = null

    @Column(name = "QPRESENTER")
    var qpresenter: String? = null

    @Column(name = "QPHONE")
    var qphone: String? = null

    @Column(name = "QBRING")
    var qbring: LocalDate? = null

    @Column(name = "QSECTION")
    var qsection: String? = null

    @Column(name = "QSECTIONIDEA")
    var qsectionidea: String? = null

    @Column(name = "QBUREAU")
    var qbureau: String? = null

    @Column(name = "QBUREAUIDEA")
    var qbureauidea: String? = null

    @Column(name = "QBUREAUTIME")
    var qbureautime: LocalDate? = null

    @Column(name = "QTYPE")
    var qtype: String? = null

    @Column(name = "QRESULT")
    var qresult: String? = null

    @Column(name = "QSHOW")
    var qshow: String? = null

    @Column(name = "QDISPOSE")
    var qdispose: LocalDate? = null

    @Column(name = "QTRANSACTOR")
    var qtransactor: String? = null

    @Column(name = "QCLOSE")
    var qclose: String? = null

    @Column(name = "QCLOSETIME")
    var qclosetime: LocalDate? = null

    @Column(name = "QCLOSEMAN")
    var qcloseman: String? = null

    @Column(name = "QCLOSESHOW")
    var qcloseshow: String? = null

    @Column(name = "QSTATE")
    var qstate: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "QDESTINE")
    var qdestine: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

}
