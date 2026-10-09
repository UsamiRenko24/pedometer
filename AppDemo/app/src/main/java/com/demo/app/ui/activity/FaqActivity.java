package com.demo.app.ui.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.demo.app.R;
import com.demo.app.data.enum2.FaqItem;
import com.demo.app.databinding.ActivityFaqBinding;
import com.demo.app.utils.AppLog;

/** 常见问题列表。点某一条进入对应详情。 */
public class FaqActivity extends BaseActivity<ActivityFaqBinding> {

    /** 创建列表页布局。 */
    @Nullable
    @Override
    protected ActivityFaqBinding getViewBinding() {
        return ActivityFaqBinding.inflate(getLayoutInflater());
    }

    /** 按枚举顺序填入全部问题，点击后打开对应详情。 */
    @Override
    protected void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (FaqItem item : FaqItem.values()) {
            View row = inflater.inflate(R.layout.item_faq, binding.faqList, false);
            TextView question = row.findViewById(R.id.tv_question);
            question.setText(item.getQuestionRes());
            row.setOnClickListener(v -> {
                AppLog.i("FaqActivity", "open " + item.name());
                startActivity(FaqDetailActivity.intent(this, item));
            });
            binding.faqList.addView(row);
        }
    }

    /** 返回按钮关闭列表页。 */
    @Override
    protected void initEvents() {
        binding.btnFaqBack.setOnClickListener(v -> finish());
    }
}
