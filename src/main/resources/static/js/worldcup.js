import { apiGet, apiPost } from './api.js';
import { currentUser } from './login.js';

const app = document.getElementById('app');



let candidates = []; // 월드컵 후보 전체 리스트
let roundCandidates = []; // 현재 라운드에 진출한 후보 리스트
let winners = []; // 현재 라운드 승자 리스트
let currentRound = 0;
let worldcupId = null;
let byeCandidate = null; //부전승 처리를 위해
let roundCounter = 0; //현재 진행중인 대결 횟수 기록

/**
 * 특정 월드컵을 초기화하고 첫 라운드를 시작합니다.
 * @param {string} id - 월드컵 ID
 */
export async function renderWorldcupScreen(id) {
    worldcupId = id;
    // 1. 월드컵 후보 데이터 로드 (API 명세 보강: GET /api/worldcup/{id})
    const data = await apiGet(`/worldcup/${worldcupId}`);
    if (!data || !data.candidates || data.candidates.length < 2) {
        app.innerHTML = '<p class="error">월드컵 데이터를 불러오거나 후보가 부족합니다.</p>';
        return;
    }
    // 초기화 및 라운드 설정
    candidates = data.candidates;
    roundCandidates = [...candidates];
    winners = [];

    // 후보 목록을 무작위로 섞습니다.
    shuffleArray(roundCandidates);

    // 💡 초기 부전승 결정 로직 제거 (전체 후보 수를 유지해야 합니다.)
    byeCandidate = null;
    roundCounter = 0;
    // 초기 라운드 강 수 설정 (11명이면 16강으로 시작)
    currentRound = findInitialRound(candidates.length);
    startNextRound();
}

// 💡 후보 수에 따른 초기 강 수(32강, 16강 등)를 계산하는 헬퍼 함수 추가 (worldcup.js 파일 안에)
function findInitialRound(count) {
    let round = 2;
    while (round < count) {
        round *= 2;
    }
    return round;
}

/**
 * 배열을 무작위로 섞습니다. (Fisher-Yates 알고리즘)
 */
function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
}

/**
 * 다음 라운드 또는 다음 대결을 시작합니다.
 */
function startNextRound() {
    // 1. 🏁 라운드 종료 처리 (기존과 동일)
    if (roundCandidates.length === 0) {

        // 1-1. 최종 우승자 결정
        if (winners.length === 1) {
            const finalWinner = winners[0];
            window.location.hash = `#result/${worldcupId}/${finalWinner.id}`;
            return;
        }

        // 1-2. 다음 라운드 준비
        roundCandidates = winners;
        winners = [];
        shuffleArray(roundCandidates);

        // 다음 라운드의 강 수 재설정
        currentRound = findInitialRound(roundCandidates.length);
        roundCounter = 0;
    }

    // 💡 [수정됨] 로직 순서 변경: 대결 가능한 후보가 2명 이상인지 먼저 확인
    // 2. ⚔️ 일반 대결 처리 (후보가 2명 이상 남아있다면 무조건 대결)
    if (roundCandidates.length >= 2) {
        const candidate1 = roundCandidates.pop();
        const candidate2 = roundCandidates.pop();

        roundCounter++; // 대결 카운트 증가
        renderMatch(candidate1, candidate2);
        return; // 대결을 렌더링했으면 함수 종료
    }

    // 💡 [수정됨] 2명씩 짝을 짓고 나서 딱 1명이 남았을 때 부전승 처리
    // 3. 🛡️ 부전승(Bye) 처리 (남은 후보가 1명일 때)
    if (roundCandidates.length === 1) {
        const byeCandidate = roundCandidates.pop();

        // 부전승 전용 렌더링 함수 호출
        renderByeMatch(byeCandidate);
        return;
    }
}

/**
 * 부전승 대결 화면을 렌더링합니다. (사용자가 부전승 후보를 클릭하도록 유도)
 */
