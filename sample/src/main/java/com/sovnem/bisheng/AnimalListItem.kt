package com.sovnem.bisheng

import com.sovnem.bisheng.sample.R

@VHRef(AnimalViewHolder::class, lazyLoad = true)
data class AnimalListItem(val animalName: String)

@VHLayoutId(lazyLoad = true)
class AnimalViewHolder : BiShengBaseVH<AnimalListItem>() {
    
    override fun onCreateView(parent: android.view.ViewGroup): android.view.View {
        return android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_animal_item, parent, false)
    }
    
    override fun bindData(
        data: AnimalListItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {
        // Bind animal data here
    }
}