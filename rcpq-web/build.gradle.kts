import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("org.springframework.boot")
    kotlin("plugin.spring")
}
// 双 main 类(RcpqApplicationKt 业务/SyncMainKt 外挂同步)需显式指定业务入口
springBoot {
    mainClass.set("com.jlrc.rcpq.RcpqApplicationKt")
}
// 外挂同步的独立可执行 jar(不依赖 boot loader, 与业务 bootJar 分离):
//   java -jar rcpq-web-sync.jar [表名...] — 入口 SyncMainKt, 见 scripts/sync_daily.sh
val syncJar = tasks.register<Jar>("syncJar") {
    archiveClassifier.set("sync")
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes("Main-Class" to "com.jlrc.rcpq.web.sync.SyncMainKt") }
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
dependencies {
    implementation(project(":rcpq-service"))
    implementation(project(":rcpq-entity"))
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.5")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.5")
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.alibaba:easyexcel:3.3.4")
    // 报盘导出直接用 POI 以 resources/baopan 模板为底稿生成(版本对齐 easyexcel 3.3.4 依赖, 避免冲突)
    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.poi:poi-ooxml:4.1.2")
}
