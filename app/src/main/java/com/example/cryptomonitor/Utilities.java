package com.example.cryptomonitor;

import android.app.Activity;
import android.graphics.Color;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * This is a collection of static constants and methods used
 * in many calculations.
 *
 * @author Silent Hussar
 */
public class Utilities {
    //<<<< some stuff deleted >>>>

    public static final int[] PIE_CHART_COLORS = {
            Color.rgb(46, 204, 113), Color.rgb(231, 76, 60), Color.rgb(241, 196, 15), Color.rgb(52, 152, 219),
            Color.rgb(244, 67, 54), Color.rgb(233, 30, 99), Color.rgb(156, 39, 176),
            Color.rgb(33, 150, 243), Color.rgb(0, 188, 212), Color.rgb(76, 175, 80),
            Color.rgb(205, 220, 57), Color.rgb(255, 235, 59), Color.rgb(255, 152, 0),
            Color.rgb(255, 87, 34), Color.rgb(123, 31, 162), Color.rgb(81, 45, 168),
            Color.rgb(56, 142, 60), Color.rgb(230, 74, 25), Color.rgb(211, 47, 47),
            Color.rgb(194, 24, 91), Color.rgb(183, 28, 28), Color.rgb(27, 94, 32)
    };
    private static long back_pressed = 0;

