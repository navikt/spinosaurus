package no.nav.syfo.repository

import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.syfo.dto.InntektsmeldingEntitet
import java.sql.ResultSet
import java.sql.Timestamp
import javax.sql.DataSource

interface InntektsmeldingRepository {
    fun lagreInnteksmelding(innteksmelding: InntektsmeldingEntitet): InntektsmeldingEntitet

    fun findByJournalpost(journalpostId: String): InntektsmeldingEntitet?

    fun findByUuid(uuid: String): InntektsmeldingEntitet?

    fun findByUuids(uuids: List<String>): List<InntektsmeldingEntitet>

    fun deleteAll()
}

class InntektsmeldingRepositoryMock : InntektsmeldingRepository {
    private val mockrepo = mutableSetOf<InntektsmeldingEntitet>()

    override fun lagreInnteksmelding(innteksmelding: InntektsmeldingEntitet): InntektsmeldingEntitet {
        mockrepo.add(innteksmelding)
        return innteksmelding
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

    override fun lagreInnteksmelding(innteksmelding: InntektsmeldingEntitet): InntektsmeldingEntitet {
        val insertStatement =
            """INSERT INTO INNTEKTSMELDING (INNTEKTSMELDING_UUID, AKTOR_ID, ORGNUMMER, JOURNALPOST_ID, BEHANDLET, ARBEIDSGIVER_PRIVAT,FNR, DATA)
        VALUES (?, ?, ?, ?, ?, ?,?, ?::jsonb)
        RETURNING *;
            """.trimMargin()

        ds.connection.use {
            val ps = it.prepareStatement(insertStatement)
            ps.setString(1, innteksmelding.uuid)
            ps.setString(2, innteksmelding.aktorId)
            ps.setString(3, innteksmelding.orgnummer)
            ps.setString(4, innteksmelding.journalpostId)
            ps.setTimestamp(5, Timestamp.valueOf(innteksmelding.behandlet))
            ps.setString(6, innteksmelding.arbeidsgiverPrivat)
            ps.setString(7, innteksmelding.fnr.toString())
            ps.setString(8, innteksmelding.data)

            ps.executeQuery()
        }

        return innteksmelding
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
                    orgnummer = res.getString("ORGNUMMER"),
                    journalpostId = res.getString("JOURNALPOST_ID"),
                    behandlet = res.getTimestamp("BEHANDLET").toLocalDateTime(),
                    arbeidsgiverPrivat = res.getString("ARBEIDSGIVER_PRIVAT"),
                    fnr = Fnr(res.getString("FNR")),
                    data = res.getString("data"),
                ),
            )
        }

        return returnValue
    }
}
