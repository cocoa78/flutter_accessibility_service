# flutter_accessibility_service

a plugin for interacting with Accessibility Service in Android.

微信一键分享

for more info check [Accessibility Service](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService)

### Installation and usage

Add package to your pubspec:

```yaml
dependencies:
  flutter_accessibility_service: any # or the latest version on Pub
```

Inside AndroidManifest add this to bind your accessibility service with your application

```xml
    .
    .
    <service  android:name="slayer.accessibility.service.flutter_accessibility_service.AccessibilityListener" android:label="@string/accessibility_service_title" android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data android:name="android.accessibilityservice" android:resource="@xml/accessibilityservice" />
    </service>
    .
    .
</application>

```

Create Accesiblity config file named `accessibilityservice.xml` inside `res/xml` and add the following code inside it:

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/accessibility_service_description" 
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackAllMask"
    android:notificationTimeout="100"
    android:accessibilityFlags="flagDefault|flagIncludeNotImportantViews|flagRetrieveInteractiveWindows"
    android:canRetrieveWindowContent="true"
    android:packageNames="com.tencent.mm"
>
</accessibility-service>

```

创建无障碍服务名称 和描述 文件strings.xml  在 `res/values`, and add the following code inside it:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resource>
    <string name="accessibility_service_title">App一键分享</string>
    <string name="accessibility_service_description">开启该服务后,点击一键分享即可帮您自动粘贴文字,自动选择图片,无需您手动操作.\n\n个别型号的安卓手机,退出App后,手机会主动关闭该功能导致该功能失效.出现该情况时,您可以尝试先关闭再重新开启该服务,或者重启手机.</string>
</resource>

```

### USAGE

```dart
 /// check if accessibility permission is enebaled
 final bool status = await FlutterAccessibilityService.isAccessibilityPermissionEnabled();

 /// request accessibility permission
 /// it will open the accessibility settings page and return `true` once the permission granted.
 final bool status = await FlutterAccessibilityService.requestAccessibilityPermission();

  /*
   * 一键分享微信朋友圈
   * @param params Map<String, dynamic> , 例如{"text":"demo","waitingImageCount":1}
   *  text : 待分享文本, 请确保在调用该方法前, 文本已保存粘贴板位置顶部
      waitingImageCount:  待选择图片数量, 请确保在调用该方法前, 图片刚已保存到相册中,且在相册位置顶部,否则可能分享图片选择有问题
   */
 await FlutterAccessibilityService.shareWechatTimeline();

```
