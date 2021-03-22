package com.sovnem.bisheng

import kotlinx.android.synthetic.main.layout_people_item.view.*

@VHRef(PeopleViewHolder::class)
data class PeopleListItem(val name: String, val age: Int)

@VHLayoutId(R.layout.layout_people_item)
class PeopleViewHolder : BiShengBaseVH<PeopleListItem>() {
    override fun bindData(
        data: PeopleListItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {
        containerView.name.text = "名字：" + data.name
        containerView.age.text = "年龄：" + data.age
    }

}