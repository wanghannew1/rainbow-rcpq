package com.jlrc.rcpq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
class RcpqApplication

fun main(args: Array<String>) {
    runApplication<RcpqApplication>(*args)
}
