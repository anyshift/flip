import request from "@/utils/request";

/*注册方法*/
export function register(userInfo) {
    return request({
        url: '/register?eCode=' + userInfo.emailVerificationCode + "&eOwner=" + userInfo.emailCodeOwner + "&code=" + userInfo.captcha + "&codeOwner=" + userInfo.captchaOwner,
        method: 'post',
        data: {
            username: userInfo.username.trim(),
            password: userInfo.password,
            email: userInfo.email
        }
    })
}

/*登录方法*/
export function login(username, password, captcha, captchaOwner) {
    const data = {
        username: username,
        password: password
    }
    return request({
        url: '/login?code=' + captcha + "&codeOwner=" + captchaOwner,
        method: 'post',
        data: data
    })
}

export function refreshTokenAPI(refreshToken) {
    const data = {
        refreshToken: refreshToken,
    }
    return request({
        url: '/refresh',
        data: data,
        method: 'post',
        headers: {
            requireToken: true,
            'Content-Type': 'application/x-www-form-urlencoded'
        },
    })
}

/*退出登录方法*/
export function logout() {
    return request({
        url: '/logout',
        headers: {
            requireToken: true
        },
        method: 'post'
    })
}

export function getEmailCodeAPI(uid) {
    return request({
        url: '/activate',
        method: 'get',
        params: {
            uid,
        },
        headers: {
            requireToken: true
        },
    })
}

export function activateEmailAPI(uid, emailCode) {
    return request({
        url: '/activate',
        method: 'put',
        data: {
            uid,
        },
        params: {
            emailCode,
        },
        headers: {
            requireToken: true
        },
    })
}

/*获取图形验证码*/
export function getCaptchaImage() {
    return request({
        url: '/_captcha',
        method: 'get',
    })
}

/*校验图形验证码*/
export function checkCaptcha(code, codeOwner) {
    return request({
        url: '/_checkCaptcha',
        params: {
            code: code,
            codeOwner: codeOwner
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        method: 'get'
    })
}

/*校验用户名唯一性*/
export function checkUsernameUnique(username) {
    return request({
        url: '/_checkUsernameUnique',
        params: {
            username: username
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        method: 'get'
    })
}

