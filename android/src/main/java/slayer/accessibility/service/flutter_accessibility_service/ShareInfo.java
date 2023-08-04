package slayer.accessibility.service.flutter_accessibility_service;

import android.text.TextUtils; 

/**
 * Created by StoneHui on 2018/10/30.
 * 
 * 分享信息。
 */
public class ShareInfo {
  public static String text = "";
  /**
   * 待选择图片数量。
   */
  public static int waitingImageCount = 0;

  /**
   * 是否有待分享文本
   */
  public static boolean hasText() {
    return !TextUtils.isEmpty(text);
  }
}