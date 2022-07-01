## Sample Financing application

The application in this repository: 
* creates an H2 database in the root directory of the project;
* sets up the database structure according to the entities;
* seeds some initial data, runs the financing algorithm and prints out the report.

### Example

The rates are measured in bps (basis points). One basis point is 0,01%, or 0,0001.

Suppose today is 2021-05-27. We have 2 purchasers Purchaser1 and Purchaser2 and one creditor Creditor1.
Purchaser1 has set up the annual rate of 50 bps for the Creditor1. Purchaser2 has set up 40 bps for the same creditor.
The Creditor1 has set up 4 bps as maximum financing rate.

The Creditor1 has a single invoice with the value of 10 000,00 EUR and the maturity date of 2021-06-26. 
The financing term of the invoice (duration between today and maturity date) is then 30 days. 

When we run the financing, the financing rate for Purchaser1 for this invoice should be calculated as 
50 bps * 30 days / 360 days/year = 4,167 bps, the financing rate for Purchaser2 would be 
40 bps * 30 days / 360 days/year = 3,333 bps. The Purchaser1's financing rate is greater than the maximum financing
rate set up by the Creditor, so only Purchaser2 wins the financing of the invoice.

The Purchaser2's interest is then calculated as 10 000,00 EUR * 3,333 bps * 0,0001 = 3,33 EUR. 
The creditor payment is 10 000,00 EUR - 3,33 EUR = 9 996,67 EUR.

The reporting should then produce something like:

```
Purchaser2  Creditor1   9 996,67 EUR    3,33 EUR
```

## Glossary

* **Creditor** - a company that has sold some goods to the **Debtor**
* **Debtor** - a company that has purchased some goods from the **Creditor**
* **Invoice** - according to this document, the **Debtor** is to pay to the **Creditor** for the purchased goods 
  on a specific date in the future called **maturity date**.  
* **Maturity date** - the date when the **Creditor** expects the payment from the **Debtor**. If the **Invoice**
is financed, and the **Creditor** already got their money early from the **Purchaser**, this is the date when
  the **Debtor** pays to the **Purchaser** instead.
* **Purchaser** - a bank that is willing to finance the **Invoice** (i.e. provide money for this invoice early 
  to the **Creditor**), with some **interest** subtracted. The **Purchaser** receives the full amount of money 
  from the **Debtor** on **maturity date**, thus receiving their **interest**.  
* **Financing date** - the date on which the financing has occurred.  
* **Financing Term** - the duration in days between the **financing date** and the **Invoice**'s
  **maturity date**. The financing is essentially a loan given by the **Purchaser** to the **Creditor** for the 
  duration of the term, with a certain **financing rate** and responsibility of the **Debtor** to pay back the loan.
* **bps** - basis points, a unit of measure for the rates. 1 bps = 0.01% = 0.0001.
* **Annual Rate** - the interest (in bps) that the **Purchaser** expects to get for the term of 360 days.
* **Financing Rate** - the actual financing rate for a particular **Invoice**, proportional to its financing term. 
  Calculated as `financingRate = annualRate * financingTerm / 360`
* **Early payment amount** - the amount of money paid by the **Purchaser** to the **Creditor** for the particular
financed invoice on **financing date**. This amount is less than the value of the invoice.
* **Maturity payment amount** - the amount of money paid back by the **Debtor** to the **Purchaser** on 
  **maturity date**. This amount is equal to the value of the invoice.
* **Purchaser interest** - the difference between **maturity payment amount** and **early payment amount**.  
