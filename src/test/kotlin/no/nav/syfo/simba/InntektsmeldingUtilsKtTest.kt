package no.nav.syfo.simba

import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntektsmelding
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InntektsmeldingUtilsKtTest {
    @Test
    fun `selvbestemt skal sendes til Spleis`() {
        val selvbestemt =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.Selvbestemt(UUID.randomUUID()),
            )

        assertTrue(selvbestemt.skalSendesTilSpleis())
    }

    @Test
    fun `fisker skal _ikke_ sendes til Spleis`() {
        val fisker =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.Fisker(UUID.randomUUID()),
            )

        assertFalse(fisker.skalSendesTilSpleis())
    }

    @Test
    fun `uten arbeidsforhold skal _ikke_ sendes til Spleis`() {
        val utenArbeidsforhold =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.UtenArbeidsforhold(UUID.randomUUID()),
            )

        assertFalse(utenArbeidsforhold.skalSendesTilSpleis())
    }

    @Test
    fun `behandlingsdager skal _ikke_ sendes til Spleis`() {
        val behandlingsdager =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.Behandlingsdager(UUID.randomUUID()),
            )

        assertFalse(behandlingsdager.skalSendesTilSpleis())
    }

    @Test
    fun `forespurt IM uten forespurt AGP skal sendes til Spleis`() {
        val imMedForespurtAgp =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.Forespurt(
                        id = UUID.randomUUID(),
                        erAgpForespurt = true,
                    ),
                agp = null,
            )

        assertTrue(imMedForespurtAgp.skalSendesTilSpleis())
    }

    @Test
    fun `forespurt IM med forespurt AGP skal sendes til Spleis`() {
        val imMedForespurtAgp =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.Forespurt(
                        id = UUID.randomUUID(),
                        erAgpForespurt = true,
                    ),
                agp = mockAgp(),
            )

        assertTrue(imMedForespurtAgp.skalSendesTilSpleis())
    }

    @Test
    fun `forespurt IM uten ikke-forespurt AGP skal sendes til Spleis`() {
        val imMedForespurtAgp =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.Forespurt(
                        id = UUID.randomUUID(),
                        erAgpForespurt = false,
                    ),
                agp = null,
            )

        assertTrue(imMedForespurtAgp.skalSendesTilSpleis())
    }

    @Test
    fun `forespurt IM med ikke-forespurt AGP skal _ikke_ sendes til Spleis`() {
        val imMedForespurtAgp =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.Forespurt(
                        id = UUID.randomUUID(),
                        erAgpForespurt = false,
                    ),
                agp = mockAgp(),
            )

        assertFalse(imMedForespurtAgp.skalSendesTilSpleis())
    }

    @Test
    fun `forespurt, ekstern IM uten forespurt AGP skal sendes til Spleis`() {
        val imMedForespurtAgp =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.ForespurtEkstern(
                        id = UUID.randomUUID(),
                        erAgpForespurt = true,
                        _avsenderSystem = mockAvsenderSystem(),
                    ),
                agp = null,
            )

        assertTrue(imMedForespurtAgp.skalSendesTilSpleis())
    }

    @Test
    fun `forespurt, ekstern IM med forespurt AGP skal sendes til Spleis`() {
        val imMedForespurtAgp =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.ForespurtEkstern(
                        id = UUID.randomUUID(),
                        erAgpForespurt = true,
                        _avsenderSystem = mockAvsenderSystem(),
                    ),
                agp = mockAgp(),
            )

        assertTrue(imMedForespurtAgp.skalSendesTilSpleis())
    }

    @Test
    fun `forespurt, ekstern IM uten ikke-forespurt AGP skal sendes til Spleis`() {
        val imMedForespurtAgp =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.ForespurtEkstern(
                        id = UUID.randomUUID(),
                        erAgpForespurt = false,
                        _avsenderSystem = mockAvsenderSystem(),
                    ),
                agp = null,
            )

        assertTrue(imMedForespurtAgp.skalSendesTilSpleis())
    }

    @Test
    fun `forespurt, ekstern IM med ikke-forespurt AGP skal _ikke_ sendes til Spleis`() {
        val imMedForespurtAgp =
            mockInntektsmelding().copy(
                type =
                    Inntektsmelding.Type.ForespurtEkstern(
                        id = UUID.randomUUID(),
                        erAgpForespurt = false,
                        _avsenderSystem = mockAvsenderSystem(),
                    ),
                agp = mockAgp(),
            )

        assertFalse(imMedForespurtAgp.skalSendesTilSpleis())
    }
}
