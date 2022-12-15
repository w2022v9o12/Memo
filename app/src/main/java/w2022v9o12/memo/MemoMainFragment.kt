package w2022v9o12.memo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ktx.getValue
import java.util.*
import kotlin.collections.ArrayList

class MemoMainFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // 파이어베이스 준비
        auth = Firebase.auth
        val database = Firebase.database
        val memoListRef = database.getReference("MemoList").child(auth.currentUser!!.uid)

        // 프래그먼트 작업 시작
        val fragmentView = inflater.inflate(R.layout.fragment_memo_main, container, false)

        // RecyclerView 작업
        val memoDMList = ArrayList<MemoDM>()

        val mmfRecyclerView = fragmentView.findViewById<RecyclerView>(R.id.mmFragment_recyclerView)
        val mmfRecyclerViewAdapter = MMFragmentRVA(memoDMList)
        mmfRecyclerView.adapter = mmfRecyclerViewAdapter

        val rvLayout = GridLayoutManager(requireContext(), 2)
        mmfRecyclerView?.layoutManager = rvLayout

        memoListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val keyList = ArrayList<String>()    // 데이터 삭제를 위한 리스트

                // 저장했던 메모 정보 불러오기
                memoDMList.clear()
                for(item in snapshot.children) {
                    memoDMList.add(item.getValue(MemoDM::class.java)!!)

                    keyList.add(item.key!!)
                }

                // 메모 (아이템) 클릭
                mmfRecyclerViewAdapter.eventListener = object : MMFragmentRVA.ItemClickListener {
                    override fun itemClick(itemView: View, position: Int) {
                        val memoDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.mmf_rv_item_click_dialog, null)
                        val memoDialog = AlertDialog.Builder(requireContext()).setView(memoDialogView)
                        val showingDialog = memoDialog.show()

                        val titleTextView = showingDialog.findViewById<TextView>(R.id.memo_click_title)
                        titleTextView?.text = memoDMList[position].title

                        val contentTextView = showingDialog.findViewById<TextView>(R.id.memo_click_content)
                        contentTextView?.text = memoDMList[position].content

                        val dateTextView = showingDialog.findViewById<TextView>(R.id.memo_click_date)
                        dateTextView?.text = memoDMList[position].date

                        // 휴지통 (삭제) 아이콘 클릭
                        val trashCanIcon = showingDialog.findViewById<ImageView>(R.id.memo_click_trash_can)
                        trashCanIcon?.setOnClickListener {
                            memoListRef.child(keyList[position]).removeValue()

                            showingDialog.dismiss()
                        }

                        // 수정하기 아이콘 클릭
                        val updateIcon = showingDialog.findViewById<ImageView>(R.id.memo_click_update)
                        updateIcon?.setOnClickListener {
                            val updateMemoDialog = LayoutInflater.from(requireContext()).inflate(R.layout.write_memo_dialog, null)
                            val updateMemo = AlertDialog.Builder(requireContext()).setView(updateMemoDialog)
                            val showingUpdateMemo = updateMemo.show()

                            val title = showingUpdateMemo.findViewById<EditText>(R.id.wmd_title_editText)
                            title?.setText(memoDMList[position].title)

                            val content = showingUpdateMemo.findViewById<EditText>(R.id.wmd_content_editText)
                            content?.setText(memoDMList[position].content)

                            // 저장 버튼
                            val saveButton = showingUpdateMemo.findViewById<Button>(R.id.wmd_save_button)
                            saveButton?.setOnClickListener {
                                // 저장 버튼 클릭한 날짜
                                val calendar = GregorianCalendar()
                                val year = calendar.get(Calendar.YEAR)
                                val month = calendar.get(Calendar.MONTH) + 1
                                val day = calendar.get(Calendar.DATE)
                                val hour = calendar.get(Calendar.HOUR)

                                memoDMList[position].title = title?.text.toString()
                                memoDMList[position].content = content?.text.toString()
                                memoDMList[position].date = "${year}년 ${month}월 ${day}일 ${hour}시 (수정됨)"
                                memoListRef.child(keyList[position]).setValue(memoDMList[position])

                                showingUpdateMemo.dismiss()
                                showingDialog.dismiss()
                            }

                            // 취소 버튼
                            val cancelButton = showingUpdateMemo.findViewById<Button>(R.id.wmd_cancel_button)
                            cancelButton?.setOnClickListener { showingUpdateMemo.dismiss() }
                        }
                    }
                }

                mmfRecyclerViewAdapter.notifyDataSetChanged()    // 리사이클러 뷰에 전할 데이터가 준비되고 나서 새로고침 (이유: 비동기식 처리)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 네비게이션을 통한 이동 작업
        val dDayButton = fragmentView.findViewById<TextView>(R.id.mmFragment_dday_btn)
        dDayButton.setOnClickListener { dDayButton.findNavController().navigate(R.id.action_memoMainFragment_to_DDayFragment) }

        return fragmentView
    }
}