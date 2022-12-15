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

        auth = Firebase.auth

        val database = Firebase.database
        val myRef = database.getReference("EmailList")
        val countRef = database.getReference("Count")    // 사용자의 메모, D-Day 사용 횟수

        // 로그인 버튼
        val loginButton = findViewById<Button>(R.id.auth_login_button)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.auth_email_editText).text.toString()
            val password = findViewById<EditText>(R.id.auth_password_editText).text.toString()

            if(email == "") {
                Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            } else if(password == "") {
                Toast.makeText(this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "로그인 할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // 회원가입 되어있는 이메일들 가져오기
        val emailArrayList = arrayListOf<String>()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(setItem in snapshot.children) {
                    emailArrayList.add(setItem.value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // 회원가입 클릭 (회원가입 다이얼로그 실행)
        val authSignUpTextView = findViewById<TextView>(R.id.auth_signUp_textView)
        authSignUpTextView.setOnClickListener {
            val signUpDialogView = LayoutInflater.from(this).inflate(R.layout.sign_up_dialog, null)
            val signUpDialog = AlertDialog.Builder(this).setView(signUpDialogView)
            val showingDialog = signUpDialog.show()

            // 이메일, 비밀번호 입력 후 확인 버튼 클릭
            val confirmButton = showingDialog.findViewById<Button>(R.id.signUp_dialog_confirm_button)
            confirmButton!!.setOnClickListener {
                val emailEditText = showingDialog.findViewById<EditText>(R.id.signUp_dialog_email_editText)
                val passwordEditText = showingDialog.findViewById<EditText>(R.id.signUp_dialog_password_editText)

                // 이메일과 비밀번호가 알맞게 입력 되었는지 알 수 있도록 해주는 변수
                var allRight = false

                // 이메일 검사
                // 이메일 부분이 비어있을 때
                if(emailEditText!!.text.toString() == "") {
                    Toast.makeText(this, "이메일을 입력해야 합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    allRight = true
                }

                // 이메일의 특수문자와 관련된 검사
                if(emailEditText!!.text.toString() != "") {
                    val emailValue = emailEditText!!.text.toString()
                    val prohibitionList = listOf<String>(
                        "`", "~", "!", "#", "$", "%", "^", "&", "*", "(",
                        ")", "-", "_", "=", "+", "[", "{", "]", "}", ";",
                        ":", ",", "<", ">", "/", "?", "|",
                        "\'", "\"", "\\"
                    )

                    // 이메일 형식에 맞지 않는 특수문자가 들어 있는지 검사
                    for(item in prohibitionList) {
                        if(emailValue.indexOf(item) != -1) {
                            allRight = false
                        }
                    }

                    // 특수문자 @와 .의 유무 및 여러번 쓰였는지 검사
                    if(emailValue.count { it == '@' } < 1 || 1 < emailValue.count { it == '@' }) {
                        allRight = false
                    } else if(emailValue.count { it == '.' } < 1 || 1 < emailValue.count { it == '.' }) {
                        allRight = false
                    }

                    if(allRight == false) {
                        Toast.makeText(this, "옳바른 이메일 형식으로 기입해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                }

                // 이메일 중복 여부 검사 (이미 회원가입 되어있는 이메일인지 검사)
                if(emailEditText!!.text.toString() != "") {
                    val emailValue = emailEditText!!.text.toString()

                    var check = false

                    for(email in emailArrayList) {
                        if(emailValue == email) {
                            check = true
                        }
                    }

                    if(check) {
                        allRight = false
                        Toast.makeText(this, "이미 가입 되어있는 이메일입니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                // 비밀번호 검사
                // 비밀번호 부분이 비어있을 때
                if(passwordEditText!!.text.toString() == "") {
                    allRight = false
                    Toast.makeText(this, "비밀번호를 입력해야 합니다.", Toast.LENGTH_SHORT).show()
                }
                // Firebase 비밀번호 조건에 부합되는지 검사 (6자리 이상)
                else if(passwordEditText!!.text.toString().length < 6) {
                    allRight = false
                    Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                }

                // 회원가입 성공
                if(allRight) {
                    auth.createUserWithEmailAndPassword(emailEditText!!.text.toString(), passwordEditText!!.text.toString())
                        .addOnCompleteListener(this) { task ->
                            if(task.isSuccessful) {
                                // Firebase Realtime Database에 이메일 추가, 사용 횟수 작업
                                myRef.push().setValue(emailEditText!!.text.toString())
                                countRef.child(auth.currentUser!!.uid).child("MemoCount").setValue(0)
                                countRef.child(auth.currentUser!!.uid).child("D-DayCount").setValue(0)

                                // 회원가입 다이얼로그 종료 및 자동 로그인 처리
                                showingDialog.dismiss()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                }
            }
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