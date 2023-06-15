import {createApp} from 'vue';
import {createPinia} from 'pinia';

import App from './App.vue';
import ElementPlus from 'element-plus';
import router from '@/router/index';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import axios from 'axios';
import VueAxios from 'vue-axios';
import _ from 'lodash';
import dayjs from 'dayjs';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import 'element-plus/theme-chalk/dark/css-vars.css';
import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css';
import gsap from 'gsap';
import {ScrollTrigger} from 'gsap/ScrollTrigger';
import {ScrollToPlugin} from 'gsap/ScrollToPlugin';
import './assets/styles/dark/css-vars.css'; /*引入自定义css变量文件*/
import VueDOMPurifyHTML from 'vue-dompurify-html';
import moment from "moment";
import 'moment/dist/locale/zh-cn';
import UndrawUi from 'undraw-ui';
import 'undraw-ui/dist/style.css';
import directive from './directive';
import { library } from '@fortawesome/fontawesome-svg-core';
import {  } from '@fortawesome/free-brands-svg-icons';
import { faLightbulb } from '@fortawesome/free-regular-svg-icons';
import { faCar, faMotorcycle, faBicycle, faTractor, faGavel, faStreetView, faSignHanging, faRoadCircleExclamation,
    faPersonWalkingDashedLineArrowRight, faCarBurst, faPersonBiking, faVanShuttle, faPersonFallingBurst, faShieldHalved, faScaleBalanced,
    faHardDrive, faBacon, faRoad, faMugHot } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
/*import { faVuejs, faJava, faReact, faCss3Alt, faSquareJs, faSlideshare, faConnectdevelop } from '@fortawesome/free-brands-svg-icons';
import { faFeather, faClipboardQuestion, faFlag, faBug, faEarthAsia, faGamepad, faFilm, faAddressCard, faCodeCompare,
    faVideo, faBriefcase, faAward, faSquareShareNodes, faBuildingUser, faFolderTree, faListCheck } from '@fortawesome/free-solid-svg-icons';*/

const app = createApp(App);
const pinia = createPinia();

gsap.registerPlugin(ScrollTrigger, ScrollToPlugin);

/*library.add(faVuejs, faJava, faReact, faCss3Alt, faSquareJs, faFeather, faSlideshare, faClipboardQuestion,
    faFlag, faBug, faEarthAsia, faGamepad, faFilm, faVideo, faBriefcase, faAward, faSquareShareNodes,
    faBuildingUser, faAddressCard, faCodeCompare, faConnectdevelop, faFolderTree, faListCheck)*/
library.add(faCar, faMotorcycle, faBicycle, faTractor, faLightbulb, faGavel, faStreetView, faSignHanging, faRoadCircleExclamation,
    faPersonWalkingDashedLineArrowRight, faCarBurst, faPersonBiking, faVanShuttle, faPersonFallingBurst, faShieldHalved, faScaleBalanced,
    faHardDrive, faBacon, faRoad, faMugHot)
app.component('font-awesome-icon', FontAwesomeIcon)

/* 全局导入Element图标 */
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
}

pinia.use(piniaPluginPersistedstate);

moment.locale('zh-CN');

/* 挂载自定义指令 */
directive(app);

app.use(router);
app.use(pinia);
app.use(VueAxios, axios);
app.use(ElementPlus);
app.use(VueDOMPurifyHTML);
app.use(VueCropper);
app.use(_);
app.use(UndrawUi)
app.use(dayjs);
app.use(moment);

app.mount('#app');
