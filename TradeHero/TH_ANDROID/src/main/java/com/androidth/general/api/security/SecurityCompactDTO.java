package com.androidth.general.api.security;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.androidth.general.common.persistence.DTO;
import com.androidth.general.R;
import com.androidth.general.api.market.Exchange;
import com.androidth.general.api.security.compact.BondCompactDTO;
import com.androidth.general.api.security.compact.CoveredWarrantDTO;
import com.androidth.general.api.security.compact.DepositoryReceiptDTO;
import com.androidth.general.api.security.compact.EquityCompactDTO;
import com.androidth.general.api.security.compact.FundCompactDTO;
import com.androidth.general.api.security.compact.FxSecurityCompactDTO;
import com.androidth.general.api.security.compact.IndexSecurityCompactDTO;
import com.androidth.general.api.security.compact.LockedSecurityCompactDTO;
import com.androidth.general.api.security.compact.PreferenceShareDTO;
import com.androidth.general.api.security.compact.PreferredSecurityDTO;
import com.androidth.general.api.security.compact.StapledSecurityDTO;
import com.androidth.general.api.security.compact.TradableRightsIssueDTO;
import com.androidth.general.api.security.compact.UnitCompactDTO;
import com.androidth.general.api.security.compact.UnitTrustSecurityCompactDTO;
import com.androidth.general.api.security.compact.WarrantDTO;
import java.util.Date;
import timber.log.Timber;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = SecurityCompactDTO.class,
        property = "securityType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LockedSecurityCompactDTO.class, name = LockedSecurityCompactDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = EquityCompactDTO.class, name = EquityCompactDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = FundCompactDTO.class, name = FundCompactDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = WarrantDTO.class, name = WarrantDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = BondCompactDTO.class, name = BondCompactDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = UnitCompactDTO.class, name = UnitCompactDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = TradableRightsIssueDTO.class, name = TradableRightsIssueDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = PreferenceShareDTO.class, name = PreferenceShareDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = DepositoryReceiptDTO.class, name = DepositoryReceiptDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = CoveredWarrantDTO.class, name = CoveredWarrantDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = PreferredSecurityDTO.class, name = PreferredSecurityDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = StapledSecurityDTO.class, name = StapledSecurityDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = IndexSecurityCompactDTO.class, name = IndexSecurityCompactDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = UnitTrustSecurityCompactDTO.class, name = UnitTrustSecurityCompactDTO.DTO_DESERIALISING_TYPE),
        @JsonSubTypes.Type(value = FxSecurityCompactDTO.class, name = FxSecurityCompactDTO.DTO_DESERIALISING_TYPE),
})
public class SecurityCompactDTO implements DTO, Parcelable
{
    public static final String EXCHANGE_SYMBOL_FORMAT = "%s:%s";

    private final long createdAtNanoTime = System.nanoTime();
    public Integer id;
    public String symbol;
    public String name;
    public String exchange;
    public String yahooSymbol;
    public String currencyDisplay;
    public String currencyISO;
    @Nullable public Double marketCap;
    @Nullable public Double lastPrice;
    public String imageBlobUrl;

    private Date lastPriceDateEST;
    //// EDT/EST converted to UTC
    @Nullable public Date lastPriceDateAndTimeUtc;

    @Nullable public Double toUSDRate;
    public Date toUSDRateDate;

    public boolean active;

    public Double askPrice;
    public Double bidPrice;
    public Double volume;
    public Double averageDailyVolume;
    public Double previousClose;
    public Double open;
    public Double high;
    public Double low;
    public Double pe;
    public Double eps;

    @Nullable public Boolean marketOpen;

    public Integer pc50DMA;
    public Integer pc200DMA;
    // OK above
    public String exchangeTimezoneMsftName;
    // Example "09:30:00"
    public String exchangeOpeningTimeLocal;
    // Example "16:00:00"
    public String exchangeClosingTimeLocal;
    //
    public String secTypeDesc;
    public Double risePercent;

    protected String timeTillNextExchangeOpen;

    public String marker;
    public Boolean isCFD;
    public Double minShort;
    public Double maxShort;
    public Double minLong;
    public Double maxLong;
    public Integer sortorderInExchange;
    public Integer sortorderOverall;
    public Integer UnderlyingSecurityId;


    //<editor-fold desc="Constructors">
    public SecurityCompactDTO()
    {
        super();
    }

