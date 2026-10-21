package no.nav.syfo.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.testcontainers.postgresql.PostgreSQLContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class WithPostgresContainer {
    private val postgresContainer =
        PostgreSQLContainer("postgres:15")
            .withReuse(true)
            .withLabel("app", "spinosaurus")
            .also {
                it.setCommand("postgres", "-c", "fsync=off", "-c", "log_statement=all", "-c", "wal_level=logical")

                it.start()

                println(
                    """
                    Databasecontainer er startet opp 🐘
                    port='${it.firstMappedPort}'
                    jdbcUrl='jdbc:postgresql://localhost:${it.firstMappedPort}/test'
                    credentials: 'test' og 'test'
                    """.trimIndent(),
                )
            }

    private val hikariConfig = createHikariConfig(postgresContainer)
    val dataSource = HikariDataSource(hikariConfig)

    @BeforeAll
    fun migrate() {
        migrate(hikariConfig)
    }

    @AfterAll
    fun stopContainer() {
        println("Stopper Postgres-container...")
        dataSource.close()
        postgresContainer.stop()
        println("Postgres-container er stoppet.")
    }
}

private fun createHikariConfig(container: PostgreSQLContainer): HikariConfig =
    HikariConfig().apply {
        jdbcUrl = container.jdbcUrl
        username = container.username
        password = container.password
        maximumPoolSize = 3
        driverClassName = "org.postgresql.Driver"
    }

private fun migrate(config: HikariConfig) {
    HikariDataSource(config)
        .also {
            Flyway
                .configure()
                .dataSource(it)
                .lockRetryCount(50)
                .load()
                .migrate()
        }.close()
}