    public static void onBackPressed(Activity activity) {
        if (System.currentTimeMillis() - back_pressed < 2000)
            activity.finish();
        else
            Toast.makeText(activity.getBaseContext(), activity.getString(R.string.exit_phrase), Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    public static String formatToMillion(String s) {
        String symbol = String.valueOf(s.charAt(0));
        s = s.replaceAll("[^0-9.]", "");
        double aDouble = Double.parseDouble(s);
        long killo = 1000L;
        long million = 1000000L;
        long billion = 1000000000L;
        long trillion = 1000000000000L;
        long number = Math.round(aDouble);
        if ((number >= million) && (number < billion)) {
            String fraction = calculateFraction(number, million);
            return symbol + " " + (fraction) + " M";
        } else if ((number >= billion) && (number < trillion)) {
            String fraction = calculateFraction(number, billion);
            return symbol + " " + (fraction) + " B";
        } else if ((number >= killo) && (number < million)) {
            String fraction = calculateFraction(number, killo);
            return symbol + " " + (fraction) + " K";
        }
        return Long.toString(number);
    }

    private static String calculateFraction(long number, long divisor) {
        long truncate = (number * 10L + (divisor / 2L)) / divisor;
        double fraction = (double) truncate * 0.010D;
        return cashFormatting(fraction);
    }

    public static String simpleNumberFormatting(Double d) {
        String result;
        if (d < 1) {
            BigDecimal bigDecimal = BigDecimal.valueOf(round(d, 2));
            result = bigDecimal.toPlainString();
        } else {
            //Смена Local ставиться точка пример 89.9
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat decimalFormat = new DecimalFormat(".##", symbols);
            decimalFormat.setRoundingMode(RoundingMode.CEILING);

            result = decimalFormat.format(d);
        }
        return result;
    }

    //TODO обернуть все форматы в блок static (new DecimalFormatSymbols...)
    public static Double simplePercentFormatting(Double d) {
        String result;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat decimalFormat = new DecimalFormat(".#", symbols);
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        decimalFormat.format(d);
        result = decimalFormat.format(d);
        return Double.parseDouble(result);
    }

    public static String cashFormatting(Double d) {
        String result;
        if (d < 1) {
            BigDecimal bigDecimal = BigDecimal.valueOf(round(d, 2));
            result = bigDecimal.toPlainString();
        } else {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat decimalFormat = new DecimalFormat("##,###.##", symbols);
            decimalFormat.setRoundingMode(RoundingMode.CEILING);
            decimalFormat.format(d);
            result = decimalFormat.format(d);
        }
        return result;
    }

    public static String dateFormatting(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        SimpleDateFormat sm = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        try {
            return sm.format(simpleDateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * This method returns x rounded to n digits
     */
    public static double round(double x, int n) {
        return Double.parseDouble(roundStr(x, n));
    }

    /**
     * Rounds a double number to n digits and returns a String.
     * Depending on the last unused digit, rounding is done up or down.
     *
     * @param x
     * @param n
     * @return
     */
    public static String roundStr(double x, int n) {
        String strRoundedX = roundStr(x, n, 0);
        return strRoundedX;
    }

    /**
     * This method is recursive; it rounds down a double number
     * to n digits and returns a String.
     * <p>
     * The recursive call is needed when the (n+1)th digit is 5 or greater.
     * In this case, a small delta is added to the input number and rounding
     * down of the modified number is done once again. (This is only necessary
     * when carry-ons occur, but there is no way to easily figure it out.)
     * <p>
     * Test case: rounding number 129999 to 4 digits.
     * The program first ends in 129900 which is imprecise; then it
     * adds 50 to the input because the 5-th digit is >= 5.
     * On the recursive call, rounding down is done on 129999 + 50 = 130049;
     * thus we get more precise result 130000.
     *
     * @param x
     * @param n
     * @param rcnt - recursion counter (0 or 1)
     * @return
     */
    private static String roundStr(double x, int n, int rcnt) {
        String nonZeroes = "123456789";
        String allZeroes = "000000000000000000";
// convert input to string
        String strX = Double.toString(x);
// remove difference between 'E' and 'e'
        strX = strX.toLowerCase();
// this will be the rounded output
        String strRoundedX = "";
        boolean gotDot = false;
        boolean gotNZ = false;
        int count = 0; // digit counter
// parse the input number in string representation
        for (int i = 0; i < strX.length(); i++) {
// current character
            char ch = strX.charAt(i);
            if (ch != 'e') {
                if (!gotNZ && nonZeroes.indexOf(ch) >= 0)
                    gotNZ = true; // a non-zero digit found
                if (ch == '.')
                    gotDot = true; // decimal point found
                if (ch != '-' && ch != '+' && ch != '.'  // do not count these characters
                        && !(gotDot && ch == '0' && !gotNZ) // do not count leading zeroes in numbers < 1.0
                ) {
                    count++; // count digits only
                }
                if (!gotNZ && ch == '0' && i <= 1)
                    count--; // ignore the leading zero (possibly with the minus)
                if (count > n) {
// now have parsed sufficient number of digits
                    int idxE = strX.indexOf('e');
                    if (!gotDot) {
// add trailing zeroes if necessary
                        int countOfZeroes;
                        int idxDot = strX.indexOf('.');
                        if (idxDot > 0) {
                            countOfZeroes = idxDot - i;
                        } else {
                            if (idxE >= 0)
                                countOfZeroes = idxE - i;
                            else
                                countOfZeroes = strX.length() - i;
                        }
                        strRoundedX = strRoundedX + allZeroes.substring(0, countOfZeroes);
                    }
// append the exponent part, if any
                    if (idxE >= 0)
                        strRoundedX = strRoundedX + strX.substring(idxE);
// explore the next digit, if any
                    if (ch != '.') {
                        int nextDigit = Integer.parseInt(ch + "");
// check if rounding up is needed indeed
                        if (nextDigit >= 5 && rcnt == 0) {
                            String extra = "";
                            for (int j = 0; j < strX.length(); j++) {
                                char chj = strX.charAt(j);
                                if (j < i) {
// replace non-zeroes with leading zeroes
                                    if (nonZeroes.indexOf(chj) >= 0)
                                        extra = extra + '0';
                                    else
                                        extra = extra + chj;
                                } else if (j == i)
                                    extra = extra + '5';
                                else
                                    extra = extra + chj;
                            }
                            double delta = Double.parseDouble(extra);
                            double x1 = x + delta; // this results in rounding up
//System.out.println("### recursive call on  x1 = " + x1
//+ "\n\tdelta = " + delta + "  extra = " + extra );
                            strRoundedX = roundStr(x1, n, rcnt + 1); //#### recursion ###
                        }
                    }
                    break;
                } else
                    strRoundedX = strRoundedX + ch;
            } else {
// exponent found; append it
                strRoundedX = strRoundedX + strX.substring(i);
                break;
            }
        }
// remove the trailing single dot, if any
        if (strRoundedX.endsWith("."))
            strRoundedX = strRoundedX.substring(0, strRoundedX.length() - 1);
        return strRoundedX;
    }
}
