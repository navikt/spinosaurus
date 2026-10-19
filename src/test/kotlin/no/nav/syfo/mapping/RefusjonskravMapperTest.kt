package no.nav.syfo.mapping

import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.RefusjonEndring
import no.nav.syfo.domain.inntektsmelding.EndringIRefusjon
import no.nav.syfo.domain.inntektsmelding.Refusjon
import no.nav.syfo.repository.buildIM
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Refusjon as Refusjonskrav

class RefusjonskravMapperTest {
    private val im = buildIM()

    @Test
    fun `kun beloep gir tom endringsliste`() {
        val resultat = im.copy(refusjon = Refusjon(BigDecimal(1000), null), endringerIRefusjon = emptyList()).tilRefusjonskrav("id")
        assertEquals(Refusjonskrav(1000.0, emptyList()), resultat)
    }

    @Test
    fun `endringer sorteres paa startdato og ugyldige hoppes over`() {
        val resultat =
            im
                .copy(
                    refusjon = Refusjon(BigDecimal(1000), null),
                    endringerIRefusjon =
                        listOf(
                            EndringIRefusjon(LocalDate.of(2024, 3, 1), BigDecimal(300)),
                            EndringIRefusjon(null, BigDecimal(999)),
                            EndringIRefusjon(LocalDate.of(2024, 2, 1), BigDecimal(200)),
                            EndringIRefusjon(LocalDate.of(2024, 4, 1), null),
                        ),
                ).tilRefusjonskrav("id")
        assertEquals(
            listOf(
                RefusjonEndring(200.0, LocalDate.of(2024, 2, 1)),
                RefusjonEndring(300.0, LocalDate.of(2024, 3, 1)),
            ),
            resultat?.endringer,
        )
    }

    @Test
    fun `opphoersdato blir endring med beloep 0`() {
        val resultat =
            im
                .copy(
                    refusjon = Refusjon(BigDecimal(1000), LocalDate.of(2024, 5, 1)),
                    endringerIRefusjon = listOf(EndringIRefusjon(LocalDate.of(2024, 2, 1), BigDecimal(200))),
                ).tilRefusjonskrav("id")
        assertEquals(
            listOf(
                RefusjonEndring(200.0, LocalDate.of(2024, 2, 1)),
                RefusjonEndring(0.0, LocalDate.of(2024, 5, 1)),
            ),
            resultat?.endringer,
        )
    }

    @Test
    fun `opphoer ignoreres naar endring har samme dato`() {
        val dato = LocalDate.of(2024, 5, 1)
        val resultat =
            im
                .copy(
                    refusjon = Refusjon(BigDecimal(1000), dato),
                    endringerIRefusjon = listOf(EndringIRefusjon(dato, BigDecimal(200))),
                ).tilRefusjonskrav("id")
        assertEquals(listOf(RefusjonEndring(200.0, dato)), resultat?.endringer)
    }

    @Test
    fun `ingen refusjon gir null`() {
        val resultat = im.copy(refusjon = Refusjon(), endringerIRefusjon = emptyList()).tilRefusjonskrav("id")
        assertNull(resultat)
    }

    // Dette er ikke mulig i praksis, men spinosauruskontrakten tillater det.
    @Test
    fun `endringer uten beloep per maaned gir null`() {
        val resultat =
            im
                .copy(
                    refusjon = Refusjon(null, LocalDate.of(2024, 5, 1)),
                    endringerIRefusjon = listOf(EndringIRefusjon(LocalDate.of(2024, 2, 1), BigDecimal(200))),
                ).tilRefusjonskrav("id")
        assertNull(resultat)
    }
}
