package no.nav.syfo.mapping

import no.nav.syfo.domain.inntektsmelding.EndringIRefusjon
import no.nav.syfo.domain.inntektsmelding.Refusjon
import no.nav.syfo.repository.buildIM
import no.nav.syfo.web.api.Refusjonsendring
import no.nav.syfo.web.api.Refusjonskrav
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class RefusjonskravMapperTest {
    private val im = buildIM()

    @Test
    fun `kun beloep gir tom endringsliste`() {
        val resultat = im.copy(refusjon = Refusjon(BigDecimal(1000), null), endringerIRefusjon = emptyList()).tilRefusjonskrav("id")
        assertEquals(Refusjonskrav(BigDecimal(1000), emptyList()), resultat)
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
                Refusjonsendring(BigDecimal(200), LocalDate.of(2024, 2, 1)),
                Refusjonsendring(BigDecimal(300), LocalDate.of(2024, 3, 1)),
            ),
            resultat.endringer,
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
                Refusjonsendring(BigDecimal(200), LocalDate.of(2024, 2, 1)),
                Refusjonsendring(BigDecimal.ZERO, LocalDate.of(2024, 5, 1)),
            ),
            resultat.endringer,
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
        assertEquals(listOf(Refusjonsendring(BigDecimal(200), dato)), resultat.endringer)
    }

    @Test
    fun `ingen refusjon gir null og tom liste`() {
        val resultat = im.copy(refusjon = Refusjon(), endringerIRefusjon = emptyList()).tilRefusjonskrav("id")
        assertEquals(Refusjonskrav(null, emptyList()), resultat)
    }
}
