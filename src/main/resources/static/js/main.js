/**
 * ═══════════════════════════════════════════════════════════
 * 🏥 Clinic Application — Main JavaScript
 * ═══════════════════════════════════════════════════════════
 *
 * ماژول‌های اصلی:
 *  • Auth      — مدیریت توکن و احراز هویت
 *  • API       — wrapper برای fetch با توکن
 *  • Toast     — نمایش پیام‌های زیبا
 *  • Form      — اعتبارسنجی و ارسال فرم‌ها
 *  • Date      — تبدیل تاریخ میلادی ↔ شمسی
 *  • Utils     — توابع کمکی
 * ═══════════════════════════════════════════════════════════
 */

'use strict';

// ═══════════════════════════════════════════════════════════
// 🔐 Auth — مدیریت توکن و احراز هویت
// ═══════════════════════════════════════════════════════════

const Auth = {
    ACCESS_TOKEN_KEY: 'accessToken',
    REFRESH_TOKEN_KEY: 'refreshToken',
    USER_KEY: 'user',

    /**
     * ذخیره توکن‌ها و اطلاعات کاربر پس از ورود موفق.
     */
    saveSession(authResponse) {
        localStorage.setItem(this.ACCESS_TOKEN_KEY, authResponse.accessToken);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, authResponse.refreshToken);
        localStorage.setItem(this.USER_KEY, JSON.stringify(authResponse.user));
    },

    /**
     * دریافت Access Token.
     */
    getAccessToken() {
        return localStorage.getItem(this.ACCESS_TOKEN_KEY);
    },

    /**
     * دریافت Refresh Token.
     */
    getRefreshToken() {
        return localStorage.getItem(this.REFRESH_TOKEN_KEY);
    },

    /**
     * دریافت اطلاعات کاربر جاری.
     */
    getCurrentUser() {
        const userStr = localStorage.getItem(this.USER_KEY);
        if (!userStr) return null;
        try {
            return JSON.parse(userStr);
        } catch (e) {
            return null;
        }
    },

    /**
     * بررسی لاگین بودن.
     */
    isAuthenticated() {
        return !!this.getAccessToken();
    },

    /**
     * بررسی نقش کاربر جاری.
     */
    hasRole(role) {
        const user = this.getCurrentUser();
        return user && user.role === role;
    },

    /**
     * بررسی یکی از نقش‌ها.
     */
    hasAnyRole(...roles) {
        const user = this.getCurrentUser();
        return user && roles.includes(user.role);
    },

    /**
     * خروج از سیستم.
     */
    async logout(redirect = true) {
        const token = this.getAccessToken();

        if (token) {
            try {
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: { 'Authorization': 'Bearer ' + token }
                });
            } catch (e) {
                console.warn('Logout API failed:', e);
            }
        }

        this.clearSession();

        if (redirect) {
            window.location.href = '/login?logout=true';
        }
    },

    /**
     * پاک کردن کامل session.
     */
    clearSession() {
        localStorage.removeItem(this.ACCESS_TOKEN_KEY);
        localStorage.removeItem(this.REFRESH_TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    },

    /**
     * اجبار به ورود مجدد.
     */
    requireAuth() {
        if (!this.isAuthenticated()) {
            const returnUrl = encodeURIComponent(window.location.pathname);
            window.location.href = `/login?returnUrl=${returnUrl}`;
            return false;
        }
        return true;
    },

    /**
     * تازه‌سازی Access Token با Refresh Token.
     */
    async refreshToken() {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) return false;

        try {
            const res = await fetch('/api/auth/refresh', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            });

            if (!res.ok) return false;

            const data = await res.json();
            this.saveSession(data);
            return true;
        } catch (e) {
            console.error('Token refresh failed:', e);
            return false;
        }
    }
};

// ═══════════════════════════════════════════════════════════
// 🌐 API — wrapper برای fetch با توکن
// ═══════════════════════════════════════════════════════════

