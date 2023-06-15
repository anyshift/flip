import useUserStore from "../stores/userStore";

export function hasRole(roleValue) {
    if (roleValue && roleValue instanceof Array && roleValue.length > 0) {
        const roles = useUserStore().role;

        return roles.some(role => {
            return roleValue.includes(role)
        });
    } else return false;
}

export function hasAuthority(authorityValue) {
    if (authorityValue && authorityValue instanceof Array && authorityValue.length > 0) {
        const authorities = useUserStore().authorities;

        if (authorities === null) {
            return false;
        }

        return authorities.some(authority => {
            return authorityValue.includes(authority.authority);
        })
    } else return false;
}