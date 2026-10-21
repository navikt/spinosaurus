package no.nav.syfo.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.metrics.prometheus.PrometheusMetricsTrackerFactory

fun createHikariConfig(
    jdbcUrl: String,
    username: String,
    password: String,
) = HikariConfig().apply {
    this.jdbcUrl = jdbcUrl
    this.username = username
    this.password = password
    minimumIdle = 1
    maximumPoolSize = 3
    maxLifetime = 30001
    connectionTimeout = 2000
    idleTimeout = 10001
    poolName = "defaultPool"
    driverClassName = "org.postgresql.Driver"
}
