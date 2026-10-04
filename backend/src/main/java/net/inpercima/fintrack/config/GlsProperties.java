package net.inpercima.fintrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gls")
public class GlsProperties {

    private String bankCode = "43060967";
    private String host = "fints1.atruvia.de";
    private int port = 443;
    private String passportFile = "./data/gls-passport.dat";
    private String hbciversion = "300";
    private int logLevel = 3;

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassportFile() {
        return passportFile;
    }

    public void setPassportFile(String passportFile) {
        this.passportFile = passportFile;
    }

    public String getHbciversion() {
        return hbciversion;
    }

    public void setHbciversion(String hbciversion) {
        this.hbciversion = hbciversion;
    }
}
