import { apiGet, apiPost } from './api.js';
import { currentUser } from './login.js';

const app = document.getElementById('app');

/**
 * 최종 결과 화면을 렌더링하고 댓글 기능을 활성화합니다.
 * @param {string} worldcupId - 월드컵 ID
 * @param {string} winnerId - 최종 우승 후보 ID
 */
export async function renderResultScreen(worldcupId, winnerId) {
    app.innerHTML = '<h2 style="text-align:center;">결과를 불러오는 중...</h2>';

    // 1. 결과 API 호출 (GET /api/result/{id})
    // 이 API는 우승 후보 정보와 '1등으로 뽑은 사용자 닉네임 목록'을 반환해야 합니다.
    const resultData = await apiGet(`/result/${worldcupId}/${winnerId}`);

    if (!resultData || !resultData.winner) {
        app.innerHTML = '<p class="error">결과 데이터를 불러올 수 없습니다.</p>';
        return;
    }

    const winner = resultData.winner; // 우승 후보 객체
    const winnerNicknames = resultData.topWinnerNicknames || []; // 1등을 뽑은 사용자 닉네임 목록

    // 2. 결과 화면 렌더링
    app.innerHTML = `
        <div id="resultScreen">
            <div class="winner-container">
                <h2>🎉 최종 결과! 🎉</h2>
                <img src="${winner.imagePath}" alt="${winner.name}">
                <h3>"${winner.name}"</h3>
                <p style="font-size: 1.1rem; color: var(--color-primary); margin-top: 10px;">
                    (${currentUser.nickname})님의 1등 트윗
                </p>
            </div>

            <div class="comment-section">
                <h3>댓글 남기기</h3>
                <form id="commentForm">
                    <textarea id="commentContent" placeholder="${currentUser.nickname}님, 한 말씀 남겨주세요!" required></textarea>
                    <button type="submit">댓글 등록</button>
                </form>
                
                <h3 style="margin-top: 2rem;">한마디 모음</h3>
                <div id="commentList">
                    </div>
            </div>
        </div>
    `;

    // 3. 댓글 로드 및 이벤트 리스너 설정
    await loadComments(worldcupId);

    document.getElementById('commentForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const content = document.getElementById('commentContent').value.trim();

        if (content) {
            // POST /api/result/{id}/comments
            const success = await apiPost(`/result/${worldcupId}/comments`, {
                content: content,
                nickname: currentUser.nickname, // 서버에서 닉네임 검증 필요
                userId: currentUser.userId
            });

            if (success) {
                document.getElementById('commentContent').value = '';
                await loadComments(worldcupId); // 댓글 목록 새로고침
            }
        }
    });
}

/**
 * 해당 월드컵의 댓글 목록을 불러와 렌더링합니다.
 * @param {string} worldcupId - 월드컵 ID
 */
async function loadComments(worldcupId) {
    const commentListDiv = document.getElementById('commentList');
    commentListDiv.innerHTML = '<p style="text-align:center;">댓글 불러오는 중...</p>';

    // GET /api/result/{id}/comments
    // 백엔드는 각 댓글마다 content, createdAt, 그리고 'nickname'을 함께 반환합니다.
    const comments = await apiGet(`/result/${worldcupId}/comments`);

    if (comments && comments.length > 0) {
        commentListDiv.innerHTML = comments.map(comment => `
            <div class="comment-item">
                <p>${comment.content}</p>
                <p class="comment-meta">
                    작성자: <strong>${comment.nickname}</strong> 
                    (${new Date(comment.createdAt).toLocaleDateString()})
                </p>
            </div>
        `).join('');
    } else {
        commentListDiv.innerHTML = '<p style="text-align:center;">아직 댓글이 없습니다. 첫 댓글을 남겨보세요!</p>';
    }
}

/**
 * 우승자 결과 없이 댓글 목록만 보여주는 화면을 렌더링합니다.
 * @param {string} worldcupId - 월드컵 ID
 */
export async function renderCommentPage(worldcupId) {
    const app = document.getElementById('app');
    if (!currentUser || currentUser.nickname !== 'admin') {
        alert("🚫 관리자만 접근할 수 있는 페이지입니다.");
        window.location.hash = '#main'; // 메인 화면으로 강제 이동
        return; // 함수 실행 중단
    }

    // 기본 골격 렌더링
    app.innerHTML = `
        <div id="resultScreen" class="comments-only-page">
             <h2 style="margin-bottom: 0.5rem;">💬 월드컵 댓글 관리 (Admin)</h2>
             </div>
    `;

    // 기존에 정의된 댓글 로드 함수 재사용하여 데이터 채우기
    await loadComments(worldcupId);
}