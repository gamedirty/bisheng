package com.sovnem.bisheng

import com.sovnem.bisheng.sample.R
import com.sovnem.bisheng.sample.databinding.LayoutPeopleItemBinding

@VHRef(PeopleViewHolder::class, lazyLoad = true)
data class PeopleListItem(val name: String, val age: Int)

@VHLayoutId(lazyLoad = true)
class PeopleViewHolder : BiShengBaseVH<PeopleListItem>() {
    
    private val binding: LayoutPeopleItemBinding by lazy {
        LayoutPeopleItemBinding.bind(containerView)
    }
    
    override fun onCreateView(parent: android.view.ViewGroup): android.view.View {
        return android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_people_item, parent, false)
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