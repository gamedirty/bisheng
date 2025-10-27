package com.sovnem.bisheng

import com.sovnem.bisheng.databinding.LayoutPeopleItemBinding

@VHRef(PeopleViewHolder::class, lazyLoad = false)
data class PeopleListItem(val name: String, val age: Int)

@VHLayoutId(R.layout.layout_people_item, lazyLoad = false)
class PeopleViewHolder : BiShengBaseVH<PeopleListItem>() {
    
    private val binding: LayoutPeopleItemBinding by lazy {
        LayoutPeopleItemBinding.bind(containerView)
    }
    
    override fun bindData(
        data: PeopleListItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {
        binding.name.text = "名字：" + data.name
        binding.age.text = "年龄：" + data.age
    }

}