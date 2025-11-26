//모든 API 호출을 처리하는 중앙 파일

// 백엔드 API의 기본 경로
const API_BASE_URL = '/api';

// GET 요청을 처리하는 범용 함수
export async function apiGet(path) {
    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            headers: {
                'Content-Type': 'application/json',
                // 로그인 후 받은 세션/토큰이 있다면 여기에 추가 (예: Authorization 헤더)
                // 'Authorization': `Bearer ${localStorage.getItem('userToken')}`
            }
        });
        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }
        return response.json();
    } catch (error) {
        console.error("GET 요청 실패:", error);
        alert("데이터를 불러오는 데 실패했습니다.");
        return null;
    }
}

// POST 요청을 처리하는 범용 함수
export async function apiPost(path, data) {
    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // 로그인 후 받은 세션/토큰이 있다면 여기에 추가
                // 'Authorization': `Bearer ${localStorage.getItem('userToken')}`
            },
            body: JSON.stringify(data)
        });

        // 200 OK 또는 201 Created 등 성공적인 상태 코드 확인
        if (!response.ok) {
            const errorBody = await response.json();
            throw new Error(`HTTP Error: ${response.status} - ${errorBody.message || '서버 오류'}`);
        }

        const contentType = response.headers.get("content-type");
        if (response.status === 204 || (contentType && !contentType.includes("application/json"))) {
            return { success: true };
        }

        // 응답 본문이 없을 수도 있으므로 확인
        return response.json();
    } catch (error) {
        console.error("POST 요청 실패:", error);
        alert(error.message || "요청 처리 중 오류가 발생했습니다.");
        return null;
    }
}

// --- 특정 API 호출 함수 예시 ---

/**
 * 닉네임을 서버에 등록하고 세션을 받습니다.
 * @param {string} nickname
 * @returns {object} 서버 응답 데이터 (예: { token: '...', nickname: '...' })
 */
export async function loginUser(nickname, password) { // 💡 password 매개변수 추가
    return apiPost('/login', { nickname, password });
}

/**
 * 월드컵 목록을 가져옵니다.
 * @returns {Array} 월드컵 리스트 (예: [{ id: 1, title: '...', thumbnail: '...' }])
 */
export async function fetchWorldcupList() {
    return apiGet('/main');
}

// 💡 PUT 요청을 처리하는 함수 추가 (apiPost와 유사)
export async function apiPut(path, data) {
    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: 'PUT', // 💡 HTTP 메서드 PUT
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorBody = await response.json();
            throw new Error(`HTTP Error: ${response.status} - ${errorBody.message || '서버 오류'}`);
        }

        const contentType = response.headers.get("content-type");
        if (response.status === 204 || (contentType && !contentType.includes("application/json"))) {
            return { success: true };
        }
        return response.json();
    } catch (error) {
        console.error("PUT 요청 실패:", error);
        alert(error.message || "요청 처리 중 오류가 발생했습니다.");
        return null;
    }
}

// DELETE 요청을 처리하는 범용 함수
export async function apiDelete(path) {
    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: 'DELETE', // 💡 HTTP 메서드를 DELETE로 지정
            headers: {
                'Content-Type': 'application/json',
                // 'Authorization': `Bearer ${localStorage.getItem('userToken')}`
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        // 성공 응답 (204 No Content가 예상됨)
        return { success: true };

    } catch (error) {
        console.error("DELETE 요청 실패:", error);
        alert(error.message || "삭제 요청 처리 중 오류가 발생했습니다.");
        return null;
    }
}

// 이 외의 함수들 (worldcup/{id}, /result/{id}/comments 등)도 여기에 추가될 것입니다.
// 예: export async function saveSelection(worldcupId, selectedCandidateId) { ... }