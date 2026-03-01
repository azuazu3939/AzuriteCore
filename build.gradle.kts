plugins {
    kotlin("jvm") version "2.3.20-RC"
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "com.github.azuazu3939"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.onarandombox.com/public/")
}

dependencies {
    compileOnly("io.lumine:Mythic-Dist:5.11.2")
    compileOnly("io.lumine:MythicCrucible-Dist:2.2.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.55"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.22.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.22.0")

    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(21)
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
        relocate("com.github.shynixn", "com.github.azuazu3939.lib.com.github.shynixn")
        relocate("org.jetbrains", "com.github.azuazu3939.lib.org.jetbrains")
    }
}
