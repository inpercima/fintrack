package de.marcelsandbox.glsfinance.runner;

import de.marcelsandbox.glsfinance.service.GlsFinTsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GlsRunner implements CommandLineRunner {

    private final GlsFinTsService service;

    public GlsRunner(GlsFinTsService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) throws Exception {
        service.readAccount();
    }
}
