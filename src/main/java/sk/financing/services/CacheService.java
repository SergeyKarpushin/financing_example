package sk.financing.services;

import sk.financing.entities.Purchaser;
import sk.financing.entities.extra.PurchaserCreditorRate;
import sk.financing.repository.PurchaserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static sk.financing.Constants.BPS_DAILY_RATE_COEFFICIENT;

@Service
public class CacheService {

    @Autowired
    private PurchaserRepository purchaserRepository;

    private final Map<Long, List<PurchaserCreditorRate>> creditorPurchaserRateMap = new ConcurrentHashMap<>();

    public Map<Long, List<PurchaserCreditorRate>> getCreditorPurchaserRateMap() {
        return creditorPurchaserRateMap;
    }

    @Transactional
    public void initCaches() {
        List<Purchaser> purchasers = purchaserRepository.findAll();
        purchasers.forEach(p -> p.getPurchaserFinancingSettings().forEach(pfs -> {
            PurchaserCreditorRate purchaserCreditorRate = PurchaserCreditorRate.builder()
                    .purchaser(p)
                    .rateInBps(pfs.getAnnualRateInBps() * BPS_DAILY_RATE_COEFFICIENT)
                    .build();
            getCreditorPurchaserRateMap().computeIfAbsent(pfs.getCreditor().getId(), k -> new ArrayList<>())
                    .add(purchaserCreditorRate);
        }));

        //System.out.println(getCreditorPurchaserRateMap());
    }
}
