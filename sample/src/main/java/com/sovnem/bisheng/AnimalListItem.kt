package com.sovnem.bisheng

@VHRef(AnimalViewHolder::class)
data class AnimalListItem(val animalName: String)

@VHLayoutId(R.layout.layout_animal_item)
class AnimalViewHolder : BiShengBaseVH<AnimalListItem>() {
    override fun bindData(
        data: AnimalListItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {

    }
}