package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AB01")
open class AB01 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AB01_AAB001")
    @SequenceGenerator(name = "SEQ_AB01_AAB001", sequenceName = "SEQ_AB01_AAB001", allocationSize = 1)
    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAB003")
    var aab003: String? = null

    @Column(name = "AAB007")
    var aab007: String? = null

    @Column(name = "AAB010")
    var aab010: LocalDate? = null

    @Column(name = "AAB011")
    var aab011: LocalDate? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAB019")
    var aab019: String? = null

    @Column(name = "AAB020")
    var aab020: String? = null

    @Column(name = "AAB048")
    var aab048: String? = null

    @Column(name = "AAB054")
    var aab054: String? = null

    @Column(name = "AAB022")
    var aab022: String? = null

    @Column(name = "AAB021")
    var aab021: String? = null

    @Column(name = "AAB025")
    var aab025: String? = null

    @Column(name = "AAB026")
    var aab026: String? = null

    @Column(name = "AAB052")
    var aab052: String? = null

    @Column(name = "AAB053")
    var aab053: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAB013")
    var aab013: String? = null

    @Column(name = "AAB014")
    var aab014: String? = null

    @Column(name = "AAB023")
    var aab023: String? = null

    @Column(name = "AAE004")
    var aae004: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE015")
    var aae015: String? = null

    @Column(name = "AAE016")
    var aae016: String? = null

    @Column(name = "AAB01D")
    var aab01d: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "BAB002")
    var bab002: String? = null

    @Column(name = "PARENTID")
    var parentid: String? = null

    @Column(name = "HASCHILD")
    var haschild: String? = null

    @Column(name = "ORDERNUM")
    var ordernum: String? = null

    @Column(name = "ID")
    var id: String? = null

    @Column(name = "AAB00A")
    var aab00a: String? = null

    @Column(name = "AAB998")
    var aab998: String? = null

    @Column(name = "AAB00B")
    var aab00b: LocalDate? = null

    @Column(name = "AAB00C")
    var aab00c: LocalDate? = null

    @Column(name = "AAB049")
    var aab049: BigDecimal? = null

    @Column(name = "ISVALID")
    var isvalid: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

}
