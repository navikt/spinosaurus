package no.nav.syfo.repository

import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.syfo.dto.InntektsmeldingEntitet
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource

interface InntektsmeldingRepository {
    fun lagreInnteksmelding(
        innteksmelding: InntektsmeldingEntitet,
        journalpostId: String,
        orgnr: String,
        behandlet: LocalDateTime,
    )

    fun findByJournalpost(journalpostId: String): InntektsmeldingEntitet?

    fun findByUuid(uuid: String): InntektsmeldingEntitet?

    fun findByUuids(uuids: List<String>): List<InntektsmeldingEntitet>

    fun deleteAll()
}

class InntektsmeldingRepositoryMock : InntektsmeldingRepository {
    private val mockrepo = mutableSetOf<InntektsmeldingEntitet>()

    override fun lagreInnteksmelding(
        innteksmelding: InntektsmeldingEntitet,
        journalpostId: String,
        orgnr: String,
        behandlet: LocalDateTime,
    ) {
        mockrepo.add(innteksmelding)
    }

    override fun findByUuid(uuid: String): InntektsmeldingEntitet? = findByUuids((listOf(uuid))).firstOrNull()

    override fun findByUuids(uuids: List<String>): List<InntektsmeldingEntitet> = mockrepo.filter { it.uuid in uuids }

    override fun findByJournalpost(journalpostId: String): InntektsmeldingEntitet? {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {}
}

class InntektsmeldingRepositoryImp(
    private val ds: DataSource,
) : InntektsmeldingRepository {
    override fun findByJournalpost(journalpostId: String): InntektsmeldingEntitet? {
        val findByAktorId = "SELECT * FROM INNTEKTSMELDING WHERE JOURNALPOST_ID = ?;"

        return ds.connection.use {
            val res =
                it
                    .prepareStatement(findByAktorId)
                    .apply {
                        setString(1, journalpostId)
                    }.executeQuery()
            resultLoop(res).firstOrNull()
        }
    }

    override fun findByUuid(uuid: String): InntektsmeldingEntitet? = findByUuids(listOf(uuid)).firstOrNull()

    override fun findByUuids(uuids: List<String>): List<InntektsmeldingEntitet> {
        if (uuids.isEmpty()) return emptyList()
        val query = "SELECT * FROM INNTEKTSMELDING WHERE INNTEKTSMELDING_UUID = ANY(?);"

        return ds.connection.use {
            val res =
                it
                    .prepareStatement(query)
                    .apply {
                        setArray(1, it.createArrayOf("text", uuids.toTypedArray()))
                    }.executeQuery()

            resultLoop(res)
        }
    }

    override fun lagreInnteksmelding(
        innteksmelding: InntektsmeldingEntitet,
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
            ps.setString(1, innteksmelding.uuid)
            ps.setString(2, innteksmelding.aktorId)
            ps.setString(3, orgnr)
            ps.setString(4, journalpostId)
            ps.setTimestamp(5, Timestamp.valueOf(behandlet))
            ps.setString(6, null)
            ps.setString(7, innteksmelding.fnr.toString())
            ps.setString(8, innteksmelding.data)

            ps.executeQuery()
        }
    }

    override fun deleteAll() {
        val deleteStatememnt = "DELETE FROM INNTEKTSMELDING;"
        ds.connection.use {
            it.prepareStatement(deleteStatememnt).executeUpdate()
        }
    }

    private fun resultLoop(res: ResultSet): ArrayList<InntektsmeldingEntitet> {
        val returnValue = ArrayList<InntektsmeldingEntitet>()

        while (res.next()) {
            returnValue.add(
                InntektsmeldingEntitet(
                    uuid = res.getString("INNTEKTSMELDING_UUID"),
                    aktorId = res.getString("AKTOR_ID"),
                    fnr = Fnr(res.getString("FNR")),
                    data = res.getString("data"),
                ),
            )
        }

        return returnValue
    }
}
