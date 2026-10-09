package com.demo.app.ui.guide;

/**
 * 引导页宿主回调：Fragment 不自行跳 Activity，只通知 GuideActivity。
 */
public interface GuideNavigator {
    void goNextGuidePage();

    void goPrevGuidePage();

    /** 直接打开某一页，不按“下一页”再加一。 */
    void openGuidePage(int page);

    /** 语言已写入后，按新语种重新创建后面的引导页。 */
    void reloadGuideAfterLanguage();

    void finishGuideFlow();

    void skipGuideFlow();

    /** 翻页已经停稳。滑动过程中为 false，避免边滑边跑重动画。 */
    boolean isGuidePageIdle();
}
