// ==UserScript==
// @name         B站视频时长过滤
// @namespace    http://tampermonkey.net/
// @version      1.0
// @description  屏蔽B站上时长小于10分钟的视频
// @author       You
// @match        https://*.bilibili.com/*
// @grant        none
// ==/UserScript==

(function() {
    'use strict';

    // 定义最小视频时长(秒)
    const MIN_DURATION = 600; // 10分钟 = 600秒

    // 观察视频列表变化
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === 'childList') {
                hideShortVideos();
            }
        });
    });

    // 隐藏短视频函数
    function hideShortVideos() {
        // 获取所有视频卡片
        const videoCards = document.querySelectorAll('.video-card, .bili-video-card');

        videoCards.forEach((card) => {
            // 获取视频时长元素（兼容多种B站视频卡片结构）
            // 优先选择明确标记为duration的元素
            let durationElem = card.querySelector('.duration, .bili-video-card__stats-duration');
            
            // 如果没找到，尝试从bili-cover-card__stat中找最后一个span（通常是时长）
            if (!durationElem) {
                const stats = card.querySelectorAll('.bili-cover-card__stat span');
                if (stats.length > 0) {
                    durationElem = stats[stats.length - 1];
                }
            }
            if (!durationElem) return;

            // 解析时长
            const durationText = durationElem.textContent.trim();
            const durationSeconds = parseDuration(durationText);

            // 检查是否小于最小时长
            if (durationSeconds > 0 && durationSeconds < MIN_DURATION) {
                // 隐藏视频卡片
                card.style.display = 'none';
                // 或者移除视频卡片
                // card.remove();
            }
        });
    }

    // 解析时长文本为秒数
    function parseDuration(durationText) {
        // 处理形如 '02:34/05:36' 的格式，提取斜杠后面的内容作为时长
        if (durationText.includes('/')) {
            durationText = durationText.split('/')[1].trim();
        }
        
        // 格式可能是 '09:30' 或 '1:23:45'
        const parts = durationText.split(':').map(part => parseInt(part, 10));

        if (parts.length === 2) {
            // 分:秒
            return parts[0] * 60 + parts[1];
        } else if (parts.length === 3) {
            // 时:分:秒
            return parts[0] * 3600 + parts[1] * 60 + parts[2];
        }

        return 0;
    }

    // 初始加载时执行一次
    hideShortVideos();

    // 开始观察页面变化
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
})();