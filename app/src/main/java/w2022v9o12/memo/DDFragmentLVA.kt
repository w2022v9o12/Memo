package w2022v9o12.memo

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.TimeUnit
import com.google.firebase.database.ktx.getValue

class DDFragmentLVA(val dataList: List<DDayDM>, val context: Context) : BaseAdapter() {
    private lateinit var auth: FirebaseAuth

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        auth = Firebase.auth

        val database = Firebase.database
        val dDayRef = database.getReference("D-DayList").child(auth.currentUser!!.uid)

        var itemView = convertView
        if(itemView == null) {
            itemView = LayoutInflater.from(parent?.context).inflate(R.layout.ddf_lv_item, parent, false)
        }

        // D-Day 날짜 계산
        val startDate = Calendar.getInstance()
        startDate.set(dataList[position].startDate[0], dataList[position].startDate[1], dataList[position].startDate[2])    // 년도, 월, 일
        val endDate = Calendar.getInstance()
        endDate.set(dataList[position].endDate[0], dataList[position].endDate[1], dataList[position].endDate[2])

        val calcDateResult = TimeUnit.MILLISECONDS.toDays(endDate.timeInMillis - startDate.timeInMillis)

        // 어떤 뷰가 어떤 데이터를 받을지 설정
        // 제목
        val title = itemView!!.findViewById<TextView>(R.id.ddf_lvi_textView)
        title.text = "${dataList[position].title} ( D - ${calcDateResult} )"

        // 수정 버튼 클릭
        val editButton = itemView!!.findViewById<Button>(R.id.ddf_lvi_button)
        editButton.setOnClickListener {
            val cldDialogView = LayoutInflater.from(context).inflate(R.layout.calc_dday_dialog, null)
            val cldDialog = AlertDialog.Builder(context).setView(cldDialogView)
            val cldDialogShow = cldDialog.show()

            val cldTitle = cldDialogShow.findViewById<EditText>(R.id.dday_dialog_title)
            cldTitle?.setText(dataList[position].title)
            val cldContent = cldDialogShow.findViewById<EditText>(R.id.dday_dialog_content)
            cldContent?.setText(dataList[position].content)
            val cldStartDateText = cldDialogShow.findViewById<TextView>(R.id.dday_dialog_startDate)
            val cldEndDateText = cldDialogShow.findViewById<TextView>(R.id.dday_dialog_endDate)

            // 다이얼로그: 시작 날짜 버튼
            var sYear = 0
            var sMonth = 0
            var sDay = 0

            val pickStartDate = cldDialogShow.findViewById<Button>(R.id.dday_dialog_sdBtn)
            pickStartDate?.setOnClickListener {
                val gCalendar = GregorianCalendar()
                val nYear = gCalendar.get(Calendar.YEAR)
                val nMonth = gCalendar.get(Calendar.MONTH)
                val nDay = gCalendar.get(Calendar.DATE)

                val startDatePick = DatePickerDialog(context, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        cldStartDateText?.setText("시작: ${year}년 ${month + 1}월 ${dayOfMonth}일")
                        sYear = year
                        sMonth = month
                        sDay = dayOfMonth
                    }
                }, nYear, nMonth, nDay)
                startDatePick.show()
            }

            // 다이얼로그: 종료 날짜 버튼
            var eYear = 0
            var eMonth = 0
            var eDay = 0

            val pickEndDate = cldDialogShow.findViewById<Button>(R.id.dday_dialog_edBtn)
            pickEndDate?.setOnClickListener {
                val gCalendar = GregorianCalendar()
                val nYear = gCalendar.get(Calendar.YEAR)
                val nMonth = gCalendar.get(Calendar.MONTH)
                val nDay = gCalendar.get(Calendar.DATE)

                val endDatePick = DatePickerDialog(context, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        cldEndDateText?.setText("시작: ${year}년 ${month + 1}월 ${dayOfMonth}일")
                        eYear = year
                        eMonth = month
                        eDay = dayOfMonth
                    }
                }, nYear, nMonth, nDay)
                endDatePick.show()
            }

            // 저장을 위한 사전 작업
            var key = ""
            dDayRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    for(setItem in snapshot.children){
                        if(position == count) {
                            key = setItem.key!!
                        }
                        count++
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            // 다이얼로그: 저장 버튼 클릭
            val cldSaveButton = cldDialogShow.findViewById<Button>(R.id.dday_dialog_saveBtn)
            cldSaveButton?.setOnClickListener {
                dDayRef.child(key).setValue(DDayDM(cldTitle?.text.toString(), cldContent?.text.toString(), arrayListOf<Int>(sYear, sMonth, sDay), arrayListOf<Int>(eYear, eMonth, eDay)))

                cldDialogShow.dismiss()
            }

            // 다이얼로그: 취소 버튼 클릭
            val cldCancelButton = cldDialogShow.findViewById<Button>(R.id.dday_dialog_cancelBtn)
            cldCancelButton?.setOnClickListener { cldDialogShow.dismiss() }

            // 다이얼로그: 삭제 버튼 클릭
            val cldDeleteBtn = cldDialogShow.findViewById<ImageView>(R.id.dday_dialog_delete)
            cldDeleteBtn?.visibility = View.VISIBLE
            cldDeleteBtn?.setOnClickListener {
                dDayRef.child(key).removeValue()

                cldDialogShow.dismiss()
            }
        }

        return itemView
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}