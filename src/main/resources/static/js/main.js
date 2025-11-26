import { fetchWorldcupList } from './api.js';
import { currentUser } from './login.js';

const app = document.getElementById('app');

/**
 * 월드컵 목록 화면을 렌더링하고 이벤트 리스너를 설정합니다.
 */
export async function renderMainScreen() {
    if (!currentUser || !currentUser.nickname) {
        // 로그인 정보가 없으면, 로그인 화면으로 리디렉션하거나 로그인 화면을 다시 표시합니다.
        console.error("사용자 정보 없음. 로그인 필요.");
        window.location.hash = '';
        return;
    }

    // 닉네임 표시 업데이트 (login.js에서 이미 했지만, 재확보)
    const userInfoDiv = document.getElementById('userInfo');
    userInfoDiv.innerHTML = `환영합니다, <strong>${currentUser.nickname}</strong>님!`;

    // 관리자 구분을 위해 닉네임에 클래스 추가
    if (currentUser.nickname === 'admin') {
        userInfoDiv.querySelector('strong').classList.add('admin-nickname');
    }

    const worldcups = await fetchWorldcupList();

    app.innerHTML = ''; // 기존 콘텐츠 지우기

    let listHtml = '<h2>💖 이상형 월드컵을 선택하세요 💖</h2>';

    // 관리자 메뉴 버튼 추가
    if (currentUser.nickname === 'admin') {
        listHtml = `
            <div style="text-align:center; margin-bottom: 20px;">
                <h2>관리자님, 환영합니다!</h2>
                <p style="margin-bottom: 20px;">월드컵을 관리하시려면 관리자 메뉴로 이동하십시오.</p>
                <button class="admin-button" onclick="window.location.hash = '#admin'">월드컵 관리 메뉴로 이동 🛠️</button>
            </div>
            <hr>
            ${listHtml} `;
    }

    if (worldcups && worldcups.length > 0) {
        listHtml += '<div id="worldcupList">';
        worldcups.forEach(wc => {
            listHtml += `
        <div class="worldcup-card" data-id="${wc.id}">
            <img src="${wc.thumbnailUrl}" alt="${wc.title} 썸네일">
            <h3>${wc.title}</h3>
            <button class="start-button" data-worldcup-id="${wc.id}">월드컵 시작!</button>
        </div>
    `;
        });
        listHtml += '</div>';
    } else {
        listHtml += '<p style="text-align:center;">아직 개설된 월드컵이 없습니다.</p>';
    }

    app.innerHTML += listHtml;

    // 이벤트 리스너 설정: 월드컵 시작 버튼 클릭 시
    document.querySelectorAll('.start-button').forEach(button => {
        button.addEventListener('click', (e) => {
            const worldcupId = e.target.dataset.worldcupId;
            // 해시 변경을 통해 worldcup.js 라우팅 트리거
            window.location.hash = `#worldcup/${worldcupId}`;
        });
    });
}