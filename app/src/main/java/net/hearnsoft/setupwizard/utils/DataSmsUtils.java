package net.hearnsoft.setupwizard.utils;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import net.hearnsoft.setupwizard.R;

import java.util.List;

public class DataSmsUtils {

    private static final String EMERGENCY_ACCOUNT_HANDLE_ID = "E";
    private static final ComponentName PSTN_CONNECTION_SERVICE_COMPONENT =
            new ComponentName("com.android.phone",
                    "com.android.services.telephony.TelephonyConnectionService");

    private Context mContext;
    private TelephonyManager mTeleManager;
    protected TelecomManager mTelecomManager;
    protected ConnectivityManager mConnectivityManager;

    public DataSmsUtils(Context context) {
        mContext = context;
        mTeleManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public boolean isDataEnabled() {
        return mTeleManager.isDataEnabled();
    }

    public void setDataEnabled(boolean enabled) {
        mTeleManager.setDataEnabled(enabled);
    }

    public void setDefaultDatSubId(SubscriptionManager subMgr,int subId) {
        mTeleManager.createForSubscriptionId(subId);
        subMgr.setDefaultDataSubId(subId);
        setDataEnabled(true);
    }

    public CharSequence getSummary(SubscriptionInfo info) {
        final PhoneAccountHandle handle = getDefaultCallingAccountHandle();
        if ((handle != null) && (!isCallingAccountBindToSubscription(handle))) {
            // display VoIP account in summary when configured through settings within dialer
            return getLabelFromCallingAccount(handle);
        }
        if (info != null) {
            // display subscription based account
            return SubscriptionUtil.getUniqueSubscriptionDisplayName(info, mContext);
        } else {
            if (isAskEverytimeSupported()) {
                return mContext.getString(R.string.calls_and_sms_ask_every_time);
            } else {
                return "";
            }
        }
    }

    /**
     * Get default calling account
     *
     * @return current calling account {@link PhoneAccountHandle}
     */
    @SuppressLint("MissingPermission")
    public PhoneAccountHandle getDefaultCallingAccountHandle() {
        final PhoneAccountHandle currentSelectPhoneAccount =
                getTelecomManager().getUserSelectedOutgoingPhoneAccount();
        if (currentSelectPhoneAccount == null) {
            return null;
        }
        final List<PhoneAccountHandle> accountHandles =
                getTelecomManager().getCallCapablePhoneAccounts(false);
        final PhoneAccountHandle emergencyAccountHandle = new PhoneAccountHandle(
                PSTN_CONNECTION_SERVICE_COMPONENT, EMERGENCY_ACCOUNT_HANDLE_ID);
        if (currentSelectPhoneAccount.equals(emergencyAccountHandle)) {
            return null;
        }
        for (PhoneAccountHandle handle : accountHandles) {
            if (currentSelectPhoneAccount.equals(handle)) {
                return currentSelectPhoneAccount;
            }
        }
        return null;
    }

    /**
     * Get label from calling account
     *
     * @param handle to get label from {@link PhoneAccountHandle}
     * @return label of calling account
     */
    public CharSequence getLabelFromCallingAccount(PhoneAccountHandle handle) {
        CharSequence label = null;
        final PhoneAccount account = getPhoneAccount(handle);
        if (account != null) {
            label = account.getLabel();
        }
        if (label != null) {
            label = mContext.getPackageManager().getUserBadgedLabel(label, handle.getUserHandle());
        }
        return (label != null) ? label : "";
    }

    /**
     * Check if calling account bind to subscription
     *
     * @param handle {@link PhoneAccountHandle} for specific calling account
     */
    public boolean isCallingAccountBindToSubscription(PhoneAccountHandle handle) {
        final PhoneAccount account = getPhoneAccount(handle);
        if (account == null) {
            return false;
        }
        return account.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION);
    }

    private PhoneAccount getPhoneAccount(PhoneAccountHandle handle) {
        return getTelecomManager().getPhoneAccount(handle);
    }

    private TelecomManager getTelecomManager() {
        if (mTelecomManager == null) {
            mTelecomManager = mContext.getSystemService(TelecomManager.class);
        }
        return mTelecomManager;
    }

    protected boolean isAskEverytimeSupported() {
        return true;
    }

}
