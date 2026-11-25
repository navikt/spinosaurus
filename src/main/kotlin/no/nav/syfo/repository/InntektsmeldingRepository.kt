package no.nav.syfo.repository

import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.syfo.dto.InntektsmeldingEntitet
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

class InntektsmeldingRepository(
    private val ds: DataSource,
) {
    fun lagreInntektsmelding(
        inntektsmelding: InntektsmeldingEntitet,
        journalpostId: String,
        orgnr: String,
        behandlet: LocalDateTime,
    ) {
        val insertStatement =
            """INSERT INTO INNTEKTSMELDING (INNTEKTSMELDING_UUID, AKTOR_ID, ORGNUMMER, JOURNALPOST_ID, BEHANDLET, ARBEIDSGIVER_PRIVAT,FNR, DATA)
        VALUES (?, ?, ?, ?, ?, ?,?, ?::jsonb)
        RETURNING *;
            """.trimMargin()

        ds.connection.use {
            val ps = it.prepareStatement(insertStatement)
            ps.setString(1, inntektsmelding.uuid.toString())
            ps.setString(2, inntektsmelding.aktorId)
            ps.setString(3, orgnr)
            ps.setString(4, journalpostId)
            ps.setTimestamp(5, Timestamp.valueOf(behandlet))
            ps.setString(6, null)
            ps.setString(7, inntektsmelding.fnr.toString())
            ps.setString(8, inntektsmelding.data)

            ps.executeQuery()
        }
    }

    fun findById(inntektsmeldingId: UUID): InntektsmeldingEntitet? = findByIds(setOf(inntektsmeldingId)).firstOrNull()

    fun findByIds(inntektsmeldingIder: Set<UUID>): List<InntektsmeldingEntitet> {
        if (inntektsmeldingIder.isEmpty()) return emptyList()
        val query = "SELECT * FROM INNTEKTSMELDING WHERE INNTEKTSMELDING_UUID = ANY(?);"

        val inntektsmeldingIderArray = inntektsmeldingIder.map { it.toString() }.toTypedArray()
        return ds.connection.use {
            val res =
                it
                    .prepareStatement(query)
                    .apply {
                        setArray(1, it.createArrayOf("text", inntektsmeldingIderArray))
                    }.executeQuery()

            resultLoop(res)
        }
    }

    private fun resultLoop(res: ResultSet): ArrayList<InntektsmeldingEntitet> {
        val returnValue = ArrayList<InntektsmeldingEntitet>()

        while (res.next()) {
            returnValue.add(
                InntektsmeldingEntitet(
                    uuid = res.getString("INNTEKTSMELDING_UUID").let(UUID::fromString),
                    aktorId = res.getString("AKTOR_ID"),
                    fnr = Fnr(res.getString("FNR")),
                    data = res.getString("data"),
                ),
            )
        }

        return returnValue
    }
}
