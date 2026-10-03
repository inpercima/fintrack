package de.marcelsandbox.glsfinance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gls")
public class GlsProperties {

    private String bankCode = "43060967";
    private String userId;
    private String pin;
    private String host = "fints1.atruvia.de";
    private int port = 443;
    private String passportFile = "./data/gls-passport.dat";
    private String passportPassword = "change-me-local-only";
    private String hbciversion = "300";

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
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

    public String getPassportPassword() {
        return passportPassword;
    }

    public void setPassportPassword(String passportPassword) {
        this.passportPassword = passportPassword;
    }

    public String getHbciversion() {
        return hbciversion;
    }

    public void setHbciversion(String hbciversion) {
        this.hbciversion = hbciversion;
    }
}
