package com.demo.app.ui.guide;

/**
 * 引导页里会持续重绘的动效。翻页时先停，停稳后再开。
 */
public interface GuidePageMotion {
    void setPageSettled(boolean settled);
}
