package com.example.appqlchitieu.utils

/**
 * Wrapper nhỏ để lấy userId theo kiểu "đã đăng nhập thì trả id,
 * chưa đăng nhập thì báo rõ".
 *
 * Không bắt buộc dùng. Nếu muốn gọn code ở nhiều màn thì dùng.
 */
class UserSession(private val session: SessionManager) {

    fun requireUserId(): Int {
        val id = session.getUserId()
        check(id != -1) { "Chưa đăng nhập" }
        return id
    }

    fun userIdOrNull(): Int? {
        val id = session.getUserId()
        return if (id == -1) null else id
    }
}
