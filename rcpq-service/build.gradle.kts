dependencies {
    api(project(":rcpq-entity"))
    implementation("org.springframework:spring-context:6.1.14")
    implementation("org.springframework:spring-core:6.1.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
