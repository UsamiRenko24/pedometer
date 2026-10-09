package com.demo.app.data.enum2;

import androidx.annotation.StringRes;

import com.demo.app.R;

/** 常见问题。每题带标题、答案，以及详情页是否显示默认桌面入口。 */
public enum FaqItem {
    DISORGANIZED(R.string.faq_q1, R.string.faq_a1, true),
    RESTORE_LAYOUT(R.string.faq_q2, R.string.faq_a2, true),
    MANY_ADS(R.string.faq_q3, R.string.faq_a3, false),
    ADS_SAFE(R.string.faq_q4, R.string.faq_a4, false),
    LAUNCHER_SECURITY(R.string.faq_q5, R.string.faq_a5, false),
    CHOOSE_LAUNCHER(R.string.faq_q6, R.string.faq_a6, false),
    VIRUS(R.string.faq_q7, R.string.faq_a7, false),
    PERMISSIONS(R.string.faq_q8, R.string.faq_a8, false),
    UNINSTALL(R.string.faq_q9, R.string.faq_a9, false);

    @StringRes
    private final int questionRes;
    @StringRes
    private final int answerRes;
    private final boolean showHomeBottom;

    /** 绑定这道题的文案，以及详情页要不要显示默认桌面入口。 */
    FaqItem(@StringRes int questionRes, @StringRes int answerRes, boolean showHomeBottom) {
        this.questionRes = questionRes;
        this.answerRes = answerRes;
        this.showHomeBottom = showHomeBottom;
    }

    /** 问题标题。 */
    @StringRes
    public int getQuestionRes() {
        return questionRes;
    }

    /** 答案正文，可带加粗。 */
    @StringRes
    public int getAnswerRes() {
        return answerRes;
    }

    /** 第 1、2 题为 true，详情页底部显示 Default Home Screen。 */
    public boolean showHomeBottom() {
        return showHomeBottom;
    }
}
