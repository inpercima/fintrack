package net.inpercima.fintrack.runner;

import net.inpercima.fintrack.service.GlsCredentialPrompt;
import net.inpercima.fintrack.service.GlsCredentials;
import net.inpercima.fintrack.service.GlsFinTsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GlsRunner implements CommandLineRunner {

    private final GlsCredentialPrompt credentialPrompt;
    private final GlsFinTsService glsFinTsService;

    public GlsRunner(
            GlsCredentialPrompt credentialPrompt,
            GlsFinTsService glsFinTsService) {
        this.credentialPrompt = credentialPrompt;
        this.glsFinTsService = glsFinTsService;
    }

    @Override
    public void run(String... args) throws Exception {
        GlsCredentials credentials = credentialPrompt.requestCredentials();

        glsFinTsService.readAccount(credentials);
    }
}
