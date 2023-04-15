package com.example.brokergateway.client;

import com.binance.api.client.constant.BinanceApiConstants;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.MarginNewOrderResponse;
import com.binance.api.client.domain.account.NewOrderResponseType;
import com.binance.api.client.domain.account.SideEffectType;
import com.binance.api.client.impl.BinanceApiService;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CustomBinanceApiService extends BinanceApiService {

    @Headers(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @POST("/sapi/v1/margin/order/oco")
    Call<MarginNewOrderResponse> newMarginOcoOrder(@Query("symbol") String symbol, @Query("side") OrderSide side, @Query("quantity") String quantity,
                                                   @Query("price") String price, @Query("stopPrice") String stopPrice, @Query("stopLimitPrice")String stopLimitPrice,
                                                   @Query("newOrderRespType") NewOrderResponseType newOrderRespType, @Query("sideEffectType") SideEffectType sideEffectType,
                                                   @Query("stopLimitTimeInForce") TimeInForce stopLimitTimeInForce, @Query("recvWindow") Long recvWindow,
                                                   @Query("timestamp") Long timestamp);
}
