package slayer.accessibility.service.flutter_accessibility_service;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

import java.util.concurrent.*;
import java.util.regex.Pattern;
import android.widget.EditText;
import java.util.LinkedList;


public class AccessibilityListener extends AccessibilityService {    
    public static String ACCESSIBILITY_INTENT = "accessibility_event";

    private final String LAUNCHER_UI = "com.tencent.mm.ui.LauncherUI";
    private final String TIMELINE_UI = "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI";
    private final String UPLOAD_UI = "com.tencent.mm.plugin.sns.ui.SnsUploadUI";
    private final String ALBUM_PREVIEW_UI = "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI";
    private final String[] DISCOVER_TEXT_LIST = {"发现", "Discover"};

    private final String[] TIMELINE_TEXT_LIST = {"朋友圈", "Moments"};

    private final String[] SHARE_TEXT_LIST = {"拍照分享", ""};

    private final String[] SELECT_FROM_ALBUM_TEXT_LIST = {
    "从相册选择", "Select Photos or Videos from Album", "Choose from Album"  
    };

    private final String[] POST_TEXT_LIST = {"发表", "Post"};

    // GridView或RecyclerView
    private final Pattern gvOrRcvRegex = Pattern.compile(".*\\.(GridView|RecyclerView)$");

    // EditText
    private final Pattern etRegex = Pattern.compile(EditText.class.getName() + "$");

    // View或CheckBox 
    private final Pattern vOrCbRegex = Pattern.compile(".*\\.(View|CheckBox)$");

    // Button  
    private final Pattern btnRegex = Pattern.compile(".*\\.(AppCompatButton|Button)$");

    // 当前步骤
    public static Step step = Step.Launcher;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (step == Step.AllDone) {
            return;
        }

