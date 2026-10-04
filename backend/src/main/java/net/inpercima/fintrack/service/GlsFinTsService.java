package net.inpercima.fintrack.service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIVersion;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.inpercima.fintrack.config.GlsProperties;

@Service
public class GlsFinTsService {

    private static final Logger LOG = LoggerFactory.getLogger(GlsFinTsService.class);

    private final GlsProperties properties;

    public GlsFinTsService(GlsProperties properties) {
        this.properties = properties;
    }

    public void readAccount(GlsCredentials credentials) throws Exception {
        File passportFile = new File(properties.getPassportFile());

        File parent = passportFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        Properties props = new Properties();
        // 1=error, 2=warn, 3=info, 4=debug. Higher levels may contain sensitive data.
        props.setProperty("log.loglevel.default", String.valueOf(properties.getLogLevel()));

        HBCIUtils.init(
                props,
                new GlsCallback(credentials));

        HBCIUtils.setParam(
                "client.passport.default",
                "PinTan");

        HBCIUtils.setParam(
                "client.passport.PinTan.init",
                "1");

        HBCIPassport passport = null;
        HBCIHandler handler = null;

        try {
            passport = AbstractHBCIPassport.getInstance(passportFile);

            passport.setCountry("DE");
            passport.setHost(properties.getHost());
            passport.setPort(properties.getPort());
            passport.setFilterType("Base64");

            HBCIVersion version = resolveVersion();
            LOG.info("Initialisiere FinTS-Handler: bankCode={}, host={}, port={}, hbciVersion={}",
                    properties.getBankCode(), properties.getHost(), properties.getPort(), version.getId());

            try {
                // Beim ersten Start synchronisiert hbci4j hier die System-ID und die Nutzerdaten (BPD/UPD).
                // Dafür kann die Bank eine TAN-Verfahrenswahl oder Bestätigungen verlangen (siehe Callback).
                handler = new HBCIHandler(
                        version.getId(),
                        passport);
            } catch (Exception e) {
                LOG.error("HBCIHandler konnte nicht erzeugt werden. Mögliche Ursachen: falsche Benutzerkennung/PIN, "
                        + "für FinTS nicht freigeschaltener Zugang, fehlende TAN-Freigabe oder nicht unterstützte "
                        + "HBCI-Version (aktuell {}). Log-Level über gls.log-level erhöhen.", version.getId(), e);
                throw e;
            }

            Konto[] accounts = passport.getAccounts();

            if (accounts == null || accounts.length == 0) {
                throw new IllegalStateException(
                        "GLS hat keine Konten für diesen Zugang geliefert.");
            }

            System.out.println();
            System.out.println("=== GLS Konten ===");

            for (int i = 0; i < accounts.length; i++) {
                Konto konto = accounts[i];

                System.out.printf(
                        "%d) IBAN=%s, BIC=%s, Name=%s%n",
                        i + 1,
                        safe(konto.iban),
                        safe(konto.bic),
                        safe(konto.name));
            }

            Konto konto = accounts[0];

            HBCIJob saldoJob = handler.newJob("SaldoReq");
            saldoJob.setParam("my", konto);
            saldoJob.addToQueue();

            HBCIJob umsatzJob = handler.newJob("KUmsAll");
            umsatzJob.setParam("my", konto);
            umsatzJob.addToQueue();

            System.out.println();
            System.out.println(
                    "Frage Kontostand und Umsätze bei der GLS ab ...");

            HBCIExecStatus status = handler.execute();

            if (!status.isOK()) {
                throw new IllegalStateException(
                        "FinTS-Kommunikation fehlgeschlagen: " + status);
            }

            printBalance(saldoJob);
            printTransactions(umsatzJob);

        } finally {
            if (handler != null) {
                handler.close();
            }

            if (passport != null) {
                passport.close();
            }
        }
    }

    private HBCIVersion resolveVersion() {
        String id = properties.getHbciversion();
        HBCIVersion version = id == null ? null : HBCIVersion.byId(id.trim());
        if (version == null) {
            throw new IllegalStateException("Unbekannte HBCI-Version '" + id + "'. Erlaubt: "
                    + java.util.Arrays.stream(HBCIVersion.values()).map(HBCIVersion::getId)
                            .collect(java.util.stream.Collectors.joining(", ")));
        }
        return version;
    }

    private void printBalance(HBCIJob saldoJob) {
        GVRSaldoReq result = (GVRSaldoReq) saldoJob.getJobResult();

        if (!result.isOK()) {
            System.err.println(
                    "Kontostand konnte nicht abgerufen werden: " + result);
            return;
        }

        if (result.getEntries() == null
                || result.getEntries().length == 0) {

            System.out.println(
                    "Kontostand: keine Daten");

            return;
        }

        Value value = result.getEntries()[0].ready.value;

        System.out.println();
        System.out.println("=== Kontostand ===");
        System.out.println(value);
    }

