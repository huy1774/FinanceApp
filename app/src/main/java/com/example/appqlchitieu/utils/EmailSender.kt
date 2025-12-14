package com.example.appqlchitieu.utils
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    private const val EMAIL = "tienpham2kk4@gmail.com"
    private const val PASSWORD = "rrfhenfhjjhhxmlh"

    fun sendOtp(email: String, otp: String, callback: (Boolean) -> Unit) {

        Thread {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(EMAIL, PASSWORD)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    subject = "Mã xác thực OTP"
                    setText("Mã OTP của bạn là: $otp\nVui lòng không chia sẻ mã này cho bất kỳ ai.")
                }

                Transport.send(message)

                // Trả về main thread
                Handler(Looper.getMainLooper()).post {
                    callback(true)
                }

            } catch (e: Exception) {
                Log.e("EmailSender", "Lỗi gửi OTP", e)

                Handler(Looper.getMainLooper()).post {
                    callback(false)
                }
            }
        }.start()
    }
}
