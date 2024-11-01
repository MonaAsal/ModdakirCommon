package com.moddakir.moddakir.ui.bases.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.moddakirapps.R
import com.example.moddakirapps.databinding.ActivityLoginMaqratackBinding
import com.example.moddakirapps.databinding.ActivityLoginStudentBinding
import com.example.moddakirapps.databinding.ActivityLoginTeacherBinding
import com.google.gson.Gson
import com.moddakir.moddakir.App
import com.moddakir.moddakir.App.Companion.ApplicationVersion
import com.moddakir.moddakir.App.Companion.ColorPrimary
import com.moddakir.moddakir.App.Companion.SecondColor
import com.moddakir.moddakir.App.Companion.WhatsAppNum
import com.moddakir.moddakir.helper.LocaleHelper
import com.moddakir.moddakir.helper.SavedFingerAccountsPreferences
import com.moddakir.moddakir.helper.SharedPrefHelper.Companion.setIntoSharedPref
import com.moddakir.moddakir.network.Resource
import com.moddakir.moddakir.network.model.User
import com.moddakir.moddakir.network.model.response.ModdakirResponse
import com.moddakir.moddakir.network.model.response.ResponseModel
import com.moddakir.moddakir.ui.bases.HomeActivity
import com.moddakir.moddakir.ui.bases.holyQuran.QuranInstance.loginQuran
import com.moddakir.moddakir.ui.widget.ButtonCalibriBold
import com.moddakir.moddakir.utils.AccountPreference
import com.moddakir.moddakir.utils.FingerBiometricAuthenticator
import com.moddakir.moddakir.utils.Language
import com.moddakir.moddakir.utils.LanguageOptionFragment
import com.moddakir.moddakir.utils.getLanguageCode
import com.moddakir.moddakir.utils.observe
import com.moddakir.moddakir.viewModel.AutViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : SocialMedialActivity(), OnTouchListener {
    override var layoutId: Int = R.layout.activity_login_student
    private lateinit var bindingStudent: ActivityLoginStudentBinding
    private lateinit var bindingTeacher: ActivityLoginTeacherBinding
    private lateinit var bindingWhiteLabel: ActivityLoginMaqratackBinding
    private var fingerBiometricAuthenticator: FingerBiometricAuthenticator? = null
    private var passwordHideShow = false
    private var deviceLanguageOrSaved: String = ""
    private val authViewModel: AutViewModel by viewModels()

    companion object{
        lateinit var username: String
        lateinit var password: String
        lateinit var lang: String
    }
    override fun initializeViewModel() {}
    override fun observeViewModel() {
        observe(authViewModel.loginLiveData, ::handleLoginResponse)
        observe(authViewModel.loginTeacherLiveData, ::handleLoginTeacherResponse)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bindingScreenDesign()
        lang = LocaleHelper.getLocale(this).toString()
        when (ApplicationVersion) {
            App.AppVersion.Version1.toString() -> {
                initFingerBiometricAuthenticator()
                fingerPrintDesign()
                bindingStudent.registerTxt.setOnClickListener { navigateToRegisterScreen() }
                bindingStudent.mobileLogin.setOnClickListener { navigateToLoginMobileScreen("Create") }
                bindingStudent.finger.setOnClickListener { fingerBiometricAuthenticator?.authenticate() }
                bindingStudent.languageTv.setOnClickListener { changeLang() }
                bindingStudent.whatsappCotactTv.setOnClickListener { openWhatsApp("https://wa.me/966538713107") }
                bindingStudent.tvForgetPassword.setOnClickListener { navigateToForgetPasswordScreen() }
                bindingStudent.btnLogin.setOnClickListener {
                    loginStudent()
                    username = bindingStudent.etEmail.text.toString()
                    password = bindingStudent.etPassword.text.toString()
                }

            }

            App.AppVersion.Version2.toString() -> {
                showStudentAppDialog()
                bindingTeacher.btnLogin.setOnClickListener {
                    loginTeacher()
                    username = bindingTeacher.etEmail.text.toString()
                    password = bindingTeacher.etPassword.text.toString()
                }
                bindingTeacher.tvForgetPassword.setOnClickListener { navigateToForgetPasswordScreen() }

            }

            App.AppVersion.Version3.toString() -> {

                bindingWhiteLabel.btnLogin.setOnClickListener {
                    loginWl()
                    username = bindingWhiteLabel.etEmail.text.toString()
                    password = bindingWhiteLabel.etPassword.text.toString()
                }
                bindingWhiteLabel.btnJoinUs.setOnClickListener { v ->
                    val dependentManagersDialogFragment =
                        DependentManagersDialogFragment(ProgramType.Maqraatec.value)
                    dependentManagersDialogFragment.show(
                        supportFragmentManager,
                        dependentManagersDialogFragment.getTag()
                    )
                }
                bindingWhiteLabel.languageTv.setOnClickListener { changeLangWL() }
                bindingWhiteLabel.tvForgetPassword.setOnClickListener { navigateToForgetPasswordScreen() }
                bindingWhiteLabel.whatsappCotactTv.setOnClickListener { openWhatsApp(WhatsAppNum) }
            }
        }
    }

    private fun loginWl() {
        authViewModel.login(
            bindingWhiteLabel.etEmail.getText().toString().trim(),
            bindingWhiteLabel.etPassword.getText().toString().trim(),
            LocaleHelper.getLocale(this@LoginActivity).toString()
        )
    }

    private fun setAppColor() {
        val listTextViewPrimaryColors =
            listOf(bindingWhiteLabel.tvForgetPassword, bindingWhiteLabel.loginTV)
        val listButtonPrimaryColors: List<ButtonCalibriBold> = listOf(
            bindingWhiteLabel.btnLogin,
            bindingWhiteLabel.btnJoinUs,
            bindingWhiteLabel.finger
        )
        val listButtonSecondColor: List<ButtonCalibriBold> =
            listOf(bindingWhiteLabel.btnJoinTheGeneralProgram)
        setButtonsColor(listButtonPrimaryColors, ColorPrimary)
        setButtonsColor(listButtonSecondColor, SecondColor)
        setPrimaryColor(listTextViewPrimaryColors, ColorPrimary)
    }

    private fun changeLangWL() {
        var language = Language.english
        if (LocaleHelper.getLocale(this)?.equals("en") == true)
            language = Language.arabic
        setChangeLang(language)
    }

    private fun loginTeacher() {
        authViewModel.loginTeacher(
            bindingStudent.etEmail.getText().toString().trim(),
            bindingStudent.etPassword.getText().toString().trim(),
            LocaleHelper.getLocale(this@LoginActivity).toString()
        )
    }

    private fun loginStudent() {
        authViewModel.login(
            bindingStudent.etEmail.getText().toString().trim(),
            bindingStudent.etPassword.getText().toString().trim(),
            LocaleHelper.getLocale(this@LoginActivity).toString()
        )
    }

    private fun openWhatsApp(uri: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(browserIntent)
    }

    private fun fingerPrintDesign() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val savedFingerAccount = SavedFingerAccountsPreferences()
                val accounts: ArrayList<com.moddakir.moddakir.network.model.SavedFingerAccount?>? =
                    savedFingerAccount.getSavedAccounts()
                if (accounts.isNullOrEmpty() || !fingerBiometricAuthenticator!!.canAuthenticate()) {
                    bindingStudent.finger.visibility = View.GONE
                }
            } catch (e: Exception) {
                Timber.e("FingerAuth exce" + e.message)
                e.printStackTrace()
                bindingStudent.finger.visibility = View.GONE
            }
        } else {
            bindingStudent.finger.visibility = View.GONE
        }
    }

    private fun bindingScreenDesign() {
        when (ApplicationVersion) {
            App.AppVersion.Version1.toString() -> {
                layoutId = R.layout.activity_login_student
                bindingStudent = ActivityLoginStudentBinding.inflate(layoutInflater)
                App.bindingStudent=bindingStudent
                setContentView(bindingStudent.root)
            }

            App.AppVersion.Version2.toString() -> {
                layoutId = R.layout.activity_login_teacher
                bindingTeacher = ActivityLoginTeacherBinding.inflate(layoutInflater)
                setContentView(bindingTeacher.root)
            }

            App.AppVersion.Version3.toString() -> {
                layoutId = R.layout.activity_login_rattel
                bindingWhiteLabel = ActivityLoginMaqratackBinding.inflate(layoutInflater)
                setContentView(bindingWhiteLabel.root)
                setAppColor()
                showAppButtons()
            }
        }
    }

    private fun showAppButtons() {
        if (intent.getBooleanExtra(LoginScreenEntities.joinUsPrograms.toString(), false)) {
            bindingWhiteLabel.btnJoinUs.visibility = View.VISIBLE
        }
        if (intent.getBooleanExtra(LoginScreenEntities.joinGeneralProgram.toString(), false)) {
            bindingWhiteLabel.btnJoinTheGeneralProgram.visibility = View.VISIBLE
        }
        if (intent.getBooleanExtra(
                LoginScreenEntities.educationMinistryProgram.toString(),
                false
            )
        ) {
            bindingWhiteLabel.btnEducationMinistryProgram.visibility = View.VISIBLE
        }
        if (intent.getBooleanExtra(LoginScreenEntities.appLogo.toString(), false)) {
            bindingWhiteLabel.topLogoIV.visibility = View.VISIBLE
        }
    }

    private fun changeLang() {
        val languageOptionFragment = LanguageOptionFragment(
            { it ->
                when (it) {
                    Language.english -> {
                        setChangeLang(Language.english)
                    }

                    Language.french -> {
                        setChangeLang(Language.french)
                    }

                    Language.urdu -> {
                        setChangeLang(Language.urdu)
                    }

                    Language.indonesia -> {
                        setChangeLang(Language.indonesia)
                    }

                    Language.arabic -> setChangeLang(Language.arabic)
                    else -> {
                        setChangeLang(Language.empty)
                    }
                }
            },
            false
        )
        languageOptionFragment.show(supportFragmentManager, languageOptionFragment.tag)
    }

    private fun setChangeLang(language: Language) {
        LocaleHelper.setLocale(this, getLanguageCode(language))
       // navigateToLoginIntroScreen()
    }

    private fun initFingerBiometricAuthenticator() {
        fingerBiometricAuthenticator = object : FingerBiometricAuthenticator() {
            override fun moveToNext() {
                //  this@LoginActivity.moveToNext()
            }

            override fun getContext(): Context {
                return this@LoginActivity
            }

            override fun getHostActivity(): FragmentActivity {
                return this@LoginActivity
            }

            override fun setAccountData(email: String?, password: String?) {
                bindingStudent.etEmail.setText(email)
                bindingStudent.etPassword.setText(password)
                bindingStudent.btnLogin.performClick()
            }
        }
        (fingerBiometricAuthenticator as FingerBiometricAuthenticator).initFingerAuth()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        passwordHideShow = !passwordHideShow
        when (event.action) {
            MotionEvent.ACTION_UP -> bindingStudent.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            MotionEvent.ACTION_DOWN -> bindingStudent.etPassword.inputType =
                (InputType.TYPE_CLASS_TEXT)
        }
        passwordChange()
        return false
    }

    private fun passwordChange() {
        if (passwordHideShow) {
            bindingStudent.showPassIcon.setImageResource(R.drawable.ic_password_show)
        } else {
            bindingStudent.showPassIcon.setImageResource(R.drawable.ic_password_hide)
        }
    }

    private fun showStudentAppDialog() {
        val messageDialog = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
        messageDialog.setTitleText(resources.getString(R.string.teacher_app_login_message))
        messageDialog.setCancelable(true)
        messageDialog.setConfirmText(resources.getString(R.string.download_Student_app))
        messageDialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog? ->
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.moddakir.moddakir")
                )
            )
            messageDialog.dismissWithAnimation()
        }
        messageDialog.setCancelClickListener { sweetAlertDialog: SweetAlertDialog? -> messageDialog.dismissWithAnimation() }
        messageDialog.setCancelText(resources.getString(R.string.continue_))
        messageDialog.show()
    }

    private fun handleLoginResponse(loginResponse: Resource<ModdakirResponse<ResponseModel>>) {
        when (loginResponse) {
            is Resource.Loading -> {
                bindingStudent.btnLogin.isEnabled = false
            }

            is Resource.Success -> loginResponse.data?.let {
                val user: User = loginResponse.data.data!!.student
                deviceLanguageOrSaved =  LocaleHelper.getLocale(this).toString()
                authViewModel.handleLoggedUser(loginResponse.data.data)
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("isNew", loginResponse.data.data.isNewUser)
                intent.putExtra("freeMin", loginResponse.data.data.freeMinutes)
                intent.putExtra("isChild", user.isChildDependent)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                loginQuran()
            }

            is Resource.NetworkError -> {
                loginResponse.errorCode?.let {
                }
            }

            is Resource.DataError -> {
                loginResponse.errorResponse?.let { showServerErrorMessage(loginResponse.errorResponse) }
            }
        }
    }

    private fun handleLoginTeacherResponse(loginResponse: Resource<ModdakirResponse<ResponseModel>>) {
        when (loginResponse) {
            is Resource.Loading -> {
                bindingStudent.btnLogin.isEnabled = false
            }
            is Resource.Success -> loginResponse.data?.let {
                Toast.makeText(
                    this@LoginActivity,
                    loginResponse.data.message,
                    Toast.LENGTH_LONG
                ).show()

                if (loginResponse.data.data!!.teacher != null) {
                    val userstr = Gson().toJson(loginResponse.data.data.teacher)
                    setIntoSharedPref(this,"user",userstr)

                    if (loginResponse.data.data.teacher.briefTrans != null && loginResponse.data.data.teacher.briefTrans!!.ar!= null && loginResponse.data.data.teacher.briefTrans!!.ar!!.isNotEmpty())
                        setIntoSharedPref(this,"getBriefTransAr",loginResponse.data.data.teacher.briefTrans!!.ar!!)

                    if (loginResponse.data.data.teacher.briefTrans != null && loginResponse.data.data.teacher.briefTrans!!.en!= null && loginResponse.data.data.teacher.briefTrans!!.en!!.isNotEmpty())
                        setIntoSharedPref(this,"getBriefTransEn",loginResponse.data.data.teacher.briefTrans!!.en!!)

                    AccountPreference.setAccessToken(loginResponse.data.data.accessToken)

                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                }

            }

            is Resource.NetworkError -> {
                loginResponse.errorCode?.let {
                }
            }

            is Resource.DataError -> {
                loginResponse.errorResponse?.let { showServerErrorMessage(loginResponse.errorResponse) }
            }
        }
    }

    enum class ProgramType(val value: String) {
        Maqraatec("Maqraatec"),
        Ministry("Ministry"),
        General("General");
    }
}