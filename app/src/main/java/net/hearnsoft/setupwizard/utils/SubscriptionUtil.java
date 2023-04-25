package net.hearnsoft.setupwizard.utils;

import static android.telephony.SubscriptionManager.INVALID_SIM_SLOT_INDEX;
import static android.telephony.UiccSlotInfo.CARD_STATE_INFO_PRESENT;

import static com.android.internal.util.CollectionUtils.emptyIfNull;

import android.content.Context;
import android.os.ParcelUuid;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.util.Supplier;

import net.hearnsoft.setupwizard.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubscriptionUtil {

    private static final String PROFILE_GENERIC_DISPLAY_NAME = "CARD";

    private static List<SubscriptionInfo> sAvailableResultsForTesting;
    private static List<SubscriptionInfo> sActiveResultsForTesting;

    /**
     * Return a mapping of active subscription ids to display names. Each display name is
     * guaranteed to be unique in the following manner:
     * 1) If the original display name is not unique, the last four digits of the phone number
     *    will be appended.
     * 2) If the phone number is not visible or the last four digits are shared with another
     *    subscription, the subscription id will be appended to the original display name.
     * More details can be found at go/unique-sub-display-names.
     *
     * @return map of active subscription ids to display names.
     */
    public static Map<Integer, CharSequence> getUniqueSubscriptionDisplayNames(Context context) {
        class DisplayInfo {
            public SubscriptionInfo subscriptionInfo;
            public CharSequence originalName;
            public CharSequence uniqueName;
        }

        // Map of SubscriptionId to DisplayName
        final Supplier<Stream<DisplayInfo>> originalInfos =
                () -> getAvailableSubscriptions(context)
                        .stream()
                        .filter(i -> {
                            // Filter out null values.
                            return (i != null && i.getDisplayName() != null);
                        })
                        .map(i -> {
                            DisplayInfo info = new DisplayInfo();
                            info.subscriptionInfo = i;
                            String displayName = i.getDisplayName().toString();
                            info.originalName = TextUtils.equals(displayName, PROFILE_GENERIC_DISPLAY_NAME)
                                    ? context.getResources().getString(R.string.sim_card)
                                    : displayName.trim();
                            return info;
                        });

        // TODO(goldmanj) consider using a map of DisplayName to SubscriptionInfos.
        // A Unique set of display names
        Set<CharSequence> uniqueNames = new HashSet<>();
        // Return the set of duplicate names
        final Set<CharSequence> duplicateOriginalNames = originalInfos.get()
                .filter(info -> !uniqueNames.add(info.originalName))
                .map(info -> info.originalName)
                .collect(Collectors.toSet());

        // If a display name is duplicate, append the final 4 digits of the phone number.
        // Creates a mapping of Subscription id to original display name + phone number display name
        final Supplier<Stream<DisplayInfo>> uniqueInfos = () -> originalInfos.get().map(info -> {
            if (duplicateOriginalNames.contains(info.originalName)) {
                // This may return null, if the user cannot view the phone number itself.
                final String phoneNumber = DeviceInfoUtils.getBidiFormattedPhoneNumber(context,
                        info.subscriptionInfo);
                String lastFourDigits = "";
                if (phoneNumber != null) {
                    lastFourDigits = (phoneNumber.length() > 4)
                            ? phoneNumber.substring(phoneNumber.length() - 4) : phoneNumber;
                }

                if (TextUtils.isEmpty(lastFourDigits)) {
                    info.uniqueName = info.originalName;
                } else {
                    info.uniqueName = info.originalName + " " + lastFourDigits;
                }

            } else {
                info.uniqueName = info.originalName;
            }
            return info;
        });

        // Check uniqueness a second time.
        // We might not have had permission to view the phone numbers.
        // There might also be multiple phone numbers whose last 4 digits the same.
        uniqueNames.clear();
        final Set<CharSequence> duplicatePhoneNames = uniqueInfos.get()
                .filter(info -> !uniqueNames.add(info.uniqueName))
                .map(info -> info.uniqueName)
                .collect(Collectors.toSet());

        return uniqueInfos.get().map(info -> {
            if (duplicatePhoneNames.contains(info.uniqueName)) {
                info.uniqueName = info.originalName + " "
                        + info.subscriptionInfo.getSubscriptionId();
            }
            return info;
        }).collect(Collectors.toMap(
                info -> info.subscriptionInfo.getSubscriptionId(),
                info -> info.uniqueName));
    }

    /**
     * Get all of the subscriptions which is available to display to the user.
     *
     * @param context {@code Context}
     * @return list of {@code SubscriptionInfo}
     */
    public static List<SubscriptionInfo> getAvailableSubscriptions(Context context) {
        if (sAvailableResultsForTesting != null) {
            return sAvailableResultsForTesting;
        }
        return new ArrayList<>(emptyIfNull(getSelectableSubscriptionInfoList(context)));
    }

    /**
     * Return a list of subscriptions that are available and visible to the user.
     *
     * @return list of user selectable subscriptions.
     */
    public static List<SubscriptionInfo> getSelectableSubscriptionInfoList(Context context) {
        SubscriptionManager subManager = context.getSystemService(SubscriptionManager.class);
        List<SubscriptionInfo> availableList = subManager.getAvailableSubscriptionInfoList();
        if (availableList == null) {
            return null;
        } else {
            // Multiple subscriptions in a group should only have one representative.
            // It should be the current active primary subscription if any, or any
            // primary subscription.
            List<SubscriptionInfo> selectableList = new ArrayList<>();
            Map<ParcelUuid, SubscriptionInfo> groupMap = new HashMap<>();

            for (SubscriptionInfo info : availableList) {
                // Opportunistic subscriptions are considered invisible
                // to users so they should never be returned.
                if (!isSubscriptionVisible(subManager, context, info)) continue;

                ParcelUuid groupUuid = info.getGroupUuid();
                if (groupUuid == null) {
                    // Doesn't belong to any group. Add in the list.
                    selectableList.add(info);
                } else if (!groupMap.containsKey(groupUuid)
                        || (groupMap.get(groupUuid).getSimSlotIndex() == INVALID_SIM_SLOT_INDEX
                        && info.getSimSlotIndex() != INVALID_SIM_SLOT_INDEX)) {
                    // If it belongs to a group that has never been recorded or it's the current
                    // active subscription, add it in the list.
                    selectableList.remove(groupMap.get(groupUuid));
                    selectableList.add(info);
                    groupMap.put(groupUuid, info);
                }

            }
            return selectableList;
        }
    }

    /**
     * Whether a subscription is visible to API caller. If it's a bundled opportunistic
     * subscription, it should be hidden anywhere in Settings, dialer, status bar etc.
     * Exception is if caller owns carrier privilege, in which case they will
     * want to see their own hidden subscriptions.
     *
     * @param info the subscriptionInfo to check against.
     * @return true if this subscription should be visible to the API caller.
     */
    public static boolean isSubscriptionVisible(
            SubscriptionManager subscriptionManager, Context context, SubscriptionInfo info) {
        if (info == null) return false;
        // If subscription is NOT grouped opportunistic subscription, it's visible.
        if (info.getGroupUuid() == null || !info.isOpportunistic()) return true;

        // If the caller is the carrier app and owns the subscription, it should be visible
        // to the caller.
        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class)
                .createForSubscriptionId(info.getSubscriptionId());
        boolean hasCarrierPrivilegePermission = telephonyManager.hasCarrierPrivileges()
                || subscriptionManager.canManageSubscription(info);
        return hasCarrierPrivilegePermission;
    }

    /**
     * Return the display name for a subscription id, which is guaranteed to be unique.
     * The logic to create this name has the following order of operations:
     * 1) If the original display name is not unique, the last four digits of the phone number
     *    will be appended.
     * 2) If the phone number is not visible or the last four digits are shared with another
     *    subscription, the subscription id will be appended to the original display name.
     * More details can be found at go/unique-sub-display-names.
     *
     * @return map of active subscription ids to display names.
     */
    public static CharSequence getUniqueSubscriptionDisplayName(
            Integer subscriptionId, Context context) {
        final Map<Integer, CharSequence> displayNames = getUniqueSubscriptionDisplayNames(context);
        return displayNames.getOrDefault(subscriptionId, "");
    }

    /**
     * Return the display name for a subscription, which is guaranteed to be unique.
     * The logic to create this name has the following order of operations:
     * 1) If the original display name is not unique, the last four digits of the phone number
     *    will be appended.
     * 2) If the phone number is not visible or the last four digits are shared with another
     *    subscription, the subscription id will be appended to the original display name.
     * More details can be found at go/unique-sub-display-names.
     *
     * @return map of active subscription ids to display names.
     */
    public static CharSequence getUniqueSubscriptionDisplayName(
            SubscriptionInfo info, Context context) {
        if (info == null) {
            return "";
        }
        return getUniqueSubscriptionDisplayName(info.getSubscriptionId(), context);
    }

    public static String getDisplayName(SubscriptionInfo info) {
        final CharSequence name = info.getDisplayName();
        if (name != null) {
            return name.toString();
        }
        return "";
    }

}
