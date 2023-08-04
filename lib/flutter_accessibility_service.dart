import 'dart:async';
import 'dart:developer';
import 'dart:io';

import 'package:flutter/services.dart';

class FlutterAccessibilityService {
  FlutterAccessibilityService._();

  static const MethodChannel _methodeChannel =
      MethodChannel('x-slayer/accessibility_channel');

  /// request accessibility permission
  /// it will open the accessibility settings page and return `true` once the permission granted.
  static Future<bool> requestAccessibilityPermission() async {
    try {
      return await _methodeChannel
          .invokeMethod('requestAccessibilityPermission');
    } on PlatformException catch (error) {
      log("$error");
      return Future.value(false);
    }
  }

  /// check if accessibility permession is enebaled
  static Future<bool> isAccessibilityPermissionEnabled() async {
    try {
      return await _methodeChannel
          .invokeMethod('isAccessibilityPermissionEnabled');
    } on PlatformException catch (error) {
      log("$error");
      return false;
    }
  }
 
  /*
   * 一键分享微信朋友圈
   * @param params Map<String, dynamic> , 例如{"text":"demo","waitingImageCount":1}
   *  text : 待分享文本, 请确保在调用该方法前, 文本已保存粘贴板位置顶部
      waitingImageCount:  待选择图片数量, 请确保在调用该方法前, 图片刚已保存到相册中,且在相册位置顶部,否则可能分享图片选择有问题
   */
  static void shareWechatTimeline(Map params) async {
    if (Platform.isAndroid) {
      await _methodeChannel
            .invokeMethod('shareWechatTimeline',params);
    }
    throw Exception("Accessibility API exclusively available on Android!");
  }
}
