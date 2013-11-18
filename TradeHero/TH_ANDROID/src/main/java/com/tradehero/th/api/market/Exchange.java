package com.tradehero.th.api.market;

import com.tradehero.th.R;

/** Created with IntelliJ IDEA. User: xavier Date: 9/16/13 Time: 11:52 AM To change this template use File | Settings | File Templates. */
public enum Exchange
{
    // United Kingdom
    LSE(R.drawable.flag_country_round_united_kingdom),

    // United States of America
    NASDAQ(R.drawable.flag_country_round_united_states),
    NYSE(R.drawable.flag_country_round_united_states),
    OTCBB(R.drawable.flag_country_round_united_states),
    AMEX(R.drawable.flag_country_round_united_states),

    // singapore
    SGX(R.drawable.flag_country_round_singapore),

    // Australia
    ASX(R.drawable.flag_country_round_australia),

    // Canada
    TSX(R.drawable.flag_country_round_canada),
    TSXV(R.drawable.flag_country_round_canada),

    // Hong Kong
    HKEX(R.drawable.flag_country_round_hong_kong),

    // france
    PAR(R.drawable.flag_country_round_france),

    // netherlands
    AMS(R.drawable.flag_country_round_netherlands),

    // Belgium
    BRU(R.drawable.flag_country_round_belgium),

    // portugal
    LIS(R.drawable.flag_country_round_portugal),

    // italy
    MLSE(R.drawable.flag_country_round_italy),

    // New Zealand
    NZX(R.drawable.flag_country_round_new_zealand),

    // china
    SHA(R.drawable.flag_country_round_china),
    SHE(R.drawable.flag_country_round_china),

    // indonesia
    JKT(R.drawable.flag_country_round_indonesia),

    // South Korea
    KDQ(R.drawable.flag_country_round_korea_south),
    KRX(R.drawable.flag_country_round_korea_south),

    // taiwan
    TPE(R.drawable.flag_country_round_taiwan),

    // thailand
    SET(R.drawable.flag_country_round_thailand),

    // philippines
    PSE(R.drawable.flag_country_round_philippines),

    // malaysia
    MYX(R.drawable.flag_country_round_malaysia),

    // TODO use proper flags
    // india
    NSE(R.drawable.th_logo),
    BSE(R.drawable.th_logo),

    // japan
    TSE(R.drawable.th_logo);

    public final int logoId;

    private Exchange(int logoId)
    {
        this.logoId = logoId;
    }
}
