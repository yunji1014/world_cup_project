import { apiPost, apiGet, fetchWorldcupList, apiDelete, apiPut } from './api.js';
import { currentUser } from './login.js';

const app = document.getElementById('app');
const ADMIN_NICKNAME = 'admin';

let currentWorldcups = []; // 삭제 시 타이틀 확인을 위해 목록을 저장할 전역 변수

/**
 * 관리자 권한을 확인하고, 없으면 메인으로 돌려보냅니다.
 * @returns {boolean} 관리자 권한 유무
 */
function checkAdminAccess() {
    if (!currentUser || currentUser.nickname !== ADMIN_NICKNAME) {
        alert("관리자 권한이 없습니다.");
        window.location.hash = '#main';
        return false;
    }
    return true;
}

/**
 * 월드컵 생성 화면을 렌더링합니다.
 */
export function renderCreateScreen() {
    if (!checkAdminAccess()) return;

    app.innerHTML = `
        <div id="worldcupCreateScreen">
            <h2>✨ 새 이상형 월드컵 생성</h2>
            <form id="worldcupCreateForm" enctype="multipart/form-data"> <label for="wcTitle">월드컵 제목:</label>
                <input type="text" id="wcTitle" name="title" required> <label for="wcThumbnail">썸네일 이미지 파일:</label>
                <input type="file" id="wcThumbnail" name="thumbnail" accept="image/*" > 

                <h3 style="margin-top: 20px;">후보 목록 (파일 및 제목)</h3>
                <div id="candidatesContainer">
                    <div class="candidate-input-group">
                        <input type="file" class="candidate-file" name="candidateFiles" accept="image/*" required>
                        <input type="text" class="candidate-name" placeholder="후보 제목" required>
                        <button type="button" class="remove-candidate-btn" disabled>삭제</button>
                    </div>
                </div>
                
                <button type="button" id="addCandidateBtn" style="margin: 10px 0;">후보 추가</button>
                <button type="submit" style="margin-top: 20px;">월드컵 생성하기</button>
            </form>
        </div>
    `;

    // 후보 추가/삭제 로직
    document.getElementById('addCandidateBtn').addEventListener('click', addCandidateInput);
    document.getElementById('candidatesContainer').addEventListener('click', (e) => {
        if (e.target.classList.contains('remove-candidate-btn')) {
            e.target.closest('.candidate-input-group').remove();
            updateRemoveButtons();
        }
    });

    // 초기 버튼 상태 업데이트
    updateRemoveButtons();

    // 폼 제출 로직
    document.getElementById('worldcupCreateForm').addEventListener('submit', handleCreateSubmit);
}

function addCandidateInput() {
    const container = document.getElementById('candidatesContainer');
    if (container.children.length >= 200) {
        alert("후보는 최대 200개까지 가능합니다.");
        return;
    }
    const newGroup = document.createElement('div');
    newGroup.classList.add('candidate-input-group');

    // 💡 [수정됨] 기존 input type="text" -> input type="file"로 변경
    // 💡 [수정됨] name="candidateFiles" 추가 (FormData 수집용)
    newGroup.innerHTML = `
        <input type="file" class="candidate-file" name="candidateFiles" accept="image/*" required>
        <input type="text" class="candidate-name" placeholder="후보 제목" required>
        <button type="button" class="remove-candidate-btn">삭제</button>
    `;
    container.appendChild(newGroup);
    updateRemoveButtons();
}

function updateRemoveButtons() {
    const buttons = document.querySelectorAll('.remove-candidate-btn');
    // 후보가 2개 미만일 때는 삭제 버튼 비활성화
    buttons.forEach(btn => btn.disabled = (buttons.length <= 2));
}

