package com.name.test

import android.annotation.SuppressLint
import android.os.Bundle

import android.widget.ExpandableListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 准备数据
        val groups = listOf(
            Group("我的好友", 3),
            Group("家人", 2),
            Group("同事", 4)
        )

        val children = mapOf(
            0 to listOf(
                Child("张三", "13800138000"),
                Child("李四", "13800138001"),
                Child("王五", "13800138002")
            ),
            1 to listOf(
                Child("爸爸", "13800138100"),
                Child("妈妈", "13800138101")
            ),
            2 to listOf(
                Child("赵经理", "13800138200"),
                Child("钱主管", "13800138201"),
                Child("孙同事", "13800138202"),
                Child("周同事", "13800138203")
            )
        )

        // 2. 创建适配器
        val adapter = MyExpandableAdapter(this, groups, children)

        // 3. 找到 ExpandableListView 并设置适配器
        val expandableListView = findViewById<ExpandableListView>(R.id.expandable_list)
        expandableListView.setAdapter(adapter)  // ✅ 现在可以了！
    }
}