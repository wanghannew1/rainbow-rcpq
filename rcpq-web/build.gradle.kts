plugins {
    id("org.springframework.boot")
    kotlin("plugin.spring")
}
dependencies {
    implementation(project(":rcpq-service"))
    implementation(project(":rcpq-entity"))
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.5")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.5")
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.alibaba:easyexcel:3.3.4")
}
