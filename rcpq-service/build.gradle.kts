plugins {
    kotlin("plugin.spring")
}

dependencies {
    api(project(":rcpq-entity"))
    implementation("org.springframework:spring-context:6.1.14")
    implementation("org.springframework:spring-core:6.1.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.springframework:spring-tx:6.1.14")
    implementation("com.github.promeg:tinypinyin:2.0.3")
    // Oracle→PG 每日同步(并行运行期)驱动, JDK21 用 ojdbc11;
    // orai18n=ZHS16GBK 字符集完整支持(旧库 GBK, 缺它生僻字静默损坏)
    implementation("com.oracle.database.jdbc:ojdbc11:23.5.0.24.07")
    implementation("com.oracle.database.nls:orai18n:23.5.0.24.07")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.postgresql:postgresql:42.7.4")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
