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

class DDayFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        auth = Firebase.auth

        val database = Firebase.database
        val myRef = database.getReference("D-DayList").child(auth.currentUser!!.uid)

        val fragmentView = inflater.inflate(R.layout.fragment_d_day, container, false)

        // ListView 작업
        val dDayList = ArrayList<DDayDM>()    // D-Day 날짜 정보가 담길 리스트
        val listView = fragmentView.findViewById<ListView>(R.id.ddFragment_listView)
        val lvAdapter = DDFragmentLVA(dDayList, requireContext())
        listView.adapter = lvAdapter

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dDayList.clear()
                for(setItem in snapshot.children) {
                    dDayList.add(setItem.getValue(DDayDM::class.java)!!)
                }

                lvAdapter.notifyDataSetChanged()    // ListView 새로 고침 (이유: 비동기 처리)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 네비게이션을 이용한 이동 작업
        val memoButton = fragmentView.findViewById<TextView>(R.id.ddFragment_memo_btn)
        memoButton.setOnClickListener { memoButton.findNavController().navigate(R.id.action_DDayFragment_to_memoMainFragment) }

        return fragmentView
    }
}