package sk.financing.services;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import sk.financing.entities.Creditor;
import sk.financing.entities.FinancingRecord;
import sk.financing.entities.extra.PurchaserCreditorRate;
import sk.financing.repository.InvoiceRepository;
import sk.financing.repository.PurchaserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.financing.entities.Invoice;
import sk.financing.entities.Purchaser;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
public class FinancingService {

    // maximal amount of invoices to process at one finance round
    @Value("${sk.invoices.financing.batch.size}")
    private int invoicesBatchSize;

    private final InvoiceRepository invoiceRepository;
    private final PurchaserRepository purchaserRepository;

    private EntityManager entityManager;

    private CacheService cacheService;

    public FinancingService(InvoiceRepository invoiceRepository,
                            PurchaserRepository purchaserRepository,
                            EntityManager entityManager,
                            CacheService cacheService) {
        this.invoiceRepository = invoiceRepository;
        this.purchaserRepository = purchaserRepository;
        this.entityManager = entityManager;
        this.cacheService = cacheService;
    }

    @Transactional
    public void finance() {
        log.info("Running the financing");

        // This is the financing algorithm that needs to be implemented according to the specification.
        // For every invoice that may be financed, pick the winning purchaser. Calculate the amount of money received
        // by the creditor and the interest of the purchaser, persist calculated data.
        // The invoices will keep coming in, and the financing will be repeated multiple times. Make sure
        // already financed invoices won't be financed the second time.
        // You may improve the data structure as needed (add new entities, fields, change the mapping, etc.)
        // Take performance considerations into account: the total amount of invoices in the database could be
        // tens of millions of records, with tens of thousands actually financeable in each round.

        // TODO: lock selected invoices in order to prevent possible
        //       parallel processing from other finance() invocations
        List<Invoice> invoices = invoiceRepository.findByIsFinancedFalse();
        if (invoices.isEmpty()) {
            log.warn("There are no invoices to be financed -> exiting financing");
            return;
        }

        Date processingDate = Calendar.getInstance().getTime();

        // as volumes could be large it makes sense to process invoices in parallel
        // but using invoices.parallelStream() we got entityManager out of transaction error
        // thus we need to deal with it properly
        invoices.stream().forEach(invoice -> {
            Purchaser selectedPurchaser = findPurchaser(invoice);
            if (selectedPurchaser == null) {
                log.warn("No Purchaser has matched the invoice " + invoice.getId() + ". Moving to the next invoice");
                return;
            }

            // save financing record for current invoice and set invoice to financed
            entityManager.persist(FinancingRecord.builder()
                    .invoice(invoice)
                    .purchaser(selectedPurchaser)
                    .creditor(invoice.getCreditor())
                    .processingDate(processingDate)
                    .build());

            invoice.setFinanced(true);
            entityManager.persist(invoice);
        });
    }

    public Purchaser findPurchaser(Invoice invoice) {
        Creditor creditor = invoice.getCreditor();

        List<PurchaserCreditorRate> purchasers = cacheService.getCreditorPurchaserRateMap().get(creditor.getId());
        if (purchasers == null || purchasers.isEmpty()) {
            log.warn("No purchasers found for the creditor '" + creditor.getName() + "'. Moving to the next invoice");
            return null;
        }
        Purchaser selectedPurchaser = null;
        double minRate = 0d;
        LocalDate dateNow = LocalDate.now();
        for (PurchaserCreditorRate purchaser : purchasers) {
            if (DAYS.between(dateNow, invoice.getMaturityDate()) >= purchaser.getPurchaser().getMinimumFinancingTermInDays()
                    && purchaser.getRateInBps() <= creditor.getMaxFinancingRateInBps()) {
                if (selectedPurchaser == null || purchaser.getRateInBps() < minRate) {
                    selectedPurchaser = purchaser.getPurchaser();
                    minRate = purchaser.getRateInBps();
                }
            }
        }
        return selectedPurchaser;
    }
}

