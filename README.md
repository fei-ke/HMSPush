# HMS Push

HMS Core 是华为提供的一套服务，其中包含了推送功能，可以在华为和非华为设备上使用，

但是在非华为设备上由于缺乏系统服务配合，只能唤醒目标应用让其自己弹出通知    

同时大部分应用在非华为设备上不会主动启用 HMS 推送服务

该模块借助 [Magisk](https://github.com/topjohnwu/Magisk) 和 [LSPosed](https://github.com/LSPosed/LSPosed) 为 HMS Core 提供发送系统通知的能力，
同时支持将应用运行环境伪装成华为设备，以此来实现无后台系统级别的推送通道。

### 安装步骤：
- 下载 [HMS Push](https://github.com/fei-ke/HMSPush/releases) 并安装，在 LSPosed 中启用 HMSPush 模块，并勾选 「系统框架」作用域

- 下载 [HMS Core](https://github.com/fei-ke/HMSPush/releases/download/v0.0.6/HMSCore-v0.3.zip) 在 Magisk 中刷入然后重启设备

- LSPosed 里 HMSPush 模块里继续勾选 「HMS Core」作用域（这一步目的是实现不唤醒应用弹出通知），然后强行停止一次「HMS Core」让作用域生效

- LSPosed 里 HMSPush 模块里勾选你需要支持推送的目标应用（这一步目的是将应用环境伪装成华为设备），然后重启一到两次目标应用使其注册上推送通道

- 杀掉应用测试推送是否生效（可以使用QQ测试）
　　
### 注意：
- 并不是所有应用都支持 HMS 推送，目前测试支持的有 QQ、抖音、知乎、酷安、企业微信
- 微信、咸鱼、支付宝测试不支持

### License
[GNU General Public License v3 (GPL-3)](http://www.gnu.org/copyleft/gpl.html).

