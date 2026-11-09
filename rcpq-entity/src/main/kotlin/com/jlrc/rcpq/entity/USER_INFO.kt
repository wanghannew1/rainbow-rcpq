package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "USER_INFO")
open class USER_INFO {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USER_INFO_USERINFO")
    @SequenceGenerator(name = "SEQ_USER_INFO_USERINFO", sequenceName = "SEQ_USER_INFO_USERINFO", allocationSize = 1)
    @Column(name = "USERINFO")
    var userinfo: Long? = null

    @Column(name = "USER_ID")
    var user_id: String? = null

    @Column(name = "USER_NAME")
    var user_name: String? = null

    @Column(name = "USER_CODE")
    var user_code: String? = null

    @Column(name = "PHOTO")
    var photo: String? = null

    @Column(name = "TITLE")
    var title: String? = null

    @Column(name = "CLIENT_IP")
    var client_ip: String? = null

    @Column(name = "DEPARTMENT")
    var department: String? = null

}
