import { renderMainScreen } from './main.js';
import { renderWorldcupScreen } from './worldcup.js';
import { renderResultScreen } from './result.js';
import {renderCreateScreen, renderRankScreen, renderAdminMainScreen, renderEditScreen} from './admin.js';
import { currentUser } from './login.js'; // 💡 이 줄을 추가해야 합니다!

// **login.js**는 index.html에서 초기 로드되므로 별도로 import하지 않습니다.
// 주석과는 별개로, 모듈 시스템에서는 변수를 사용하려면 반드시 import 해야 합니다.

const app = document.getElementById('app');
const loginScreen = document.getElementById('loginScreen');

/**
 * URL 해시(Hash)에 따라 적절한 화면을 렌더링합니다.
 */
export function router() {
    // URL 해시 값 가져오기
    const hash = window.location.hash;

    // 각 경로에 따라 화면 렌더링 함수 호출
    if (!currentUser) { // 이제 currentUser가 정의되어 참조 오류가 발생하지 않습니다.
        // 로그인 정보가 없으면 로그인 화면으로 고정
        document.getElementById('loginScreen').style.display = 'block';
        document.getElementById('app').style.display = 'none';
        return;
    } else {
        document.getElementById('loginScreen').style.display = 'none';
        document.getElementById('app').style.display = 'block';
    }

    // 💡 3. admin 라우팅 규칙 업데이트
    if (hash.startsWith('#worldcup/')) {
        const id = hash.split('/')[1];
        renderWorldcupScreen(id);
    } else if (hash.startsWith('#result/')) {
        const parts = hash.split('/');
        renderResultScreen(parts[1], parts[2]); // worldcupId, winnerId
    } else if (hash.startsWith('#admin/create')) {
        renderCreateScreen();
    }else if (hash.startsWith('#admin/edit/')) { // 💡 수정 경로 추가
        const id = hash.split('/')[2];
        renderEditScreen(id);
    } else if (hash.startsWith('#admin/rank/')) {
        const id = hash.split('/')[2]; // #admin/rank/{id}
        renderRankScreen(id);
    } else if (hash === '#admin') { // 💡 핵심: #admin 진입 시 목록 화면 호출
        renderAdminMainScreen();
    } else {
        // 기본 화면: #main 또는 해시가 없을 경우
        renderMainScreen();
    }
}

// 페이지 로드 시 및 해시 변경 시 라우터 실행
window.addEventListener('load', router);
window.addEventListener('hashchange', router);