const API = {
    BASE_URL: '/api',

    /**
     * درخواست با مدیریت خودکار توکن و 401.
     */
    async request(endpoint, options = {}) {
        const url = endpoint.startsWith('http') ? endpoint : this.BASE_URL + endpoint;

        // هدرها
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        // افزودن توکن
        const token = Auth.getAccessToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        // درخواست
        let response;
        try {
            response = await fetch(url, {
                ...options,
                headers
            });
        } catch (e) {
            throw new Error('خطا در ارتباط با سرور');
        }

        // مدیریت 401 — توکن منقضی
        if (response.status === 401) {
            const refreshed = await Auth.refreshToken();
            if (refreshed) {
                // تلاش مجدد با توکن جدید
                headers['Authorization'] = 'Bearer ' + Auth.getAccessToken();
                response = await fetch(url, { ...options, headers });
            } else {
                Auth.clearSession();
                window.location.href = '/login?expired=true';
                throw new Error('نشست شما منقضی شده است');
            }
        }

        // مدیریت 403
        if (response.status === 403) {
            throw new Error('شما به این بخش دسترسی ندارید');
        }

        // مدیریت 204 (No Content)
        if (response.status === 204) {
            return null;
        }

        // پردازش پاسخ
        const contentType = response.headers.get('content-type');
        let data;

        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        // مدیریت خطا
        if (!response.ok) {
            const errorMessage = data?.message
                || data?.error
                || (data?.validationErrors
                    ? Object.values(data.validationErrors).join(' - ')
                    : `خطا (${response.status})`);

            const error = new Error(errorMessage);
            error.status = response.status;
            error.data = data;
            throw error;
        }

        return data;
    },

    // ─── متدهای اختصار ───

    get(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'GET' });
    },

    post(endpoint, body, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'POST',
            body: body ? JSON.stringify(body) : undefined
        });
    },

    put(endpoint, body, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'PUT',
            body: body ? JSON.stringify(body) : undefined
        });
    },

    patch(endpoint, body, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'PATCH',
            body: body ? JSON.stringify(body) : undefined
        });
    },

    delete(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'DELETE' });
    }
};

// ═══════════════════════════════════════════════════════════
// 🍞 Toast — نمایش پیام‌های زیبا
// ═══════════════════════════════════════════════════════════

