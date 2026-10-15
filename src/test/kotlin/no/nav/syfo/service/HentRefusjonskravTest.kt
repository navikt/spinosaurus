package no.nav.syfo.service

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.syfo.domain.inntektsmelding.Refusjon
import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.repository.buildIM
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class HentRefusjonskravTest {
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    private val repository = mockk<InntektsmeldingRepository>()
    private val service = InntektsmeldingService(repository, objectMapper)

    @Test
    fun `ukjent id gir null og duplikate ider slaas sammen`() {
        val im = buildIM().copy(refusjon = Refusjon(BigDecimal(1000), null), endringerIRefusjon = emptyList())
        val entitet =
            InntektsmeldingEntitet(
                uuid = "kjent",
                aktorId = "aktor",
                journalpostId = "jp",
                orgnummer = "org",
                arbeidsgiverPrivat = null,
                behandlet = LocalDateTime.now(),
                fnr = Fnr("28014026691"),
                data = objectMapper.writeValueAsString(im),
            )
        every { repository.findByUuids(listOf("kjent", "ukjent")) } returns listOf(entitet)

        val resultat = service.hentRefusjonskrav(listOf("kjent", "ukjent", "kjent"))

        assertEquals(listOf("kjent", "ukjent"), resultat.map { it.inntektsmeldingId })
        assertEquals(BigDecimal(1000), resultat[0].refusjonskrav?.beloepPerMaaned)
        assertNull(resultat[1].refusjonskrav)
    }

    @Test
    fun `tom liste gir tom liste`() {
        every { repository.findByUuids(emptyList()) } returns emptyList()
        assertTrue(service.hentRefusjonskrav(emptyList()).isEmpty())
    }
}