async function handleCreateSubmit(e) {
    e.preventDefault();

    const title = document.getElementById('wcTitle').value;
    const thumbnailFile = document.getElementById('wcThumbnail').files[0];
    const candidateGroups = document.querySelectorAll('.candidate-input-group');

    const formData = new FormData();
    const candidatesData = [];

    if (thumbnailFile) {
        formData.append('thumbnail', thumbnailFile);
    }

    // 💡 [수정됨] 루프 로직 개선: 파일 누락 검사
    let hasError = false;

    candidateGroups.forEach((group) => {
        const fileInput = group.querySelector('.candidate-file');
        const nameInput = group.querySelector('.candidate-name');

        // 유효성 검사: 파일이 없으면 중단
        if (!fileInput.files.length) {
            //alert(`'${nameInput.value || '이름 없는 후보'}'의 이미지 파일을 선택해주세요.`);
            hasError = true;
            return;
        }

        // 파일 추가
        formData.append('candidateFiles', fileInput.files[0]);

        // 메타데이터 추가
        candidatesData.push({
            name: nameInput.value
            // create 시에는 id나 imagePath가 필요 없음
        });
    });

    if (hasError) return; // 에러가 있으면 함수 종료

    if (candidatesData.length < 2) {
        alert("최소 2개 이상의 후보를 입력해야 합니다.");
        return;
    }

    formData.append('title', title);
    formData.append('candidatesDataJson', JSON.stringify({ candidates: candidatesData }));

    // 4. API 호출 (fetch API 직접 사용)
    try {
        const response = await fetch('/api/main/create', {
            method: 'POST',
            body: formData
        });

        // ... 에러 처리 및 성공 처리 ...
        if (!response.ok) throw new Error(await response.text());
        const resJson = await response.json();
        alert("생성 성공!");
        window.location.hash = '#main';
    } catch (e) {
        console.error(e);
        alert("실패: " + e.message);
    }
}


/**
 * 관리자 메뉴의 월드컵 목록 화면을 렌더링합니다.
 */
export async function renderAdminMainScreen() {
    if (!checkAdminAccess()) return;

    app.innerHTML = `
        <div id="adminMainScreen">
            <h2>관리자 메뉴: 월드컵 관리</h2>
            <button onclick="window.location.hash = '#admin/create'" style="margin-bottom: 20px;">+ 새 월드컵 생성</button>
            <div id="adminList">월드컵 목록을 불러오는 중...</div>
        </div>
    `;

    // 1. 월드컵 목록 로드 (api.js의 fetchWorldcupList 사용)
    const worldcups = await fetchWorldcupList();
    currentWorldcups = worldcups; // 목록 저장

    const adminListDiv = document.getElementById('adminList');
    if (!worldcups || worldcups.length === 0) {
        adminListDiv.innerHTML = '<p>생성된 월드컵이 없습니다.</p>';
        return;
    }

    let listHtml = `
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>제목</th>
                    <th>관리 기능</th>
                </tr>
            </thead>
            <tbody>
    `;

    worldcups.forEach(wc => {
        listHtml += `
            <tr data-id="${wc.id}">
                <td>${wc.id}</td>
                <td>${wc.title}</td>
                <td class="admin-actions">
                    <button class="action-rank" data-id="${wc.id}">랭킹 조회</button>
                    <button class="action-edit" data-id="${wc.id}">수정</button>
                    <button class="action-delete" data-id="${wc.id}">삭제</button>
                </td>
            </tr>
        `;
    });

    listHtml += '</tbody></table>';
    adminListDiv.innerHTML = listHtml;

    // 이벤트 리스너 추가
    adminListDiv.addEventListener('click', handleAdminActions);
}

// 💡 관리 버튼 클릭 핸들러
function handleAdminActions(e) {
    const target = e.target;
    const worldcupId = target.dataset.id;
    if (!worldcupId) return;

    if (target.classList.contains('action-rank')) {
        // 랭킹 조회 화면으로 이동 (ID 전달)
        window.location.hash = `#admin/rank/${worldcupId}`;
    } else if (target.classList.contains('action-edit')) {
        window.location.hash = `#admin/edit/${worldcupId}`; // 추후 수정 화면 라우팅
    } else if (target.classList.contains('action-delete')) {
        handleDeleteWorldcup(worldcupId);
    }
}

// 💡 삭제 기능 구현
async function handleDeleteWorldcup(worldcupId) {
    const worldcupTitle = currentWorldcups.find(wc => wc.id == worldcupId)?.title || '제목 없음';
    if (!confirm(`월드컵 ID ${worldcupId} (${worldcupTitle})를 정말로 삭제하시겠습니까? 관련 모든 데이터(후보, 결과, 댓글)가 삭제됩니다.`)) {
        return;
    }

    // DELETE /api/admin/worldcup/{worldcupId}
    // 💡 apiDelete 사용
    const response = await apiDelete(`/admin/worldcup/${worldcupId}`);

    if (response && response.success) {
        alert(`월드컵 ${worldcupId}가 성공적으로 삭제되었습니다.`);
        renderAdminMainScreen(); // 목록 새로고침
    } else {
        alert("월드컵 삭제에 실패했습니다. (서버 응답 확인)");
    }
}

