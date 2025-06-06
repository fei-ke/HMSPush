// HmsPushInterface.aidl
package one.yufz.hmspush.common;

import one.yufz.hmspush.common.model.models;
import one.yufz.hmspush.common.model.ModuleVersionModel;
import one.yufz.hmspush.common.model.PushSignModel;
import one.yufz.hmspush.common.model.PushHistoryModel;
import one.yufz.hmspush.common.model.PrefsModel;
import one.yufz.hmspush.common.model.IconModel;

interface HmsPushInterface {
    ModuleVersionModel getModuleVersion();
    List<PushSignModel> getPushSignList();
    void unregisterPush(String packageName);
    List<PushHistoryModel> getPushHistoryList();
    PrefsModel getPreference();
    void updatePreference(in PrefsModel model);

    List<IconModel> getAllIcon();
    void saveIcon(in IconModel iconModel);
    void deleteIcon(in String[] packageNames);

    boolean killHmsCore();
    void clearHmsNotificationChannels(String packageName);
}
