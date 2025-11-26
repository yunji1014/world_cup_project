import { loginUser, fetchWorldcupList } from './api.js';
import { router } from './app.js';

// 현재 사용자 정보를 저장
let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const app = document.getElementById('app');

    // 세션 체크 함수 (실제로는 서버에서 받은 토큰을 확인해야 함)
    function checkSession() {
        const storedNickname = localStorage.getItem('nickname');
        const storedUserId = localStorage.getItem('userId');
        if (storedNickname && storedUserId) {
            currentUser = { nickname: storedNickname, userId: Number(storedUserId) };
        }
    }

    // 폼 제출 이벤트 핸들러
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const nicknameInput = document.getElementById('nickname');
        const passwordInput = document.getElementById('password');
        const nickname = nicknameInput.value.trim();
        const password = passwordInput.value;

        // 서버에 로그인 요청
        const response = await loginUser(nickname, password);

        if (response && response.nickname) {
            // 로그인 성공 시 닉네임을 로컬 저장소에 저장 (간단 세션 처리)
            localStorage.setItem('nickname', response.nickname);
            localStorage.setItem('userId', response.userId.toString()); // String으로 저장
            currentUser = { nickname: response.nickname, userId: Number(response.userId) };
            alert(`${nickname}님 환영합니다!`);

            //renderMainScreen(); // 메인 화면 렌더링
            router(); // 올바른 라우팅 해시 값 설정
        } else {
            // 로그인 실패 처리 (api.js에서 이미 alert 처리될 수 있음)
            nicknameInput.value = '';
        }
    });

    // 메인 화면 렌더링 함수
    async function renderMainScreen() {
        if (!currentUser) return; // 사용자가 없으면 실행 중단

        // 닉네임 표시 업데이트
        const userInfoDiv = document.getElementById('userInfo');
        userInfoDiv.innerHTML = `환영합니다, <strong>${currentUser.nickname}</strong>님!`;

        // 월드컵 목록 API 호출
        const worldcups = await fetchWorldcupList();

        app.innerHTML = ''; // 기존 콘텐츠 지우기

        if (worldcups && worldcups.length > 0) {
            let listHtml = '<h2>월드컵을 선택하세요</h2><div id="worldcupList">';
            worldcups.forEach(wc => {
                listHtml += `
                    <div class="worldcup-card" data-id="${wc.id}">
                        <img src="${wc.thumbnail}" alt="${wc.title} 썸네일">
                        <h3>${wc.title}</h3>
                        <button onclick="window.location.hash = '#worldcup/${wc.id}'">시작</button>
                    </div>
                `;
            });
            listHtml += '</div>';
            app.innerHTML = listHtml;
            // 여기서 각 카드에 클릭 이벤트 리스너를 추가하여 월드컵 페이지로 이동
        } else {
            app.innerHTML = '<p>아직 개설된 월드컵이 없습니다.</p>';
        }

        // 관리자용 버튼 (임시)
        if (currentUser.nickname === 'admin') { // 닉네임 'admin'으로 구분한다고 가정
            const adminButton = document.createElement('button');
            adminButton.textContent = '관리자 메뉴 (월드컵 생성/랭킹)';
            adminButton.classList.add('admin-button');
            adminButton.onclick = () => window.location.hash = '#admin';
            userInfoDiv.appendChild(adminButton);

            // 관리자 닉네임 색깔 변경 (CSS에서 처리하는 것이 좋음)
            // userInfoDiv.querySelector('strong').classList.add('admin-nickname');
        }
    }

    // 초기 세션 확인
    checkSession();
});

// 외부에서도 사용하기 위해 export
export { currentUser };