// admin.js

export async function renderEditScreen(worldcupId) {
    if (!checkAdminAccess()) return;

    // 1. 데이터 가져오기
    const data = await apiGet(`/admin/worldcup/${worldcupId}`);

    // 데이터 유효성 검사
    if (!data || !data.title) {
        app.innerHTML = '<p class="error">데이터를 불러오지 못했습니다.</p>';
        return;
    }

    // 2. 기본 생성 화면 구조를 먼저 그리기
    renderCreateScreen();

    // 3. 화면 제목 및 버튼 텍스트 변경
    document.querySelector('#worldcupCreateScreen h2').textContent = `✨ 월드컵 수정 (ID: ${worldcupId})`;
    const submitBtn = document.querySelector('#worldcupCreateForm button[type="submit"]');
    submitBtn.textContent = '수정 완료';

    // 4. 폼 요소 가져오기 (여기서 form 변수를 한 번만 선언)
    const form = document.getElementById('worldcupCreateForm');

    // 5. 제목 및 썸네일 채우기
    // DTO: title, thumbnailUrl
    const titleInput = document.getElementById('wcTitle');
    if (titleInput) titleInput.value = data.title;

    const thumbnailInput = document.getElementById('wcThumbnail');
    if (thumbnailInput) {
        const thumbnailContainer = thumbnailInput.parentNode;
        const thumbPreview = document.createElement('div');

        // 썸네일 미리보기
        const thumbSrc = data.thumbnailUrl || '';
        thumbPreview.innerHTML = thumbSrc
            ? `<p>현재 썸네일:</p><img src="${thumbSrc}" style="max-width: 150px; border-radius: 10px; margin-bottom: 10px;">`
            : `<p>현재 썸네일 없음</p>`;

        thumbnailContainer.insertBefore(thumbPreview, thumbnailInput);
    }

    // 6. 후보 목록 채우기
    const container = document.getElementById('candidatesContainer');
    container.innerHTML = ''; // 초기화

    // 디버깅용 로그 (데이터 확인)
    console.log("받아온 후보 데이터:", data.candidates);

    data.candidates.forEach((candidate, index) => {
        const newGroup = document.createElement('div');
        newGroup.classList.add('candidate-input-group');

        // 💡 [핵심 수정] DTO의 필드명인 'imagePath'를 사용합니다.
        const imgPath = candidate.imagePath || "";

        // 수정 제출 시 사용하기 위해 data 속성에 기존 경로 저장
        newGroup.dataset.originalUrl = imgPath;

        newGroup.innerHTML = `
            <input type="hidden" class="candidate-id" value="${candidate.id}">
            
            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 5px;">
                <img src="${imgPath}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px; border: 1px solid #ddd;">
                <span style="font-size: 0.8rem; color: #666;">
                    ${imgPath ? '기존 이미지 유지' : '❌ 이미지 경로 없음'}
                </span>
            </div>

            <input type="file" class="candidate-file" name="candidateFiles" accept="image/*">
            <input type="text" class="candidate-name" placeholder="후보 제목" value="${candidate.name}" required>
            <button type="button" class="remove-candidate-btn">삭제</button>
        `;
        container.appendChild(newGroup);
    });

    updateRemoveButtons();

    // 7. 폼 제출 이벤트 핸들러 교체
    // 위에서 선언한 form 변수 사용
    form.onsubmit = (e) => handleEditSubmit(e, worldcupId);
}

