/**
 * detail.js
 */
document.addEventListener('DOMContentLoaded', () => {
    init();
});

async function init() {
    const pathSegments = window.location.pathname.split('/');
    const categoryId = pathSegments.filter(segment => segment !== "").pop();

    if (!categoryId || isNaN(categoryId)) return;

    const data = await fetchCategoryData(categoryId);
    if (data) {
        data.categoryId = categoryId;
        renderMandalart(data);
    }
}

async function fetchCategoryData(id) {
    try {
        const response = await fetch(`/api/mandalart/category/${id}`);
        return await response.json();
    } catch (error) {
        return null;
    }
}

function renderMandalart(data) {
    const titleText = data.title || "목표 정보 없음";
    document.getElementById('category-title').innerText = titleText;
    document.getElementById('center-cell').innerText = titleText;

    const subCells = document.querySelectorAll('.sub-cell');
    subCells.forEach((cell, index) => {
        // 텍스트 반영
        cell.innerText = (data.items && data.items[index]) ? data.items[index] : "○";

        // [핵심] 서버에서 받아온 완료 상태가 true라면 'completed' 클래스 부여
        if (data.completedList && data.completedList[index] === true) {
            cell.classList.add('completed');
        }

        cell.onclick = () => toggleCell(data.categoryId, index, cell);
    });

    updateGrowthUI(data);
}

async function toggleCell(categoryId, cellIndex, cellElement) {
    try {
        const response = await fetch(`/api/mandalart/category/${categoryId}/cell/${cellIndex}/toggle`, {
            method: 'POST'
        });
        const result = await response.json();

        if (result.success) {
            // 클릭 시 색상 및 취소선 토글
            cellElement.classList.toggle('completed', result.isCompleted);

            updateGrowthUI({
                achieved: result.achieved,
                imgKey: getImgKeyFromTitle()
            });
        }
    } catch (error) {
        console.error("Error:", error);
    }
}

function updateGrowthUI(data) {
    const imgElement = document.getElementById('growth-img');
    const textElement = document.getElementById('achievement-text');
    const progressFill = document.getElementById('progress-fill');

    const achieved = data.achieved || 0;
    const imgKey = data.imgKey || getImgKeyFromTitle();

    imgElement.src = `/images/${imgKey}/stage_${achieved}.png`;
    imgElement.onerror = () => { imgElement.src = '/images/default/stage_0.png'; };

    textElement.innerText = `현재 8단계 중 ${achieved}단계 성취!`;
    const percent = (achieved / 8) * 100;

    setTimeout(() => {
        progressFill.style.width = `${percent}%`;
    }, 100);
}

function getImgKeyFromTitle() {
    const title = document.getElementById('category-title').innerText;
    if (title.includes("독서")) return "book";
    if (title.includes("운동")) return "gym";
    if (title.includes("공부")) return "study";
    if (title.includes("경제")) return "economy";
    if (title.includes("여행")) return "travel";
    if (title.includes("취미")) return "hobby";
    if (title.includes("가족")) return "family";
    if (title.includes("자기관리")) return "selfcare";
    return "default";
}

document.getElementById('btn-back').addEventListener('click', () => {
    window.location.href = '/';
});