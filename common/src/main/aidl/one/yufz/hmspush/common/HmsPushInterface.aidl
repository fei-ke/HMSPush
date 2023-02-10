// HmsPushInterface.aidl
package one.yufz.hmspush.common;

import one.yufz.hmspush.common.model.models;

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
}