// 💡 수정 제출 핸들러 (PUT 요청)
async function handleEditSubmit(e, worldcupId) {
    e.preventDefault();

    const title = document.getElementById('wcTitle').value;
    const thumbnailInput = document.getElementById('wcThumbnail');
    const candidateGroups = document.querySelectorAll('.candidate-input-group');

    const formData = new FormData();
    const candidatesData = [];
    let hasError = false; // 에러 플래그

    // 1. 썸네일 처리 (파일이 있을 때만 추가)
    if (thumbnailInput.files.length > 0) {
        formData.append('thumbnail', thumbnailInput.files[0]);
    }

    // 2. 후보 목록 처리
    candidateGroups.forEach(group => {
        if (hasError) return; // 이미 에러가 났으면 중단

        const idInput = group.querySelector('.candidate-id');
        const fileInput = group.querySelector('.candidate-file');
        const nameInput = group.querySelector('.candidate-name');

        // renderEditScreen에서 저장해둔 기존 URL 확인
        const originalUrl = group.dataset.originalUrl;

        // 💡 [핵심 수정] 유효성 검사 로직 변경
        // 새 파일도 없고(0개) AND 기존 이미지도 없으면(빈값) -> 에러!
        const hasNewFile = fileInput && fileInput.files.length > 0;
        const hasExistingImage = originalUrl && originalUrl.trim() !== "";

        if (!hasNewFile && !hasExistingImage) {
            //alert(`'${nameInput.value || '이름 없는 후보'}'의 이미지 파일을 선택해주세요.`);
            hasError = true;
            return;
        }

        const candidateMeta = {
            id: idInput ? idInput.value : null,
            name: nameInput.value,
            imagePath: originalUrl // 기본적으로 기존 URL 유지
        };

        // 새 파일이 있는 경우에만 파일 추가 및 경로 초기화
        if (hasNewFile) {
            formData.append('candidateFiles', fileInput.files[0]);
            candidateMeta.imagePath = ""; // 백엔드에 "새 파일임" 알림
        }

        candidatesData.push(candidateMeta);
    });

    if (hasError) return; // 검사에 걸렸으면 여기서 함수 종료

    if (candidatesData.length < 2) {
        alert("최소 2개 이상의 후보가 필요합니다.");
        return;
    }

    // 3. 데이터 전송 준비
    formData.append('title', title);
    formData.append('candidatesDataJson', JSON.stringify({ candidates: candidatesData }));

    // 4. API 전송
    try {
        const response = await fetch(`/api/admin/worldcup/${worldcupId}`, {
            method: 'PUT',
            body: formData
        });

        if (response.ok) {
            alert("수정되었습니다!");
            window.location.hash = '#admin';
        } else {
            const err = await response.text();
            throw new Error(err);
        }
    } catch (error) {
        console.error("수정 실패:", error);
        alert("수정 실패: " + error.message);
    }
}


/**
 * 랭킹 조회 화면을 렌더링합니다.
 */
export async function renderRankScreen(worldcupId) {
    if (!checkAdminAccess()) return;

    app.innerHTML = `
        <div id="rankScreen">
            <h2 onclick="window.location.hash = '#admin'" style="cursor:pointer;">
                🏆 월드컵 랭킹 통계 (관리자 전용)
            </h2>
            <div id="rankControls">
                <p>⚠️ 월드컵 ID: ${worldcupId}에 대한 랭킹을 표시합니다. ⚠️</p>
                <button onclick="window.location.hash = '#admin'">목록으로 돌아가기</button>
            </div>
            <div id="rankList">랭킹 데이터를 불러오는 중...</div>
        </div>
    `;

    // 랭킹 데이터 로드 (GET /api/admin/rank/{id} - {id}는 전체 통합 ID 또는 드롭다운 선택 ID)
    // 여기서는 일단 통합 랭킹을 위해 ID를 'all'로 가정합니다.
    const rankData = await apiGet(`/admin/rank/${worldcupId}`);

    const rankListDiv = document.getElementById('rankList');

    if (rankData && rankData.length > 0) {
        let tableHtml = `
            <table>
                <thead>
                    <tr>
                        <th>순위</th>
                        <th>후보 이름</th>
                        <th>1등 횟수</th>
                        <th>총 클릭 수</th>
                        <th>이 후보를 선택한 사용자의 닉네임</th>
                    </tr>
                </thead>
                <tbody>
        `;

        rankData.forEach((item, index) => {
            tableHtml += `
                <tr>
                    <td>${index + 1}</td>
                    <td>${item.name}</td>
                    <td>${item.winCount}</td>
                    <td>${item.totalSelectionCount}</td>
                    <td>${item.topWinnerNicknames.join(', ')}</td>
                </tr>
            `;
        });

        tableHtml += '</tbody></table>';
        rankListDiv.innerHTML = tableHtml;

    } else {
        rankListDiv.innerHTML = `<p>월드컵 ID ${worldcupId}에 대해 아직 집계된 랭킹 데이터가 없습니다.</p>`;
    }
}