package com.jlrc.rcpq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

// TODO-MIGRATION: PG16 数据源接入后移除 exclude（见 docs/database-migration-inventory.md）
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
class RcpqApplication

fun main(args: Array<String>) {
    runApplication<RcpqApplication>(*args)
}
