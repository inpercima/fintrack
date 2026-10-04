package net.inpercima.fintrack.service;

import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Scanner;

@Component
public class GlsCredentialPrompt {

    public GlsCredentials requestCredentials() {
        System.out.println();
        System.out.println("=== Fintrack ===");
        System.out.println();
        System.out.println("GLS-Zugangsdaten werden nur für diese Sitzung verwendet.");
        System.out.println();

        String userId = readLine("GLS VR-NetKey / Alias: ");
        String pin = readPassword("GLS PIN: ");
        String passportPassword = readPassword("Lokales FinTS-Passwort: ");

        System.out.println();

        return new GlsCredentials(
                userId,
                pin,
                passportPassword);
    }

    private String readLine(String prompt) {
        Console console = System.console();

        if (console != null) {
            return console.readLine(prompt);
        }

        System.out.print(prompt);

        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private String readPassword(String prompt) {
        Console console = System.console();

        if (console != null) {
            char[] password = console.readPassword(prompt);
            return new String(password);
        }

        /*
         * Fallback für IDE-Konsolen, die keine java.io.Console anbieten.
         * Dort kann die Eingabe leider nicht zuverlässig verborgen werden.
         */
        System.out.print(prompt);

        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}