const Toast = {
    container: null,

    /**
     * ایجاد container اگر وجود ندارد.
     */
    init() {
        if (this.container) return;

        this.container = document.createElement('div');
        this.container.id = 'toast-container';
        this.container.style.cssText = `
            position: fixed;
            top: 20px;
            left: 20px;
            z-index: 9999;
            display: flex;
            flex-direction: column;
            gap: 10px;
            max-width: 400px;
        `;
        document.body.appendChild(this.container);

        // اضافه کردن استایل
        const style = document.createElement('style');
        style.textContent = `
            .toast {
                padding: 14px 18px;
                border-radius: 12px;
                font-family: 'Vazirmatn', Tahoma, sans-serif;
                font-size: 14px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.15);
                display: flex;
                align-items: center;
                gap: 10px;
                animation: toastSlideIn 0.3s ease;
                direction: rtl;
                min-width: 280px;
            }
            .toast.success {
                background: linear-gradient(135deg, #10b981, #059669);
                color: white;
            }
            .toast.error {
                background: linear-gradient(135deg, #ef4444, #dc2626);
                color: white;
            }
            .toast.warning {
                background: linear-gradient(135deg, #f59e0b, #d97706);
                color: white;
            }
            .toast.info {
                background: linear-gradient(135deg, #6366f1, #06b6d4);
                color: white;
            }
            .toast-icon {
                font-size: 20px;
                flex-shrink: 0;
            }
            .toast-message {
                flex: 1;
                line-height: 1.4;
            }
            .toast-close {
                background: rgba(255,255,255,0.2);
                border: none;
                color: white;
                width: 24px;
                height: 24px;
                border-radius: 6px;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 14px;
                flex-shrink: 0;
            }
            .toast-close:hover {
                background: rgba(255,255,255,0.3);
            }
            @keyframes toastSlideIn {
                from { opacity: 0; transform: translateX(-30px); }
                to { opacity: 1; transform: translateX(0); }
            }
            @keyframes toastSlideOut {
                from { opacity: 1; transform: translateX(0); }
                to { opacity: 0; transform: translateX(-30px); }
            }
        `;
        document.head.appendChild(style);
    },

    /**
     * نمایش پیام.
     */
    show(message, type = 'info', duration = 4000) {
        this.init();

        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span class="toast-icon">${icons[type] || icons.info}</span>
            <span class="toast-message">${Utils.escapeHtml(message)}</span>
            <button class="toast-close" onclick="this.parentElement.remove()">✕</button>
        `;

        this.container.appendChild(toast);

        // حذف خودکار
        if (duration > 0) {
            setTimeout(() => {
                if (toast.parentElement) {
                    toast.style.animation = 'toastSlideOut 0.3s ease';
                    setTimeout(() => toast.remove(), 300);
                }
            }, duration);
        }

        return toast;
    },

    success(message, duration = 4000) {
        return this.show(message, 'success', duration);
    },

    error(message, duration = 5000) {
        return this.show(message, 'error', duration);
    },

    warning(message, duration = 4000) {
        return this.show(message, 'warning', duration);
    },

    info(message, duration = 4000) {
        return this.show(message, 'info', duration);
    }
};

// ═══════════════════════════════════════════════════════════
// 📝 Form — مدیریت فرم‌ها
// ═══════════════════════════════════════════════════════════

const Form = {
    /**
     * جمع‌آوری داده‌های فرم.
     */
    collect(formElement) {
        const formData = new FormData(formElement);
        const data = {};

        for (const [key, value] of formData.entries()) {
            if (value === '') continue;
            data[key] = value;
        }

        return data;
    },

    /**
     * پر کردن فرم با داده.
     */
    fill(formElement, data) {
        Object.keys(data).forEach(key => {
            const input = formElement.elements[key];
            if (!input) return;

            if (input.type === 'checkbox') {
                input.checked = !!data[key];
            } else if (input.type === 'radio') {
                const radio = formElement.querySelector(`[name="${key}"][value="${data[key]}"]`);
                if (radio) radio.checked = true;
            } else {
                input.value = data[key] ?? '';
            }
        });
    },

    /**
     * پاک کردن فرم.
     */
    reset(formElement) {
        formElement.reset();

        // حذف کلاس‌های خطا
        formElement.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
    },

    /**
     * غیرفعال/فعال کردن دکمه.
     */
    toggleSubmit(button, loading = true, loadingText = 'در حال ارسال...') {
        if (loading) {
            button.dataset.originalText = button.textContent;
            button.disabled = true;
            button.textContent = loadingText;
        } else {
            button.disabled = false;
            button.textContent = button.dataset.originalText || 'ارسال';
        }
    },

    /**
     * نمایش خطاهای validation.
     */
    showErrors(formElement, errors) {
        // پاک کردن خطاهای قبلی
        formElement.querySelectorAll('.field-error').forEach(el => el.remove());

        Object.keys(errors).forEach(field => {
            const input = formElement.elements[field];
            if (!input) return;

            input.classList.add('is-invalid');

            const errorEl = document.createElement('div');
            errorEl.className = 'field-error';
            errorEl.style.cssText = 'color: #ef4444; font-size: 12px; margin-top: 4px;';
            errorEl.textContent = errors[field];

            input.parentElement.appendChild(errorEl);
        });
    }
};

// ═══════════════════════════════════════════════════════════
// 📅 Date — تاریخ شمسی
// ═══════════════════════════════════════════════════════════

const DateUtils = {
    /**
     * تبدیل تاریخ میلادی به شمسی.
     * @param {string} isoDate — YYYY-MM-DD
     */
    toJalali(isoDate) {
        if (!isoDate) return '—';

        try {
            const date = new Date(isoDate);
            return new Intl.DateTimeFormat('fa-IR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            }).format(date);
        } catch (e) {
            return isoDate;
        }
    },

    /**
     * تبدیل تاریخ و زمان به شمسی.
     */
    toJalaliDateTime(isoDateTime) {
        if (!isoDateTime) return '—';

        try {
            const date = new Date(isoDateTime);
            return new Intl.DateTimeFormat('fa-IR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            }).format(date);
        } catch (e) {
            return isoDateTime;
        }
    },

    /**
     * تبدیل به فرمت YYYY-MM-DD برای input.
     */
    toISODate(date = new Date()) {
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    },

    /**
     * امروز.
     */
    today() {
        return this.toISODate(new Date());
    },

    /**
     * فردا.
     */
    tomorrow() {
        const d = new Date();
        d.setDate(d.getDate() + 1);
        return this.toISODate(d);
    },

    /**
     * زمان کنونی به فرمت HH:MM.
     */
    currentTime() {
        const d = new Date();
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        return `${hours}:${minutes}`;
    }
};

// ═══════════════════════════════════════════════════════════
// 🛠 Utils — توابع کمکی
// ═══════════════════════════════════════════════════════════

const Utils = {
    /**
     * جلوگیری از XSS.
     */
    escapeHtml(text) {
        if (text == null) return '';
        const div = document.createElement('div');
        div.textContent = String(text);
        return div.innerHTML;
    },

    /**
     * فرمت اعداد با جداکننده هزار.
     */
    formatNumber(num) {
        if (num == null) return '0';
        return new Intl.NumberFormat('fa-IR').format(num);
    },

    /**
     * فرمت مبلغ ریال.
     */
    formatCurrency(amount) {
        if (amount == null) return '۰ ریال';
        return `${this.formatNumber(amount)} ریال`;
    },

    /**
     * کوتاه کردن متن.
     */
    truncate(text, length = 50) {
        if (!text) return '';
        if (text.length <= length) return text;
        return text.substring(0, length) + '...';
    },

    /**
     * تأخیر.
     */
    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    },

    /**
     * Debounce — برای جستجو.
     */
    debounce(func, wait = 300) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    /**
     * تولید UUID.
     */
    uuid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    },

    /**
     * گرفتن پارامتر از URL.
     */
    getQueryParam(name) {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
    },

    /**
     * بررسی شماره موبایل ایرانی.
     */
    isValidIranianPhone(phone) {
        return /^09[0-9]{9}$/.test(phone);
    },

    /**
     * بررسی ایمیل.
     */
    isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    },

    /**
     * کپی به clipboard.
     */
    async copyToClipboard(text) {
        try {
            await navigator.clipboard.writeText(text);
            Toast.success('کپی شد');
            return true;
        } catch (e) {
            Toast.error('خطا در کپی');
            return false;
        }
    },

    /**
     * دانلود فایل.
     */
    downloadFile(content, filename, type = 'text/plain') {
        const blob = new Blob([content], { type });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(url);
    }
};

// ═══════════════════════════════════════════════════════════
// 🎯 App — راه‌اندازی اولیه
// ═══════════════════════════════════════════════════════════

const App = {
    /**
     * راه‌اندازی اولیه.
     */
    init() {
        this.setupGlobalHandlers();
        this.checkAuth();
        this.highlightActiveNav();
        console.log('🏥 Clinic App initialized');
    },

    /**
     * مدیریت خطاهای سراسری.
     */
    setupGlobalHandlers() {
        // مدیریت خطاهای fetch
        window.addEventListener('unhandledrejection', event => {
            console.error('Unhandled promise rejection:', event.reason);
        });
    },

    /**
     * بررسی احراز هویت در صفحات محافظت‌شده.
     */
    checkAuth() {
        const publicPaths = ['/', '/login', '/register', '/403', '/error'];
        const currentPath = window.location.pathname;

        // اگر صفحه عمومی است
        if (publicPaths.some(p => currentPath === p || currentPath.startsWith(p + '/'))) {
            return;
        }

        // اگر قبلاً لاگین کرده و به /login می‌رود
        if (currentPath === '/login' && Auth.isAuthenticated()) {
            window.location.href = '/dashboard';
            return;
        }

        // صفحه محافظت‌شده
        if (!Auth.isAuthenticated()) {
            const returnUrl = encodeURIComponent(currentPath);
            window.location.href = `/login?returnUrl=${returnUrl}`;
        }
    },

    /**
     * هایلایت کردن لینک فعال در منو.
     */
    highlightActiveNav() {
        const currentPath = window.location.pathname;
        document.querySelectorAll('.nav-link').forEach(link => {
            const href = link.getAttribute('href');
            if (href && currentPath.startsWith(href) && href !== '/') {
                link.classList.add('active');
            }
        });
    }
};

// ═══════════════════════════════════════════════════════════
// 🚀 راه‌اندازی خودکار
// ═══════════════════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

// در دسترس قرار دادن در window
window.Auth = Auth;
window.API = API;
window.Toast = Toast;
window.Form = Form;
window.DateUtils = DateUtils;
window.Utils = Utils;
window.App = App;