        final int eventType = accessibilityEvent.getEventType();
        if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            final String className = accessibilityEvent.getClassName().toString();
            switch(className) {
                case LAUNCHER_UI:
                    processLauncherUI();
                break;
                
                case TIMELINE_UI:
                   prepareGoIntoAlbum();                    
                break;
                
                case ALBUM_PREVIEW_UI: 
                selectImage();
                break;
                
                case UPLOAD_UI:
                processingUploadUI();
                break;
                
                default:
                break;
            }

        } else if(eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            switch(step) {
                case Discover:
                goIntoTimeline();
                break;
                
                case PrepareAlbum :
                goIntoAlbum();
                break;
                
                default:
                break;
            }
            
        }
    }

    // 定时器, 传参为定时后 执行的方法
    private void  timeScheduler(Runnable runnable) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(); 
        executor.schedule(runnable, 200, TimeUnit.MILLISECONDS);
    }

     // 处理启动页
    private void processLauncherUI() {
        step = Step.Launcher;
        goIntoDiscover();
    }

    // 进入发现页
    private void goIntoDiscover() {
        if (step != Step.Launcher) {
            return;
        }
        
        timeScheduler(() -> {
            step = Step.Discover;
            AccessibilityNodeInfo rootNode = getRootNodeInfo();
            if (rootNode != null) {
                clickNodeByText(rootNode, DISCOVER_TEXT_LIST, 2); 
            }
        });
    }

    // 进入朋友圈
    private void goIntoTimeline() {
        if (step != Step.Discover) {
            return;
        }
        timeScheduler(() -> {
            step = Step.Timeline;
            AccessibilityNodeInfo rootNode = getRootNodeInfo();
            if (rootNode != null) {
                clickNodeByText(rootNode, TIMELINE_TEXT_LIST, 8); 
            }
        });
    }

    // 准备进入相册
    private void prepareGoIntoAlbum() {
        if (step != Step.Timeline) {
            return;
        }
        timeScheduler(() -> {
        // 任务
            step = Step.PrepareAlbum;
            AccessibilityNodeInfo rootNode = getRootNodeInfo();
            if (rootNode != null) {
                clickNodeByText(rootNode, SHARE_TEXT_LIST, 0 ); 
            }
        }); 
    }

    // 进入相册
    private void goIntoAlbum() {
        if (step != Step.PrepareAlbum) {
            return;
        }
        timeScheduler(() -> {
            step = Step.AlbumPreview;
            AccessibilityNodeInfo rootNode = getRootNodeInfo();
            if (rootNode != null) {
                clickNodeByText(rootNode, SELECT_FROM_ALBUM_TEXT_LIST, 3 ); 
            }
        }); 
    }

    // 选择图片
    private void selectImage() {
        if (step != Step.AlbumPreview) {
            return;
        }
        timeScheduler(() -> {
             AccessibilityNodeInfo rootNodeInfo = getRootNodeInfo();
            if (rootNodeInfo == null) {
                return;
            }

            AccessibilityNodeInfo targetView = getChildByRegex(rootNodeInfo,gvOrRcvRegex);
            if (targetView == null) {
                return;
            }
            // 选图
            int maxIndex = ShareInfo.waitingImageCount;
            for (int i = 0; i <= maxIndex; i++) {
                AccessibilityNodeInfo vOrCbNode = targetView.getChild(i);
                //显示
                clickNodeByClassName(vOrCbNode,vOrCbRegex);
            }
            // 选图结束
            selectImageFinished();
        }); 
    }

    // 选择图片完成
    private void selectImageFinished() {
        ShareInfo.waitingImageCount = 0;
        step = Step.Upload;
        AccessibilityNodeInfo rootNode = getRootNodeInfo();
        if (rootNode != null) {
            clickNodeByClassName(rootNode, btnRegex); 
        } 
    }

    // 处理图文分享界面
    private void processingUploadUI() {
        if (step != Step.Upload) {
            return;
        }

        AccessibilityNodeInfo rootNodeInfo = getRootNodeInfo();
        if (rootNodeInfo == null) {
            step = Step.AllDone;
            return;
        }

        if (ShareInfo.hasText()){
            ShareInfo.text = "";
            
            // 粘贴待分享文案
            AccessibilityNodeInfo etNode = getChildByRegex(rootNodeInfo,etRegex);
            if (etNode != null) {
                etNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                etNode.performAction(AccessibilityNodeInfo.ACTION_PASTE); 
            }
        }

        step = Step.AllDone;
    }

    /**
     * 点击指定类名的节点
     * @param className 类名正则表达式 
     */
    private void clickNodeByClassName(AccessibilityNodeInfo rootNode, Pattern className) {
        AccessibilityNodeInfo node = getChildByRegex(rootNode,className);
        if(node != null) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

    }

    /**
     * 点击指定文案的节点 
     * @param textList 选文案列表,按顺序查找,找到即停止查找
     * @param parentCount 0 表示点击查找到的节点,大于 0 表示点击向上查找指定层级的父节点
    */
    private void clickNodeByText(AccessibilityNodeInfo rootNode, String[] textList, int parentCount) {
        AccessibilityNodeInfo node = getNodeByText(rootNode,textList);
        for (int i = 0; i < parentCount; i++) {
            node = node.getParent();
        }
        if (node != null) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

    }

     /**
     * 查找指定文案的节点
     * @param textList 备选文案列表，按顺序查找，找到即停止查找
     * @return 对应的节点或者 null
     */
    private AccessibilityNodeInfo getNodeByText(AccessibilityNodeInfo rootNode, String[] textList) {

        AccessibilityNodeInfo result = null;
        int index = 0;

        while(index < textList.length && result == null) {
            
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(textList[index]);
            if(!nodes.isEmpty()) {
            result = nodes.get(0); 
            }

            index++;
        }

        return result;

    }

    private AccessibilityNodeInfo getRootNodeInfo() {

        AccessibilityNodeInfo rootNodeInfo = null;
        // 获取所有窗口信息
        List<AccessibilityWindowInfo> windows = super.getWindows();
        
        for (AccessibilityWindowInfo window : windows) {
            if (window.getType() == AccessibilityWindowInfo.TYPE_APPLICATION) {
                rootNodeInfo = window.getRoot();
                break;
            }
        }
        
        return rootNodeInfo;
    }


    /**
     * 查找指定类名的节点 
     * @param className 类名正则表达式
     * @return 对应的节点或者null
    */
    private AccessibilityNodeInfo getChildByRegex(AccessibilityNodeInfo rootNode,Pattern className) {
        String regexString = className.pattern();
        LinkedList<AccessibilityNodeInfo> queue = new LinkedList<>();
        queue.offer(rootNode);
        AccessibilityNodeInfo info;
        while (!queue.isEmpty()) {
            info = queue.poll();
            if (info == null) { 
                continue;
            }
            String infoClassName = info.getClassName().toString();
            if (infoClassName.matches(regexString)) {
                return info;
            }
            for (int i = 0; i < info.getChildCount(); i++) {
                queue.offer(info.getChild(i));
            }
        }
        return null;

    }

    @Override
    public void onInterrupt() {

    }
}
