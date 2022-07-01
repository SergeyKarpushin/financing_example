package sk.financing.services;

import lu.crx.test.financing.entities.*;
import sk.financing.entities.*;
import sk.financing.entities.extra.PurchaserCreditorRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;

import static sk.financing.Constants.BPS_DAILY_RATE_COEFFICIENT;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FinancingServiceTest {

    @Autowired
    private FinancingService financingService;

    @Autowired
    private CacheService cacheService;

    private Creditor creditor1;
    private Purchaser purchaser1;
    private Purchaser purchaser2;

    private List<Purchaser> purchasers;
    private Debtor debtor1;
    private Invoice invoice1;

    @BeforeEach
    void setUp() {
        creditor1 = Creditor.builder()
                .name("Test Creditor1")
                .maxFinancingRateInBps(5)
                .build();
        purchaser1 = Purchaser.builder()
                .name("Test Purchaser1")
                .minimumFinancingTermInDays(10)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor1)
                        .annualRateInBps(50)
                        .build())
                .build();
        purchaser2 = Purchaser.builder()
                .name("Test Purchaser2")
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor1)
                        .annualRateInBps(40)
                        .build())
                .build();
        debtor1 = Debtor.builder()
                .name("Chocolate Factory")
                .build();
        invoice1 = Invoice.builder()
                .creditor(creditor1)
                .debtor(debtor1)
                .valueInCents(1000000)
                .maturityDate(LocalDate.now().plusDays(30))
                .build();

        purchasers = Arrays.asList(purchaser1, purchaser2);

        List<PurchaserCreditorRate> purchasers = Arrays.asList(
                PurchaserCreditorRate.builder()
                        .purchaser(purchaser1)
                        .rateInBps(purchaser1.getPurchaserFinancingSettings().iterator().next().getAnnualRateInBps() * BPS_DAILY_RATE_COEFFICIENT)
                        .build(),
                PurchaserCreditorRate.builder()
                        .purchaser(purchaser2)
                        .rateInBps(purchaser2.getPurchaserFinancingSettings().iterator().next().getAnnualRateInBps() * BPS_DAILY_RATE_COEFFICIENT)
                        .build()
        );
        cacheService.getCreditorPurchaserRateMap().put(creditor1.getId(), purchasers);
    }

    @Test
    void testFindPurchaserShouldReturnPurchaser2() {
        Purchaser purchaser = financingService.findPurchaser(invoice1);
        assertEquals(purchaser2, purchaser);
    }

    @Test
    void testInterestRate() {
        Purchaser purchaser = financingService.findPurchaser(invoice1);
        double rate = purchaser.getPurchaserFinancingSettings().stream()
                .filter(pfs -> pfs.getCreditor().getId() == invoice1.getCreditor().getId())
                .findFirst()
                .get()
                .getAnnualRateInBps() * BPS_DAILY_RATE_COEFFICIENT;

        double interest = invoice1.getValueInCents() * rate * 0.0001;

        assertEquals(333.3333333333333, interest);
    }
}