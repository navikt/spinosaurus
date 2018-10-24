package no.nav.syfo.service;

import no.nav.syfo.consumer.ws.AktoridConsumer;
import no.nav.syfo.consumer.ws.BehandleSakConsumer;
import no.nav.syfo.consumer.ws.BehandlendeEnhetConsumer;
import no.nav.syfo.consumer.ws.OppgavebehandlingConsumer;
import no.nav.syfo.domain.Inntektsmelding;
import no.nav.syfo.domain.Oppgave;
import no.nav.syfo.domain.Sykepengesoknad;
import no.nav.syfo.domain.Sykmelding;
import no.nav.syfo.repository.SykepengesoknadDAO;
import no.nav.syfo.repository.SykmeldingDAO;
import no.nav.syfo.util.Metrikk;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SaksbehandlingServiceTest {

    @Mock
    private OppgavebehandlingConsumer oppgavebehandlingConsumer;
    @Mock
    private BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    @Mock
    private BehandleSakConsumer behandleSakConsumer;
    @Mock
    private SykepengesoknadDAO sykepengesoknadDAO;
    @Mock
    private AktoridConsumer aktoridConsumer;
    @Mock
    private SykmeldingDAO sykmeldingDAO;

    @InjectMocks
    private SaksbehandlingService saksbehandlingService;

    @Before
    public void setup() {
        when(aktoridConsumer.hentAktoerIdForFnr(anyString())).thenReturn("aktorid");
        when(sykmeldingDAO.hentSykmeldingerForOrgnummer(anyString(), anyString()))
                .thenReturn(singletonList(Sykmelding.builder().orgnummer("orgnummer").id(123).build()));
        when(behandlendeEnhetConsumer.hentGeografiskTilknytning(anyString())).thenReturn("Geografisktilknytning");
        when(behandlendeEnhetConsumer.hentBehandlendeEnhet(anyString())).thenReturn("behandlendeenhet1234");
        when(oppgavebehandlingConsumer.opprettOppgave(anyString(), any())).thenReturn("1234");
        when(behandleSakConsumer.opprettSak("fnr")).thenReturn("opprettetSaksId");
        when(sykepengesoknadDAO.hentSykepengesoknaderForPerson(anyString(), anyInt()))
                .thenReturn(new ArrayList<Sykepengesoknad>() {{
                                add(Sykepengesoknad.builder()
                                        .fom(LocalDate.of(2018, 1, 1))
                                        .tom(LocalDate.of(2018, 2, 1))
                                        .saksId("saksId")
                                        .oppgaveId("oppgaveId")
                                        .build());
                            }}
                );
    }

    private Inntektsmelding lagInntektsmelding() {
        return Inntektsmelding.builder()
                .fnr("fnr")
                .arbeidsgiverOrgnummer("orgnummer")
                .journalpostId("journalpostId")
                .arbeidsforholdId(null)
                .arsakTilInnsending("Ny")
                .build();
    }

    @Test
    public void returnererSaksIdOmSakFinnes() {
        String saksId = saksbehandlingService.behandleInntektsmelding(lagInntektsmelding());

        assertThat(saksId).isEqualTo("saksId");
    }

    @Test
    public void oppretterSakOmSakIkkeFinnes() {
        when(sykmeldingDAO.hentSykmeldingerForOrgnummer(anyString(), anyString())).thenReturn(emptyList());

        String saksId = saksbehandlingService.behandleInntektsmelding(lagInntektsmelding());

        assertThat(saksId).isEqualTo("opprettetSaksId");
    }

    @Test
    public void oppretterOppgaveForSak() {
        saksbehandlingService.behandleInntektsmelding(lagInntektsmelding());

        verify(oppgavebehandlingConsumer).opprettOppgave(anyString(), any(Oppgave.class));
    }
}
