package w2022v9o12.memo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ktx.getValue

class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // 파이어베이스 준비
        auth = Firebase.auth
        val database = Firebase.database
        val emailListRef = database.getReference("EmailList")
        val countRef = database.getReference("Count")

        // 이미 가입되어 있는 이메일 가져오기
        val emailArrayList = ArrayList<String>()
        emailListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(setItem in snapshot.children) { emailArrayList.add(setItem.value.toString()) }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 로그인 버튼
        val loginButton = findViewById<Button>(R.id.auth_login_button)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.auth_email_editText).text.toString()
            val password = findViewById<EditText>(R.id.auth_password_editText).text.toString()

            // 로그인 오류 띄워주기
            if(email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }

            else if(password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }

            // 로그인 시도
            else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // 회원가입 버튼 (회원가입 다이얼로그 실행)
        val authSignUpTextView = findViewById<TextView>(R.id.auth_signUp_textView)
        authSignUpTextView.setOnClickListener {
            val signUpDialogView = LayoutInflater.from(this).inflate(R.layout.sign_up_dialog, null)
            val signUpDialog = AlertDialog.Builder(this).setView(signUpDialogView)
            val showingDialog = signUpDialog.show()

            // 회원가입 다이얼로그: 확인 버튼
            val confirmButton = showingDialog.findViewById<Button>(R.id.signUp_dialog_confirm_button)
            confirmButton?.setOnClickListener {
                val email = showingDialog.findViewById<EditText>(R.id.signUp_dialog_email_editText)?.text.toString()
                val password = showingDialog.findViewById<EditText>(R.id.signUp_dialog_password_editText)?.text.toString()

                var allRight = true    // 입력한 이메일, 비밀번호 검사 결과
                if(email.isEmpty()) {
                    Toast.makeText(this, "이메일을 입력해야 합니다.", Toast.LENGTH_SHORT).show()

                    allRight = false
                }

                // 이메일 특수문자 검사
                if(!email.isEmpty()) {
                    // 이메일 형식에 맞지 않는 특수문자 검사
                    val prohibitionList = listOf<String>(
                        "`", "~", "!", "#", "$", "%", "^", "&", "*", "(",
                        ")", "-", "_", "=", "+", "[", "{", "]", "}", ";",
                        ":", ",", "<", ">", "/", "?", "|",
                        "\'", "\"", "\\"
                    )
                    for(item in prohibitionList) {
                        if(email.indexOf(item) != -1) { allRight = false }
                    }

                    // @, . 검사
                    if(email.count { it == '@' } < 1 || 1 < email.count { it == '@' }) {
                        allRight = false
                    } else if(email.count { it == '.' } < 1 || 1 < email.count { it == '.' }) {
                        allRight = false
                    }
                    if(email.indexOf('@') > email.indexOf('.')) { allRight = false }
                    if(email.indexOf('@') == 0 || email.indexOf('@') - email.indexOf('.') == -1 || email.indexOf('.') == email.lastIndex) { allRight = false }

                    if(!allRight) { Toast.makeText(this, "옳바른 이메일 형식으로 입력해 주세요.", Toast.LENGTH_SHORT).show() }
                }

                // 이미 회원가입 되어있는 이메일인지 검사
                if(!email.isEmpty()) {
                    for(item in emailArrayList) {
                        if(email == item) { allRight = false }
                    }

                    if(!allRight) { Toast.makeText(this, "이미 가입 되어있는 이메일입니다.", Toast.LENGTH_SHORT).show() }
                }

                // 비밀번호 검사
                if(password.isEmpty()) {
                    Toast.makeText(this, "비밀번호를 입력해야 합니다.", Toast.LENGTH_SHORT).show()

                    allRight = false
                }

                // 파이어베이스 비밀번호 조건에 부합되는지 검사 (6자리 이상)
                else if(password.length < 6) {
                    Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()

                    allRight = false
                }

                // 회원가입 시도
                if(allRight) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if(task.isSuccessful) {
                                // 파이어베이스 작업
                                emailListRef.push().setValue(email)
                                countRef.child(auth.currentUser!!.uid).child("MemoCount").setValue(0)
                                countRef.child(auth.currentUser!!.uid).child("D-DayCount").setValue(0)

                                // 액티비티 작업
                                showingDialog.dismiss()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                }
            }
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