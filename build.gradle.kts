plugins {
    java
    application
}

group = "ru.confectionery"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    // MongoDB драйвер
    implementation("org.mongodb:mongodb-driver-sync:4.7.1")
    
    // Логирование
    implementation("ch.qos.logback:logback-classic:1.2.6")
    implementation("org.slf4j:slf4j-api:1.7.32")
    
    // JSON обработка
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    
    // Тестирование
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.mockito:mockito-core:4.0.0")
}

application {
    mainClass.set("ru.confectionery.Main")
}

tasks.test {
    useJUnitPlatform()
}

// Создание исполняемого JAR файла
tasks.jar {
    manifest {
        attributes["Main-Class"] = "ru.confectionery.Main"
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
    
    // Включаем зависимости в JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Настройка запуска приложения
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    jvmArgs = listOf("-Dfile.encoding=UTF-8")
}

// Настройка ресурсов
sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
        }
    }
}