function renderByeMatch(candidate) {
    const roundName = currentRound === 2 ? '결승' : `${currentRound}강`;

    app.innerHTML = `
        <div id="worldcupScreen">
            <h2 id="roundInfo">🔥 ${roundName}: 부전승 대결 🔥</h2>
            <div id="selectionArea">
                
                <div class="candidate-box bye-candidate" data-id="${candidate.id}">
                    <img src="${candidate.imagePath}" alt="${candidate.name}">
                    <div class="candidate-title">${candidate.name}</div>
                    <div class="selection-guide">이 후보를 선택해주세요!</div>
                </div>
                
                <div class="candidate-box bye-opponent" data-id="bye">
                    <div class="bye-message">
                        <p>부전승입니다.</p>
                        <p>옆 후보를 선택해주세요.</p>
                    </div>
                </div>
            </div>
        </div>
    `;

    // 💡 이벤트 리스너 설정: 부전승 후보를 클릭했을 때
    const byeBox = document.querySelector('.candidate-box.bye-candidate');
    byeBox.addEventListener('click', () => {
        handleByeSelection(candidate);
    });

    // 💡 부전승 메시지 박스는 클릭을 막아 사용자가 실수로 선택하지 않도록 합니다.
    document.querySelector('.candidate-box.bye-opponent').addEventListener('click', (e) => {
        e.stopPropagation();
    });
}

/**
 * 부전승 후보 선택을 처리하고 다음 단계로 진행합니다.
 */
function handleByeSelection(candidate) {
    // 1. 부전승 후보를 승자 목록에 추가
    winners.push(candidate);

    // 2. 부전승은 API 기록(클릭 수 증가)이 필요 없으므로 바로 다음 단계로 진행
    startNextRound();
}

/**
 * 두 후보의 대결 화면을 렌더링합니다.
 */
function renderMatch(c1, c2) {
    const roundName = currentRound === 2 ? '결승' : `${currentRound}강`;

    app.innerHTML = `
        <div id="worldcupScreen">
            <h2 id="roundInfo">🔥 ${roundName}: ${roundCounter}번째 대결 🔥</h2> 
            <div id="selectionArea">
                <div class="candidate-box" data-id="${c1.id}">
                    <img src="${c1.imagePath}" alt="${c1.name}">
                    <div class="candidate-title">${c1.name}</div>
                </div>
                <div class="candidate-box" data-id="${c2.id}">
                    <img src="${c2.imagePath}" alt="${c2.name}">
                    <div class="candidate-title">${c2.name}</div>
                </div>
            </div>
            <p style="margin-top: 20px;">원하는 사진을 클릭하세요!</p>
        </div>
    `;

    // 이벤트 리스너 설정: 선택 시
    document.querySelectorAll('.candidate-box').forEach(box => {
        box.addEventListener('click', () => {
            handleSelection(box.dataset.id, c1, c2);
        });
    });
}

/**
 * 후보 선택을 처리하고 다음 단계로 진행합니다.
 * @param {string} selectedId - 사용자가 선택한 후보의 ID
 * @param {object} c1 - 첫 번째 후보 객체
 * @param {object} c2 - 두 번째 후보 객체
 */

async function handleSelection(selectedId, c1, c2) {

    const winnerCandidate = (c1.id.toString() === selectedId) ? c1 : c2;
    const loserCandidate = (c1.id.toString() !== selectedId) ? c1 : c2;
    // 1. 백엔드에 선택 기록 저장 (클릭 수 및 1등 횟수 집계용)
    // POST /api/worldcup/{id}/select
    await apiPost(`/worldcup/${worldcupId}/select`, {
        winnerId: winnerCandidate.id,
        loserId: loserCandidate.id,
        round: currentRound,
        userId: currentUser.userId

    });

    // 2. 현재 라운드 승자 목록에 추가
    winners.push(winnerCandidate);

    // 3. 다음 라운드 진행
    startNextRound();

}