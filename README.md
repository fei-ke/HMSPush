# HMS Push


HMS Core 是华为提供的一套服务，其中包含了推送功能，可以在华为和非华为设备上使用，

但是在非华为设备上由于缺乏系统服务配合，只能唤醒目标应用让其自己弹出通知    

同时大部分应用在非华为设备上不会主动启用 HMS 推送服务

该模块借助 [LSPosed](https://github.com/LSPosed/LSPosed) 为 HMS Core 提供发送系统通知的能力，
同时支持将应用运行环境伪装成华为设备，以此来实现无后台系统级别的推送通道。

> **Warning**
> 对应用进行设备伪装会导致应用环境异常，从而导致封号等后果，请自行承担使用风险！

### 安装步骤：
- 从应用市场下载并安装 `HMS Core`，比如 [腾讯应用宝](https://sj.qq.com/appdetail/com.huawei.hwid)、[酷安](https://www.coolapk.com/apk/com.huawei.hwid)、[APKMirror](https://www.apkmirror.com/apk/huawei-internet-services/huawei-mobile-services)

- 下载最新版本 HMS Push 安装，在 LSPosed 中启用 HMSPush 模块，并勾选 「系统框架」、「HMS Core 」作用域，然后重启设备，[下载地址](https://github.com/fei-ke/HMSPush/releases/latest)

- LSPosed 里 HMSPush 模块里勾选你需要支持推送的目标应用（这一步目的是将应用环境伪装成华为设备，如果你使用了其他方式伪装设备，可以不进行这一步），然后重启一到两次目标应用使其注册上推送通道

- 杀掉应用测试推送是否生效（可以使用QQ测试）
　　
### 注意：
- 并不是所有应用都支持 HMS 推送，目前测试已支持大部分应用，比如 QQ、抖音、知乎、酷安等，闲鱼、淘宝、饿了么等 v0.0.13 起已支持

- **微信不支持，因为微信没有接入 HMS 服务**

- 请保证 HMS Core 在后台运行，不要禁用其自启权限和访问目标推送应用的权限

- 如遇到点击通知未能进入目标应用，可尝试将 HMS Core 转为系统应用，不知道如何操作可直接刷入此 [Magisk 模块](https://github.com/fei-ke/HMSPush/releases/download/v0.0.5/HMSCore-v0.3.zip)

- 反馈问题请带上 LSP 日志，到 Github 提 [Issue](https://github.com/fei-ke/HMSPush/issues) 或者加入 [Telegram 群组](https://t.me/HMSPush)，或者发送至我的邮箱 [Email](mailto:hmspush@yufz.one)

### 鸣谢
包括但不限于：
- [LSPosed](https://github.com/LSPosed/LSPosed) XPosed 框架
- [XposedBridge](https://github.com/rovo89/XposedBridge) Xposed framework APIs
- [LSPatch](https://github.com/LSPosed/LSPatch) 免 Root Xposed 框架
- [AndroidNotifyIconAdapt](https://github.com/fankes/AndroidNotifyIconAdapt) 图标库

### 反馈
[Github Issues](https://github.com/fei-ke/HMSPush/issues)、[Telegram Group](https://t.me/HMSPush)、[Email](mailto:hmspush@yufz.one)

### License
[GNU General Public License v3 (GPL-3)](http://www.gnu.org/copyleft/gpl.html).
