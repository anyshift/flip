const tokenKey = 'token';
const refreshTokenKey = 'refreshToken';

export function tokenExists() {
    if (getToken() === null && getRefreshToken() !== null) {
        removeToken();
        return false;
    } else if (getToken() !== null && getRefreshToken() === null) {
        removeToken();
        return false;
    } else return !(getToken() === null && getRefreshToken() === null);
}

export function getToken() {
    return localStorage.getItem(tokenKey)
}
export function getRefreshToken() {
    return localStorage.getItem(refreshTokenKey)
}

export function setToken(token) {
    localStorage.setItem(tokenKey, token)
}
export function setRefreshToken(refreshToken) {
    localStorage.setItem(refreshTokenKey, refreshToken)
}

export function removeToken() {
    localStorage.removeItem(tokenKey);
    localStorage.removeItem(refreshTokenKey);
}