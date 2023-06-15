import hasRole from './authority/hasRole';
import hasAuthority from './authority/hasAuthority';

/**
 * 自定义指定：https://cn.vuejs.org/guide/reusability/custom-directives.html
 * @param app createApp
 */
export default function directive(app) {
    app.directive('hasRole', hasRole); // v-hasRole
    app.directive('hasAuthority', hasAuthority); // v-hasAuthority
}