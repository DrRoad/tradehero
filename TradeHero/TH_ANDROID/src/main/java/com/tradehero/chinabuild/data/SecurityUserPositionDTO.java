package com.tradehero.chinabuild.data;

import java.util.Date;

/**
 * Created by liangyx on 6/12/15.
 */
public class SecurityUserPositionDTO {
    public Integer positionId;
    public Integer userId;
    public String userName;
    public String userPicUrl;
    public Integer quantity;
    public Double price;
    public String currencyDisplay;
    public Double roi;
    public Date openAtUtc;
}