    public SecurityCompactDTO(@NonNull SecurityCompactDTO other)
    {
        super();
        this.marker = other.marker;
        this.isCFD = other.isCFD;
        this.minShort = other.minShort;
        this.maxShort = other.maxShort;
        this.minShort = other.minShort;
        this.minLong = other.minLong;
        this.maxLong = other.maxLong;
        this.sortorderInExchange = other.sortorderInExchange;
        this.sortorderOverall = other.sortorderOverall;
        this.UnderlyingSecurityId = other.UnderlyingSecurityId;

        this.id = other.id;
        this.symbol = other.symbol;
        this.name = other.name;
        this.exchange = other.exchange;
        this.yahooSymbol = other.yahooSymbol;
        this.currencyDisplay = other.currencyDisplay;
        this.currencyISO = other.currencyISO;
        this.marketCap = other.marketCap;
        this.lastPrice = other.lastPrice;
        this.imageBlobUrl = other.imageBlobUrl;
        this.lastPriceDateEST = other.lastPriceDateEST;
        this.lastPriceDateAndTimeUtc = other.lastPriceDateAndTimeUtc;
        this.toUSDRate = other.toUSDRate;
        this.toUSDRateDate = other.toUSDRateDate;
        this.active = other.active;
        this.askPrice = other.askPrice;
        this.bidPrice = other.bidPrice;
        this.volume = other.volume;
        this.averageDailyVolume = other.averageDailyVolume;
        this.previousClose = other.previousClose;
        this.open = other.open;
        this.high = other.high;
        this.low = other.low;
        this.pe = other.pe;
        this.eps = other.eps;
        this.marketOpen = other.marketOpen;
        this.pc50DMA = other.pc50DMA;
        this.pc200DMA = other.pc200DMA;
        this.exchangeTimezoneMsftName = other.exchangeTimezoneMsftName;
        this.exchangeOpeningTimeLocal = other.exchangeOpeningTimeLocal;
        this.exchangeClosingTimeLocal = other.exchangeClosingTimeLocal;
        this.secTypeDesc = other.secTypeDesc;
        this.risePercent = other.risePercent;
        this.timeTillNextExchangeOpen = other.timeTillNextExchangeOpen;
    }
    //</editor-fold>

    @Nullable public Integer getSecurityTypeStringResourceId()
    {
        return null;
    }
    @Nullable public Integer getResourceId()
    {
        return id;
    }


    @NonNull public String getExchangeSymbol()
    {
        return String.format(EXCHANGE_SYMBOL_FORMAT, exchange, symbol);
    }

    @DrawableRes public int getExchangeLogoId()
    {
        return getExchangeLogoId(R.drawable.default_image);
    }

    @DrawableRes public int getExchangeLogoId(int defaultResId)
    {
        try
        {
            return Exchange.valueOf(exchange).logoId;
        } catch (IllegalArgumentException ex)
        {
            return defaultResId;
        } catch (NullPointerException ex) // there isn't any client Exchange resource with the given value exchange
        {
            Timber.e("Missing exchange resource for %s", exchange);
            return defaultResId;
        }
    }

    @NonNull public SecurityIntegerId getSecurityIntegerId()
    {
        return new SecurityIntegerId(id);
    }

    @NonNull public SecurityId getSecurityId()
    {
        return new SecurityId(exchange, symbol);
    }

    @Nullable public TillExchangeOpenDuration getTillExchangeOpen()
    {
        if (TextUtils.isEmpty(timeTillNextExchangeOpen))
        {
            return null;
        }
        String timeTillNextExchangeOpen = this.timeTillNextExchangeOpen;
        int lastIndex = timeTillNextExchangeOpen.lastIndexOf(":");
        int seconds = (int) Float.parseFloat(timeTillNextExchangeOpen.substring(lastIndex + 1));
        timeTillNextExchangeOpen = timeTillNextExchangeOpen.substring(0, lastIndex);
        lastIndex = timeTillNextExchangeOpen.lastIndexOf(":");
        int minutes = Integer.parseInt(timeTillNextExchangeOpen.substring(lastIndex + 1));
        timeTillNextExchangeOpen = timeTillNextExchangeOpen.substring(0, lastIndex);
        lastIndex = timeTillNextExchangeOpen.lastIndexOf(".");
        int days;
        if (lastIndex != -1)
        {
            days = Integer.parseInt(timeTillNextExchangeOpen.substring(0, lastIndex));
        }
        else
        {
            days = 0;
        }
        int hours = Integer.parseInt(timeTillNextExchangeOpen.substring(lastIndex + 1));

        return new TillExchangeOpenDuration(createdAtNanoTime, days, hours, minutes, seconds);
    }

    @Override public String toString()
    {
        return "SecurityCompactDTO{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", exchange='" + exchange + '\'' +
                ", yahooSymbol='" + yahooSymbol + '\'' +
                ", currencyDisplay='" + currencyDisplay + '\'' +
                ", currencyISO='" + currencyISO + '\'' +
                ", marketCap=" + marketCap +
                ", lastPrice=" + lastPrice +
                ", imageBlobUrl='" + imageBlobUrl + '\'' +
                ", lastPriceDateEST=" + lastPriceDateEST +
                ", lastPriceDateAndTimeUtc=" + lastPriceDateAndTimeUtc +
                ", toUSDRate=" + toUSDRate +
                ", toUSDRateDate=" + toUSDRateDate +
                ", active=" + active +
                ", askPrice=" + askPrice +
                ", bidPrice=" + bidPrice +
                ", volume=" + volume +
                ", averageDailyVolume=" + averageDailyVolume +
                ", previousClose=" + previousClose +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", pe=" + pe +
                ", eps=" + eps +
                ", marketOpen=" + marketOpen +
                ", pc50DMA=" + pc50DMA +
                ", pc200DMA=" + pc200DMA +
                ", exchangeTimezoneMsftName='" + exchangeTimezoneMsftName + '\'' +
                ", exchangeOpeningTimeLocal='" + exchangeOpeningTimeLocal + '\'' +
                ", exchangeClosingTimeLocal='" + exchangeClosingTimeLocal + '\'' +
                ", secTypeDesc='" + secTypeDesc + '\'' +
                '}';
    }

