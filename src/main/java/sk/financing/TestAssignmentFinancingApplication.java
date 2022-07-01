package sk.financing;

import sk.financing.services.CacheService;
import sk.financing.services.FinancingService;
import sk.financing.services.ReportingService;
import sk.financing.services.SeedingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestAssignmentFinancingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestAssignmentFinancingApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(
            SeedingService seedingService,
            FinancingService financingService,
            ReportingService reportingService,
            CacheService cacheService) {

        return args -> {
            // seeding master data - creditors, debtors and purchasers
            seedingService.seedMasterData();

            // seeding the invoices
            seedingService.seedInvoices();

            cacheService.initCaches();

            // running the financing
            financingService.finance();

            // printing out the financing results
            reportingService.printFinancingReport();

        };
    }

}
