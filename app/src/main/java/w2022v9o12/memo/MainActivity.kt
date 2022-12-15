package w2022v9o12.memo

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ktx.getValue
import org.w3c.dom.Text
import java.util.Calendar
import java.util.GregorianCalendar

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val database = Firebase.database
        val myRef = database.getReference("MemoList").child(auth.currentUser!!.uid)
        val dDayRef = database.getReference("D-DayList").child(auth.currentUser!!.uid)
        val countRef = database.getReference("Count").child(auth.currentUser!!.uid)

        // 사용 횟수 가져오기
        var memoCount = 0
        var dDayCount = 0
        countRef.child("MemoCount").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                memoCount = snapshot.getValue<Int>()!!
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        countRef.child("D-DayCount").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dDayCount = snapshot.getValue<Int>()!!
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // User 아이콘 클릭
        val userButton = findViewById<ImageView>(R.id.main_user_imageView)
        userButton.setOnClickListener {
            val uiDialogView = LayoutInflater.from(this).inflate(R.layout.user_info_dialog, null)
            val uiDialog = AlertDialog.Builder(this).setView(uiDialogView)
            val uiDialogShow = uiDialog.show()

            // User 아이콘 다이얼로그: 가입한 이메일 가져오기
            val uidUserEmail = uiDialogShow.findViewById<TextView>(R.id.uid_user_email_textView)
            uidUserEmail!!.text = auth.currentUser?.email!!

            // User 아이콘 다이얼로그: 메모 개수, D-Day 개수
            val uidMemoCount = uiDialogShow.findViewById<TextView>(R.id.uid_memo_count)
            uidMemoCount?.text = "⊙ 메모 ${memoCount}번 사용"
            val uidDDayCount = uiDialogShow.findViewById<TextView>(R.id.uid_dday_count)
            uidDDayCount?.text = "⊙ D-Day ${dDayCount}번 사용"

            // User 아이콘 다이얼로그: 로그아웃 클릭
            val uidLogoutButton = uiDialogShow.findViewById<TextView>(R.id.uid_logout_textView)
            uidLogoutButton!!.setOnClickListener {
                Firebase.auth.signOut()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }

        // Writing 아이콘 클릭
        val writeMemoButton = findViewById<ImageView>(R.id.main_write_memo)
        writeMemoButton.setOnClickListener {
            val wmDialogView = LayoutInflater.from(this).inflate(R.layout.write_memo_dialog, null)
            val wmDialog = AlertDialog.Builder(this).setView(wmDialogView)
            val wmDialogShow = wmDialog.show()

            // Writing 아이콘 다이얼로그: 저장 버튼 클릭
            val wmdSaveButton = wmDialogShow.findViewById<Button>(R.id.wmd_save_button)
            wmdSaveButton!!.setOnClickListener {
                val title = wmDialogShow.findViewById<EditText>(R.id.wmd_title_editText)!!.text.toString()
                val content = wmDialogShow.findViewById<EditText>(R.id.wmd_content_editText)!!.text.toString()

                // 저장한 날짜
                val calendar = GregorianCalendar()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DATE)
                val hour = calendar.get(Calendar.HOUR)

                myRef.push().setValue(MemoDM(title, content, "${year}년 ${month}월 ${day}일 ${hour}시"))
                countRef.child("MemoCount").setValue(memoCount + 1)    // 메모 사용 횟수 작업

                wmDialogShow.dismiss()
            }

            // Writing 아이콘 다이얼로그: 취소 버튼 클릭
            val wmdCancelButton = wmDialogShow.findViewById<Button>(R.id.wmd_cancel_button)
            wmdCancelButton!!.setOnClickListener { wmDialogShow.dismiss() }
        }

        // Calendar 아이콘 클릭
        val setDDayButton = findViewById<ImageView>(R.id.main_set_dDay)
        setDDayButton.setOnClickListener {
            val cldDialogView = LayoutInflater.from(this).inflate(R.layout.calc_dday_dialog, null)
            val cldDialog = AlertDialog.Builder(this).setView(cldDialogView)
            val cldDialogShow = cldDialog.show()

            val cldTitle = cldDialogShow.findViewById<EditText>(R.id.dday_dialog_title)
            val cldContent = cldDialogShow.findViewById<EditText>(R.id.dday_dialog_content)
            val cldStartDateText = cldDialogShow.findViewById<TextView>(R.id.dday_dialog_startDate)
            val cldEndDateText = cldDialogShow.findViewById<TextView>(R.id.dday_dialog_endDate)

            // Calendar 아이콘 다이얼로그: 시작 날짜 버튼
            var sYear = 0
            var sMonth = 0
            var sDay = 0

            val pickStartDate = cldDialogShow.findViewById<Button>(R.id.dday_dialog_sdBtn)
            pickStartDate?.setOnClickListener {
                val gCalendar = GregorianCalendar()
                val nYear = gCalendar.get(Calendar.YEAR)
                val nMonth = gCalendar.get(Calendar.MONTH)
                val nDay = gCalendar.get(Calendar.DATE)

                val startDatePick = DatePickerDialog(this, object : OnDateSetListener{
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        cldStartDateText?.setText("시작: ${year}년 ${month + 1}월 ${dayOfMonth}일")
                        sYear = year
                        sMonth = month
                        sDay = dayOfMonth
                    }
                }, nYear, nMonth, nDay)
                startDatePick.show()
            }

            // Calendar 아이콘 다이얼로그: 종료 날짜 버튼
            var eYear = 0
            var eMonth = 0
            var eDay = 0

            val pickEndDate = cldDialogShow.findViewById<Button>(R.id.dday_dialog_edBtn)
            pickEndDate?.setOnClickListener {
                val gCalendar = GregorianCalendar()
                val nYear = gCalendar.get(Calendar.YEAR)
                val nMonth = gCalendar.get(Calendar.MONTH)
                val nDay = gCalendar.get(Calendar.DATE)

                val endDatePick = DatePickerDialog(this, object : OnDateSetListener{
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        cldEndDateText?.setText("시작: ${year}년 ${month + 1}월 ${dayOfMonth}일")
                        eYear = year
                        eMonth = month
                        eDay = dayOfMonth
                    }
                }, nYear, nMonth, nDay)
                endDatePick.show()
            }

            // Calendar 아이콘 다이얼로그: 저장 버튼 클릭
            val cldSaveButton = cldDialogShow.findViewById<Button>(R.id.dday_dialog_saveBtn)
            cldSaveButton?.setOnClickListener {
                dDayRef.push().setValue(DDayDM(cldTitle?.text.toString(), cldContent?.text.toString(), arrayListOf<Int>(sYear, sMonth, sDay), arrayListOf<Int>(eYear, eMonth, eDay)))
                countRef.child("D-DayCount").setValue(dDayCount + 1)    // D-Day 사용 횟수 작업

                cldDialogShow.dismiss()
            }

            // Calendar 아이콘 다이얼로그: 취소 버튼 클릭
            val cldCancelButton = cldDialogShow.findViewById<Button>(R.id.dday_dialog_cancelBtn)
            cldCancelButton?.setOnClickListener { cldDialogShow.dismiss() }
        }
    }

    var isExit = false
    override fun onBackPressed() {
        if(isExit) {
            finish()
        } else {
            isExit = true
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()

            Handler().postDelayed({
                isExit = false
            }, 1500)
        }
    }
}