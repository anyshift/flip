import axios from "axios";
import errorMsg from "./errorMsg";
import {ElMessage} from "element-plus";
import {getToken, removeToken, setToken, tokenExists, getRefreshToken, setRefreshToken} from "./token";
import userStore from "../stores/userStore";
import {refreshTokenAPI} from '../api/loginAPI';
import router from "../router";

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8';
axios.defaults.withCredentials = false;

const instance = axios.create({
    baseURL: import.meta.env.VITE_AXIOS_BASEURL,
    timeout: import.meta.env.VITE_AXIOS_TIMEOUT,
})

//请求拦截器
instance.interceptors.request.use(config => {

        /*是否需要携带Token*/
        const requireToken = config.headers.requireToken === "true";

        if (requireToken && tokenExists()) {
            if (config.url === '/refresh') {
                config.headers.Authorization = "Bearer " + getRefreshToken();
            } else {
                config.headers.Authorization = "Bearer " + getToken();
            }
        }

        return config;
    },
    error => {
        console.log(error);
        return Promise.reject(error);
    }
)

instance.interceptors.response.use(response => {

        /*未设置状态码则默认成功状态*/
        const code = response.data.code || 200;

        /*二进制数据直接返回*/
        if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
            return response.data;
        }

        // 如果任意一个响应的响应头中包含Authorization字段，则取出该字段的值（该字段用于存放刷新后的访问凭证）
        if (response.headers['authorization']) {
            setToken(response.headers['authorization']);
            userStore().token = response.headers['authorization'];
        }

        if (code === 401) {
            let config = response.config;
            let refreshToken = getRefreshToken();
            let isRefreshing = false;
            let promiseArray = [];

            const retryOriginalRequest = new Promise(resolve => {
                promiseArray.push(() => {
                    resolve(instance.request(config))
                })
            })

            if (!isRefreshing) {
                isRefreshing = true;
                refreshTokenAPI(refreshToken).then(resp => {
                    setToken(resp.data.token);
                    if (resp.data.refreshToken) {
                        setRefreshToken(resp.data.refreshToken)
                    }
                    promiseArray.forEach(callback => {
                        callback(config)
                    });
                    promiseArray = [];
                }).catch(err => {
                    console.log(err)
                    if (err.code === 419 || err.code === 412) {router.push({path: '/login'}).then(r => {}).catch(e => {})}
                }).finally(() => {
                    isRefreshing = false;
                })
            }
            return retryOriginalRequest;
        } else if (code === 419) {
            userStore().$reset();
            removeToken();
            ElMessage({
                showClose: true,
                message: errorMsg["409"],
                type: 'error',
                duration: 2500
            })
            return Promise.reject(response.data);
        } else if (code === 403) {
            ElMessage({
                showClose: true,
                message: errorMsg["403"],
                type: 'error',
                duration: 2500
            })
            return Promise.reject(errorMsg["403"]);
        } else if (code === 412) {
            userStore().$reset();
            removeToken();
            ElMessage({
                showClose: true,
                message: errorMsg["412"],
                type: 'error',
                duration: 2500
            })
            return Promise.reject(response.data);
        } else if (code !== 200) {
            return Promise.reject(response.data)
        } else {
            return Promise.resolve(response.data)
        }
    },
    error => {
        if (error.response?.status === 403) {
            ElMessage({
                showClose: true,
                message: '权限不足',
                type: 'error',
                duration: 2500
            })
            return Promise.reject('权限不足');
        }
        let {message} = error
        if (message === "Network Error") {
            error.message = "远程服务器状态异常"
            return Promise.reject(error)
        } else if (message.includes("timeout")) {
            error.message = "接口请求超时"
            console.log("接口 \"" + error.config.url + "\" 请求超时")
            return Promise.reject(error)
        } else if (message.includes("Request failed with status code")) {
            error.message = "接口异常";
            console.log("接口 \"" + error.config.url + "\" " + message.substring(32, 35) + " 异常")
            return Promise.reject(error)
        }
        return Promise.reject(error)
    }
)

export default instance;