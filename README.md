# GLS Finance

Erster read-only Test für den Zugriff auf ein eigenes GLS-Konto über FinTS/HBCI und HBCI4Java.

## Voraussetzungen

- Java 25
- Maven 3.9+
- GLS-Onlinebanking-Zugang
- FinTS/HBCI PIN/TAN Zugang

Die GLS veröffentlicht aktuell für PIN/TAN:

- BLZ: `43060967`
- FinTS: `3.0`
- Host: `fints1.atruvia.de`
- Port: `443`
- Filter: `Base64`

## WICHTIG

Dieses Projekt ist absichtlich read-only. Es enthält aktuell keinen Code zum Ausführen von Überweisungen.

Die Zugangsdaten niemals in Git committen.

## Konfiguration

Zum Beispiel im Terminal:

```bash
export GLS_USER_ID='DEIN_VR_NETKEY_ODER_ALIAS'
export GLS_PIN='DEINE_FINTS_PIN'
export GLS_PASSPORT_PASSWORD='EIN_LOKALES_PASSWORT'
```

Dann:

```bash
./mvnw spring-boot:run
```

oder, falls kein Maven Wrapper vorhanden ist:

```bash
mvn spring-boot:run
```

Beim ersten Start wird unter `data/gls-passport.dat` ein lokaler FinTS-Passport angelegt.

## Hinweis zur GLS-PIN

Die GLS unterscheidet Onlinebanking-PIN/TAN und FinTS/HBCI-Verfahren. Falls dein Zugang nicht akzeptiert wird, prüfen wir als Nächstes, welches konkrete FinTS-Verfahren für dein Konto freigeschaltet ist.

## Nächste Schritte

1. Verbindung testen
2. Konten sauber modellieren
3. Umsätze in eigene DTOs übertragen
4. REST-API hinzufügen
5. Zeitraum für Umsatzabfragen kontrollieren
6. Optional MySQL
7. Erst ganz am Ende über schreibende Bankoperationen sprechen
