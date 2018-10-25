package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
@Getter
public class Inntektsmelding {
    String fnr;
    String arbeidsgiverOrgnummer;
    String journalpostId;
    String arbeidsforholdId;
    String arsakTilInnsending;
}