    public Double getVolume() {
        return volume;
    }

    public Double getRisePercent() {
        return risePercent;
    }


    /**
     * Parcelable implementations
     * @param other
     */
    private SecurityCompactDTO(Parcel other){
        this.marker = other.readString();
        this.isCFD = other.readByte()==1? true: false;
        this.minShort = other.readDouble();
        this.maxShort = other.readDouble();
        this.minShort = other.readDouble();
        this.minLong = other.readDouble();
        this.maxLong = other.readDouble();
        this.sortorderInExchange = other.readInt();
        this.sortorderOverall = other.readInt();
        this.UnderlyingSecurityId = other.readInt();


        this.id = other.readInt();
        this.symbol = other.readString();
        this.name = other.readString();
        this.exchange = other.readString();
        this.yahooSymbol = other.readString();
        this.currencyDisplay = other.readString();
        this.currencyISO = other.readString();
        this.marketCap = other.readDouble();
        this.lastPrice = other.readDouble();
        this.imageBlobUrl = other.readString();
        this.lastPriceDateEST = new Date(other.readLong());
        this.lastPriceDateAndTimeUtc = new Date(other.readLong());
        this.toUSDRate = other.readDouble();
        this.toUSDRateDate = new Date(other.readLong());
        this.active = other.readByte()==1? true: false;
        this.askPrice = other.readDouble();
        this.bidPrice = other.readDouble();
        this.volume = other.readDouble();
        this.averageDailyVolume = other.readDouble();
        this.previousClose = other.readDouble();
        this.open = other.readDouble();
        this.high = other.readDouble();
        this.low = other.readDouble();
        this.pe = other.readDouble();
        this.eps = other.readDouble();
        this.marketOpen = other.readByte()==1? true: false;
        this.pc50DMA = other.readInt();
        this.pc200DMA = other.readInt();
        this.exchangeTimezoneMsftName = other.readString();
        this.exchangeOpeningTimeLocal = other.readString();
        this.exchangeClosingTimeLocal = other.readString();
        this.secTypeDesc = other.readString();
        this.risePercent = other.readDouble();
        this.timeTillNextExchangeOpen = other.readString();
    }

    public static final Parcelable.Creator<SecurityCompactDTO> CREATOR = new Parcelable.Creator<SecurityCompactDTO>(){
        @Override
        public SecurityCompactDTO createFromParcel(Parcel source) {
            return new SecurityCompactDTO(source);
        }

        @Override
        public SecurityCompactDTO[] newArray(int size) {
            return new SecurityCompactDTO[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try{
            dest.writeString(this.marker);
            dest.writeByte((byte) (this.isCFD ? 1 : 0));
            dest.writeDouble(this.minShort);
            dest.writeDouble(this.maxShort);
            dest.writeDouble(this.minLong);
            dest.writeDouble(this.maxLong);
            dest.writeInt(this.sortorderInExchange);
            dest.writeInt(this.sortorderOverall);
            dest.writeInt(this.UnderlyingSecurityId);

            dest.writeInt(this.id);
            dest.writeString(this.symbol);
            dest.writeString(this.name);
            dest.writeString(this.exchange);
            dest.writeString(this.yahooSymbol);
            dest.writeString(this.currencyDisplay);
            dest.writeString(this.currencyISO);
            dest.writeDouble(this.marketCap);
            dest.writeDouble(this.lastPrice);
            dest.writeString(this.imageBlobUrl);
            dest.writeLong(this.lastPriceDateEST.getTime());
            dest.writeLong(this.lastPriceDateAndTimeUtc.getTime());
            dest.writeDouble(this.toUSDRate);
            dest.writeLong(this.toUSDRateDate.getTime());
            dest.writeByte((byte) (this.active ? 1 : 0));
            dest.writeDouble(this.askPrice);
            dest.writeDouble(this.bidPrice);
            dest.writeDouble(this.volume);
            dest.writeDouble(this.averageDailyVolume);
            dest.writeDouble(this.previousClose);
            dest.writeDouble(this.open);
            dest.writeDouble(this.high);
            dest.writeDouble(this.low);
            dest.writeDouble(this.pe);
            dest.writeDouble(this.eps);
            dest.writeByte((byte) (this.marketOpen ? 1 : 0));
            dest.writeInt(this.pc50DMA);
            dest.writeInt(this.pc200DMA);
            dest.writeString(this.exchangeTimezoneMsftName);
            dest.writeString(this.exchangeOpeningTimeLocal);
            dest.writeString(this.exchangeClosingTimeLocal);
            dest.writeString(this.secTypeDesc);
            dest.writeDouble(this.risePercent);
            dest.writeString(this.timeTillNextExchangeOpen);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
