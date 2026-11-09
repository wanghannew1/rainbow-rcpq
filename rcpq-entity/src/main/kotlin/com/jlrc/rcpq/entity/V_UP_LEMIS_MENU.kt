package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_UP_LEMIS_MENU")
open class V_UP_LEMIS_MENU {
    @Id
    @Column(name = "NAME")
    var name: String? = null

    @Column(name = "TITLE")
    var title: String? = null

    @Column(name = "PARENT")
    var parent: String? = null

    @Column(name = "TARGET")
    var target: String? = null

    @Column(name = "LOCATION")
    var location: String? = null

    @Column(name = "ROLES")
    var roles: String? = null

    @Column(name = "CHIEF")
    var chief: Long? = null

    @Column(name = "IMAGE")
    var image: String? = null

    @Column(name = "ALTIMAGE")
    var altimage: String? = null

    @Column(name = "DESCRIPTION")
    var description: String? = null

    @Column(name = "ORDERID")
    var orderid: Long? = null

}
