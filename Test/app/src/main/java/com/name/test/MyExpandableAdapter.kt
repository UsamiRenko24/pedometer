package com.name.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class MyExpandableAdapter(
    private val context: Context,
    private val groupList: List<Group>,
    private val childMap: Map<Int, List<Child>>
) : BaseExpandableListAdapter() {  // ⭐ 关键：必须继承这个！

    // ========== 分组相关方法 ==========
    override fun getGroupCount(): Int = groupList.size

    override fun getGroup(groupPosition: Int): Any = groupList[groupPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_group, parent, false)

        val group = groupList[groupPosition]

        view.findViewById<TextView>(R.id.tv_group_name).text = group.name
        view.findViewById<TextView>(R.id.tv_group_count).text = "(${group.count})"

        return view
    }

    // ========== 子项相关方法 ==========
    override fun getChildrenCount(groupPosition: Int): Int {
        return childMap[groupPosition]?.size ?: 0
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childMap[groupPosition]?.get(childPosition) ?: Any()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_child, parent, false)

        val child = childMap[groupPosition]?.get(childPosition)

        view.findViewById<TextView>(R.id.tv_child_name).text = child?.name
        view.findViewById<TextView>(R.id.tv_child_phone).text = child?.phone

        return view
    }

    // ========== 其他必要方法 ==========
    override fun hasStableIds(): Boolean = true

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}