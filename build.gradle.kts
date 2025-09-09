plugins {
    kotlin("jvm") version "2.2.20-RC"
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "com.github.azuazu3939"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.onarandombox.com/content/groups/public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.azisaba.net/repository/maven-public/")
}

dependencies {
    compileOnly("io.lumine:Mythic-Dist:5.8.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") {
        isTransitive = false
    }

    implementation(platform("com.intellectualsites.bom:bom-newest:1.55"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.zaxxer:HikariCP:6.0.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.3")
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
        from(sourceSets.main.get().resources.srcDirs) {
            include("**")

            val tokenReplacementMap = mapOf(
                "version" to project.version,
                "name" to project.rootProject.name,
            )

            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(projectDir) { include("LICENSE") }
    }
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari", "com.github.azuazu3939.lib.com.zaxxer.hikari")
        relocate("org.mariadb.jdbc", "com.github.azuazu3939.lib.org.mariadb.jdbc")
    }
}
