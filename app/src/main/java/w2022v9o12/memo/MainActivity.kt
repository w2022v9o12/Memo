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
import java.util.Calendar
import java.util.GregorianCalendar

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 파이어베이스 준비
        auth = Firebase.auth
        val database = Firebase.database
        val memoListRef = database.getReference("MemoList").child(auth.currentUser!!.uid)
        val dDayListRef = database.getReference("D-DayList").child(auth.currentUser!!.uid)
        val countRef = database.getReference("Count").child(auth.currentUser!!.uid)

        // 제작 횟수 정보 가져오기
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

        // 유저 아이콘 클릭
        val userButton = findViewById<ImageView>(R.id.main_user_imageView)
        userButton.setOnClickListener {
            val uiDialogView = LayoutInflater.from(this).inflate(R.layout.user_info_dialog, null)
            val uiDialog = AlertDialog.Builder(this).setView(uiDialogView)
            val showingDialog = uiDialog.show()

            // 다이얼로그: 가입한 이메일 표시
            val uidUserEmail = showingDialog.findViewById<TextView>(R.id.uid_user_email_textView)
            uidUserEmail?.text = auth.currentUser?.email

            // 다이얼로그: 메모 개수, D-Day 개수
            val uidMemoCount = showingDialog.findViewById<TextView>(R.id.uid_memo_count)
            uidMemoCount?.text = "⊙ 메모 ${memoCount}번 사용"
            val uidDDayCount = showingDialog.findViewById<TextView>(R.id.uid_dday_count)
            uidDDayCount?.text = "⊙ D-Day ${dDayCount}번 사용"

            // 다이얼로그: 로그아웃
            val uidLogoutButton = showingDialog.findViewById<TextView>(R.id.uid_logout_textView)
            uidLogoutButton?.setOnClickListener {
                Firebase.auth.signOut()

                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }

        // 메모 작성 아이콘 클릭
        val writeMemoButton = findViewById<ImageView>(R.id.main_write_memo)
        writeMemoButton.setOnClickListener {
            val wmDialogView = LayoutInflater.from(this).inflate(R.layout.write_memo_dialog, null)
            val wmDialog = AlertDialog.Builder(this).setView(wmDialogView)
            val showingDialog = wmDialog.show()

            // 다이얼로그: 저장 버튼
            val wmdSaveButton = showingDialog.findViewById<Button>(R.id.wmd_save_button)
            wmdSaveButton?.setOnClickListener {
                val title = showingDialog.findViewById<EditText>(R.id.wmd_title_editText)?.text.toString()
                val content = showingDialog.findViewById<EditText>(R.id.wmd_content_editText)?.text.toString()

                // 저장 버튼 클릭한 날짜
                val calendar = GregorianCalendar()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DATE)
                val hour = calendar.get(Calendar.HOUR)

                // 메모 저장
                memoListRef.push().setValue(MemoDM(title, content, "${year}년 ${month}월 ${day}일 ${hour}시"))
                countRef.child("MemoCount").setValue(memoCount + 1)

                showingDialog.dismiss()
            }

            // 다이얼로그: 취소 버튼
            val wmdCancelButton = showingDialog.findViewById<Button>(R.id.wmd_cancel_button)
            wmdCancelButton?.setOnClickListener { showingDialog.dismiss() }
        }

        // D-Day 작성 아이콘 클릭
        val setDDayButton = findViewById<ImageView>(R.id.main_set_dDay)
        setDDayButton.setOnClickListener {
            val cdDialogView = LayoutInflater.from(this).inflate(R.layout.calc_dday_dialog, null)
            val cdDialog = AlertDialog.Builder(this).setView(cdDialogView)
            val showingDialog = cdDialog.show()

            val cdTitle = showingDialog.findViewById<EditText>(R.id.dday_dialog_title)
            val cdContent = showingDialog.findViewById<EditText>(R.id.dday_dialog_content)
            val cdStartDateText = showingDialog.findViewById<TextView>(R.id.dday_dialog_startDate)
            val cdEndDateText = showingDialog.findViewById<TextView>(R.id.dday_dialog_endDate)

            // 다이얼로그: 시작 날짜 버튼
            var sYear = 0
            var sMonth = 0
            var sDay = 0

            val pickStartDate = showingDialog.findViewById<Button>(R.id.dday_dialog_sdBtn)
            pickStartDate?.setOnClickListener {
                val gCalendar = GregorianCalendar()
                val nYear = gCalendar.get(Calendar.YEAR)
                val nMonth = gCalendar.get(Calendar.MONTH)
                val nDay = gCalendar.get(Calendar.DATE)

                val startDatePick = DatePickerDialog(this, object : OnDateSetListener{
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        cdStartDateText?.setText("시작: ${year}년 ${month + 1}월 ${dayOfMonth}일")
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

            val pickEndDate = showingDialog.findViewById<Button>(R.id.dday_dialog_edBtn)
            pickEndDate?.setOnClickListener {
                val gCalendar = GregorianCalendar()
                val nYear = gCalendar.get(Calendar.YEAR)
                val nMonth = gCalendar.get(Calendar.MONTH)
                val nDay = gCalendar.get(Calendar.DATE)

                val endDatePick = DatePickerDialog(this, object : OnDateSetListener{
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        cdEndDateText?.setText("종료: ${year}년 ${month + 1}월 ${dayOfMonth}일")
                        eYear = year
                        eMonth = month
                        eDay = dayOfMonth
                    }
                }, nYear, nMonth, nDay)
                endDatePick.show()
            }

            // 다이얼로그: 저장 버튼
            val cdSaveButton = showingDialog.findViewById<Button>(R.id.dday_dialog_saveBtn)
            cdSaveButton?.setOnClickListener {
                dDayListRef.push().setValue(DDayDM(cdTitle?.text.toString(), cdContent?.text.toString(), arrayListOf<Int>(sYear, sMonth, sDay), arrayListOf<Int>(eYear, eMonth, eDay)))
                countRef.child("D-DayCount").setValue(dDayCount + 1)

                showingDialog.dismiss()
            }

            // 다이얼로그: 취소 버튼
            val cdCancelButton = showingDialog.findViewById<Button>(R.id.dday_dialog_cancelBtn)
            cdCancelButton?.setOnClickListener { showingDialog.dismiss() }
        }
    }

    // 뒤로가기 버튼 두 번 눌러야 종료되도록 작업
    private var isExit = false
    override fun onBackPressed() {
        if(!isExit) {
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()

            isExit = true
            Handler().postDelayed({ isExit = false }, 1500)
        } else {
            finish()
        }
    }
}