    private void printTransactions(HBCIJob umsatzJob) {
        GVRKUms result = (GVRKUms) umsatzJob.getJobResult();

        if (!result.isOK()) {
            System.err.println(
                    "Umsätze konnten nicht abgerufen werden: " + result);
            return;
        }

        List<UmsLine> transactions = result.getFlatData();

        System.out.println();
        System.out.println("=== Letzte Umsätze ===");

        if (transactions == null || transactions.isEmpty()) {
            System.out.println(
                    "Keine Umsätze geliefert.");
            return;
        }

        for (UmsLine transaction : transactions) {

            StringBuilder line = new StringBuilder();

            line.append(
                    safe(transaction.valuta));

            if (transaction.value != null) {
                line.append(" | ")
                        .append(transaction.value);
            }

            if (transaction.usage != null
                    && !transaction.usage.isEmpty()) {

                line.append(" | ")
                        .append(
                                String.join(
                                        " ",
                                        transaction.usage));
            }

            System.out.println(line);
        }
    }

    private static String safe(Object value) {
        return value == null
                ? ""
                : value.toString();
    }

    private class GlsCallback
            extends AbstractHBCICallback {

        private final GlsCredentials credentials;

        private GlsCallback(
                GlsCredentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public void log(
                String msg,
                int level,
                Date date,
                StackTraceElement trace) {
            if (msg == null) {
                return;
            }
            switch (level) {
                case HBCIUtils.LOG_ERR -> LOG.error("hbci4j: {}", msg);
                case HBCIUtils.LOG_WARN -> LOG.warn("hbci4j: {}", msg);
                case HBCIUtils.LOG_INFO -> LOG.info("hbci4j: {}", msg);
                default -> LOG.debug("hbci4j: {}", msg);
            }
        }

        @Override
        public void callback(
                HBCIPassport passport,
                int reason,
                String msg,
                int datatype,
                StringBuffer retData) {

            switch (reason) {

                case NEED_PASSPHRASE_LOAD,
                        NEED_PASSPHRASE_SAVE ->

                    replace(
                            retData,
                            credentials.passportPassword());

                case NEED_PT_PIN ->

                    replace(
                            retData,
                            credentials.pin());

                case NEED_BLZ ->

                    replace(
                            retData,
                            properties.getBankCode());

                case NEED_USERID,
                        NEED_CUSTOMERID ->

                    replace(
                            retData,
                            credentials.userId());

                case NEED_PT_SECMECH -> {
                    // Format: "<code>:<Name>|<code>:<Name>"; das erste angebotene Verfahren wählen.
                    LOG.info("Bank bietet TAN-Verfahren an: {}", retData);
                    String first = retData.toString().split("\\|")[0];
                    replace(retData, first.split(":")[0]);
                }

                case NEED_PT_TANMEDIA ->

                    LOG.info("Bank fragt nach TAN-Medium: {}", msg);

                case NEED_PT_TAN -> {
                    LOG.warn("Bank verlangt eine TAN: {}", msg);
                    java.io.Console console = System.console();
                    if (console != null) {
                        replace(retData, console.readLine("TAN: "));
                    } else {
                        throw new IllegalStateException(
                                "Die Bank verlangt eine TAN, aber es ist keine Konsole verfügbar.");
                    }
                }

                case NEED_PT_DECOUPLED, NEED_PT_DECOUPLED_RETRY ->

                    LOG.warn("Bitte Freigabe in der Banking-App bestätigen: {}", msg);

                case NEED_NEW_INST_KEYS_ACK,
                        NEED_INFOPOINT_ACK ->

                    LOG.info("Bank-Hinweis bestätigt: {}", msg);

                case HAVE_INST_MSG ->

                    LOG.info("Nachricht der Bank: {}", msg);

                case WRONG_PIN ->

                    LOG.error("Die PIN wurde von der Bank abgelehnt.");

                case HAVE_ERROR ->

                    LOG.error("FinTS: {}", msg);

                default ->
                    LOG.debug("Unbehandelter Callback-Grund {}: {}", reason, msg);
            }
        }

        @Override
        public void status(
                HBCIPassport passport,
                int statusTag,
                Object[] data) {
            /*
             * Für die erste Version benötigen wir
             * noch keine spezielle Statusanzeige.
             */
        }

        private void replace(
                StringBuffer target,
                String value) {
            target.replace(
                    0,
                    target.length(),
                    value == null
                            ? ""
                            : value);
        }
    }
}
