package com.jiokye.tankbattle_multiplayer.billing;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jiokye.tankbattle_multiplayer.utility.AppManager;
import com.jiokye.tankbattle_multiplayer.utility.MessageRegister;
import com.google.common.collect.ImmutableList;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;

import java.util.List;

public class TransactionManager {

    private static TransactionManager instance;
    private BillingClient billingClient;
    private List<ProductDetails> productDetails;
    private Purchase purchase;
    private  Activity activity;

//    static final String TAG = "InAppPurchaseTag";

    public final int[] itm50 = {634, 1570, 660, 1440, 614, 1410, 614, 1500, 624, 1370, 324, 600};
    public final int[] itm100 = {634, 1570, 660, 1440, 700, 1410, 614, 1530, 574, 610, 300, 600};
    public final int[] itm200 = {634, 1570, 660, 1440, 714, 1640, 604, 1430, 654, 1370, 310, 600, 300};

    public TransactionManager() {

    }

    public static synchronized TransactionManager getInstnce() {
        if(instance == null) {
            instance = new TransactionManager();
        }
        return instance;
    }

    public void billingSetup(Activity activity) {
        billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                    Log.i(TAG, "OnBillingSetupFinish connected");
                    queryProduct(activity);
                } else {
//                    Log.i(TAG, "OnBillingSetupFinish failed");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
//                Log.i(TAG, "OnBillingSetupFinish connection lost");
            }
        });

//        handlePendingTransaction();
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    completePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//                Log.i(TAG, "onPurchasesUpdated: Purchase Canceled");
                TankToast.showTankToast(activity, "Purchase canceled");
            } else {
                TankToast.showTankToast(activity, "Purchase error");
//                Log.i(TAG, "onPurchasesUpdated: Error");
            }
        }
    };

    private void queryProduct(Activity activity) {

        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId(AppManager.getAppString(itm100))
                                            .setProductType(BillingClient.ProductType.INAPP)
                                            .build(),

                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(AppManager.getAppString(itm200))
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),

                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(AppManager.getAppString(itm50))
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build()
                                )
                        ).build();

        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {
                        productDetails = productDetailsList;
                    }
                }
        );
    }

    public void makePurchase(Activity activity, int itemID) {
        if(productDetails.isEmpty()) {
            TankToast.showTankToast(activity,"Item is not available");
            return;
        }
        this.activity = activity;
        BillingFlowParams billingFlowParams =
                BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                                ImmutableList.of(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(productDetails.get(itemID))
                                                .build()
                                )
                        )
                        .build();

        billingClient.launchBillingFlow(this.activity, billingFlowParams);
    }

    private void completePurchase(Purchase item) {

        purchase = item;
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        PurchaseSchema p = gson.fromJson(purchase.getOriginalJson(), PurchaseSchema.class);
//        Log.d("PRODUCT ID",p.productId);

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                activity.runOnUiThread(() -> {
                    int id;
                    if (p.productId.equals(AppManager.getAppString(itm50))) {
                        id = 2;
                    } else if (p.productId.equals(AppManager.getAppString(itm100))) {
                        id = 0;
                    } else if (p.productId.equals(AppManager.getAppString(itm200))) {
                        id = 1;
                    } else {
                        return;
                    }
                    MessageRegister.getInstance().registerTransactionListener(id);
                });
            });
        }

//        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
//            activity.runOnUiThread(() -> {
//                //TODO Purchase successful
////                Log.d("PURCHASE", "Purchase successful");
////                Toast.makeText(this.activity, "Purchase successful", Toast.LENGTH_SHORT).show();
//                int id;
//                if(p.productId.equals(AppManager.getAppString(itm50))){
//                    id = 2;
//                }
//                else if(p.productId.equals(AppManager.getAppString(itm100))){
//                    id = 0;
//                }
//                else if(p.productId.equals(AppManager.getAppString(itm200))){
//                    id = 1;
//                }
//                else {
//                    return;
//                }
//                MessageRegister.getInstance().registerTransactionListener(id);
//            });
//        }
    }

    //Should be called once purchase is successful

    public void consumePurchase() {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult,
                                          @NonNull String purchaseToken) {
                if (billingResult.getResponseCode() ==
                        BillingClient.BillingResponseCode.OK) {
                    TransactionManager.this.activity.runOnUiThread(() -> {
//                        binding.consumeButton.setEnabled(false);
//                        binding.statusText.setText("Purchase consumed");
                    });
                }
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

    public class PurchaseSchema {
        public String orderId;
        public String packageName;
        public String productId;
        public long purchaseTime;
        public int purchaseState;
        public String purchaseToken;
        public int quantity;
        public boolean acknowledged;

        PurchaseSchema(){};
    }

    private void handlePendingTransaction() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                completePurchase(purchase);

                            }
                        }
                    }
                }
        );
    }


}
