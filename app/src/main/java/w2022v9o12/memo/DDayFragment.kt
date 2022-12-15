package w2022v9o12.memo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ktx.getValue

class DDayFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // 파이어베이스 준비
        auth = Firebase.auth
        val database = Firebase.database
        val dDayListRef = database.getReference("D-DayList").child(auth.currentUser!!.uid)

        // 프래그먼트 작업 시작
        val fragmentView = inflater.inflate(R.layout.fragment_d_day, container, false)

        // ListView 작업
        val dDayList = ArrayList<DDayDM>()
        val listView = fragmentView.findViewById<ListView>(R.id.ddFragment_listView)
        val listViewAdapter = DDFragmentLVA(dDayList, requireContext())

        dDayListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dDayList.clear()
                for(setItem in snapshot.children) {
                    dDayList.add(setItem.getValue(DDayDM::class.java)!!)
                }

                // ListView 새로 고침 (이유: 비동기식 처리)
                listViewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        listView.adapter = listViewAdapter

        // 네비게이션을 통한 이동 작업
        val memoButton = fragmentView.findViewById<TextView>(R.id.ddFragment_memo_btn)
        memoButton.setOnClickListener { memoButton.findNavController().navigate(R.id.action_DDayFragment_to_memoMainFragment) }

        return fragmentView
    }
}