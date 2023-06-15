import {defineStore} from 'pinia'
import {removeToken, setRefreshToken, setToken} from "@/utils/token";
import {login, logout} from "../api/loginAPI";
import {getProfileAPI} from "../api/userAPI";

const useUserStore = defineStore("user", {
    state: () => ({
        token: '',
        refreshToken: '',
        id: '',
        uid: '',
        username: '',
        nickname: '',
        email: '',
        emailVerified: Boolean,
        avatar: '',
        createTime: '',
        registerIp: '',
        role: [],
        authorities: [],
        isLogin: false,
    }),
    actions: {
        /*用户登录*/
        login(userInfo) {
            const username = userInfo.username.trim();
            const password = userInfo.password;
            const captcha = userInfo.captcha;
            const captchaOwner = userInfo.captchaOwner;
            return new Promise((resolve, reject) => {
                login(username, password, captcha, captchaOwner).then(response => {
                    setToken(response.data.token);
                    setRefreshToken(response.data.refreshToken)
                    this.token = response.data.token;
                    this.refreshToken = response.data.refreshToken;
                    resolve(response);
                }).catch(error => {
                    reject(error);
                })
            })
        },
        /*获取用户信息*/
        getProfile() {
            return new Promise((resolve, reject) => {
                getProfileAPI().then(response => {
                    const user = response.data;
                    this.$patch({
                        isLogin: true,
                        id: user.id,
                        uid: user.uid,
                        username: user.username,
                        nickname: user.nickname,
                        email: user.email,
                        emailVerified: user.emailVerified,
                        avatar: user.avatar,
                        createTime: user.createTime,
                        registerIp: user.registerIp,
                    })

                    if (user.role) {
                        this.role.push(user.role);
                    } else {
                        this.role = ['ROLE_USER']
                    }
                    if (user.authorities && user.authorities.length > 0) {
                        this.authorities = user.authorities;
                    } else {
                        this.authorities = null;
                    }
                    resolve(response);
                }).catch(error => {
                    reject(error);
                })
            })
        },
        /*退出登录*/
        logout() {
            return new Promise((resolve, reject) => {
                logout().then(response => {
                    this.$reset(); // 批量重置state
                    removeToken();
                    resolve(response);
                }).catch(error => {
                    this.$reset(); // 批量重置state
                    removeToken();
                    reject(error)
                })
            })
        },
        getLoggedUserInfo() {
            return {
                id: this.id,
                uid: this.uid,
                username: this.username,
                nickname: this.nickname,
                email: this.email,
                emailVerified: this.emailVerified,
                avatar: this.avatar,
                createTime: this.createTime,
                registerIp: this.registerIp,
                role: this.role,
            };
        }
    },
})

export default useUserStore