package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.playID);
    }

    private int getAmount(Performance performance) {
        Play playData = getPlay(performance);
        int result = 0;

        switch (playData.type) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.audience > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_EXTRA_AMOUNT
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.audience > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.audience - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.audience;
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", playData.type));
        }
        return result;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance perf : invoice.getPerformances()) {
            int thisAmount = getAmount(perf);
            Play playData = getPlay(perf);

            volumeCredits += Math.max(perf.audience - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
            // add extra credit for every five comedy attendees
            if ("comedy".equals(playData.type)) {
                volumeCredits += perf.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
            }

            result.append(
                    String.format("  %s: %s (%s seats)%n",
                            playData.getName(),
                            frmt.format(getAmount(perf) / Constants.CENTS_PER_DOLLAR),
                            perf.getAudience())
            );
            totalAmount += getAmount(perf);
        }
        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / Constants.CENTS_PER_DOLLAR)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }
}
