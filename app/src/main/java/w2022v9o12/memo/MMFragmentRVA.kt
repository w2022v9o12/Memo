package w2022v9o12.memo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MMFragmentRVA(val memoDMList: List<MemoDM>) : RecyclerView.Adapter<MMFragmentRVA.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView = itemView.findViewById<TextView>(R.id.mmf_rv_item_title)
        val contentTextView = itemView.findViewById<TextView>(R.id.mmf_rv_item_content)

        fun bindData(memoDM: MemoDM) {
            titleTextView.text = memoDM.title
            titleTextView.setBackgroundColor(Color.parseColor(memoDM.color))

            contentTextView.text = memoDM.content
            contentTextView.setBackgroundColor(Color.parseColor(memoDM.color))
        }
    }

    override fun getItemCount(): Int {
        return memoDMList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mmf_rv_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(memoDMList[position])

        // 메모 (아이템) 클릭 이벤트 처리
        if(eventListener != null) { holder.itemView.setOnClickListener { eventListener?.itemClick(it, position) } }
    }

    // 이벤트 처리를 위한 사전 작업
    interface ItemClickListener {
        fun itemClick(itemView: View, position: Int)
    }
    var eventListener: ItemClickListener? = null
}