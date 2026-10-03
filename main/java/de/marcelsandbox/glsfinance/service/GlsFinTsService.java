package de.marcelsandbox.glsfinance.service;

import de.marcelsandbox.glsfinance.config.GlsProperties;
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
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class GlsFinTsService {

    private final GlsProperties properties;

    public GlsFinTsService(GlsProperties properties) {
        this.properties = properties;
    }

    public void readAccount() throws Exception {
        validateConfiguration();

        File passportFile = new File(properties.getPassportFile());
        File parent = passportFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        Properties props = new Properties();
        HBCIUtils.init(props, new GlsCallback());

        HBCIUtils.setParam("client.passport.default", "PinTan");
        HBCIUtils.setParam("client.passport.PinTan.init", "1");

        HBCIPassport passport = null;
        HBCIHandler handler = null;

        try {
            passport = AbstractHBCIPassport.getInstance(passportFile);

            passport.setCountry("DE");
            passport.setHost(properties.getHost());
            passport.setPort(properties.getPort());
            passport.setFilterType("Base64");

            HBCIVersion version = HBCIVersion.HBCI_300;
            handler = new HBCIHandler(version.getId(), passport);

            Konto[] accounts = passport.getAccounts();

            if (accounts == null || accounts.length == 0) {
                throw new IllegalStateException("GLS hat keine Konten für diesen Zugang geliefert.");
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
                    safe(konto.name)
                );
            }

            Konto konto = accounts[0];

            HBCIJob saldoJob = handler.newJob("SaldoReq");
            saldoJob.setParam("my", konto);
            saldoJob.addToQueue();

            HBCIJob umsatzJob = handler.newJob("KUmsAll");
            umsatzJob.setParam("my", konto);
            umsatzJob.addToQueue();

            System.out.println();
            System.out.println("Frage Kontostand und Umsätze bei der GLS ab ...");

            HBCIExecStatus status = handler.execute();

            if (!status.isOK()) {
                throw new IllegalStateException("FinTS-Kommunikation fehlgeschlagen: " + status);
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

    private void printBalance(HBCIJob saldoJob) {
        GVRSaldoReq result = (GVRSaldoReq) saldoJob.getJobResult();

        if (!result.isOK()) {
            System.err.println("Kontostand konnte nicht abgerufen werden: " + result);
            return;
        }

        if (result.getEntries() == null || result.getEntries().length == 0) {
            System.out.println("Kontostand: keine Daten");
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
            System.err.println("Umsätze konnten nicht abgerufen werden: " + result);
            return;
        }

        List<UmsLine> transactions = result.getFlatData();

        System.out.println();
        System.out.println("=== Letzte Umsätze ===");

        if (transactions == null || transactions.isEmpty()) {
            System.out.println("Keine Umsätze geliefert.");
            return;
        }

        for (UmsLine transaction : transactions) {
            StringBuilder line = new StringBuilder();

            line.append(safe(transaction.valuta));

            if (transaction.value != null) {
                line.append(" | ").append(transaction.value);
            }
            if (transaction.usage != null && !transaction.usage.isEmpty()) {
                line.append(" | ").append(String.join(" ", transaction.usage));
            }

            System.out.println(line);
        }
    }

    private void validateConfiguration() {
        if (properties.getUserId() == null || properties.getUserId().isBlank()) {
            throw new IllegalStateException("GLS_USER_ID fehlt.");
        }

        if (properties.getPin() == null || properties.getPin().isBlank()) {
            throw new IllegalStateException("GLS_PIN fehlt.");
        }
    }

    private static String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private class GlsCallback extends AbstractHBCICallback {

        @Override
        public void log(String msg, int level, Date date, StackTraceElement trace) {
            // FinTS-Protokoll kann sehr ausführlich sein. Für den ersten Test
            // geben wir es nicht komplett auf stdout aus.
        }

        @Override
        public void callback(
                HBCIPassport passport,
                int reason,
                String msg,
                int datatype,
                StringBuffer retData) {

            switch (reason) {
                case NEED_PASSPHRASE_LOAD, NEED_PASSPHRASE_SAVE ->
                    replace(retData, properties.getPassportPassword());

                case NEED_PT_PIN ->
                    replace(retData, properties.getPin());

                case NEED_BLZ ->
                    replace(retData, properties.getBankCode());

                case NEED_USERID ->
                    replace(retData, properties.getUserId());

                case NEED_CUSTOMERID ->
                    replace(retData, properties.getUserId());

                case HAVE_ERROR ->
                    System.err.println("FinTS: " + msg);

                default -> {
                    // Für die reine Leseabfrage benötigen wir zunächst
                    // keine weiteren Eingaben.
                }
            }
        }

        @Override
        public void status(HBCIPassport passport, int statusTag, Object[] data) {
            // Noch keine spezielle Statusanzeige.
        }

        private void replace(StringBuffer target, String value) {
            target.replace(0, target.length(), value == null ? "" : value);
        }
    }
}
