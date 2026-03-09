/**
 * ============================================
 * 🌱 Photo Share App - API Seed Script (Fixed)
 * ============================================
 */

const axios = require('axios');
const FormData = require('form-data');
const { faker } = require('@faker-js/faker/locale/vi');
const { faker: fakerEn } = require('@faker-js/faker/locale/en');
const fs = require('fs');
const path = require('path');
const https = require('https');
const http = require('http');

// ============================================
// 📌 CONFIGURATION
// ============================================

const CONFIG = {
    apiBaseUrl: process.env.API_URL || 'http://localhost:8080/api/v1',
    totalUsers: 50,
    postsPerUser: { min: 3, max: 7 }, 
    delayBetweenRequests: 100,
    concurrency: 5,
    defaultPassword: 'Password123!',
    tempDir: path.join(__dirname, 'temp_images')
};

const PICSUM_IDS = {
    avatars: [1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 64, 65, 91, 101, 102],
    posts: {
        nature: [10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20],
        city: [1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102],
        food: [292, 312, 326, 429, 431, 452, 488],
        travel: [164, 165, 166, 167, 168, 169, 170],
        aesthetic: [211, 212, 213, 214, 215, 216],
        lifestyle: [301, 302, 303, 304, 305, 306]
    }
};
const CAPTION_TEMPLATES = { nature: ["Thiên nhiên chữa lành 🌿", "Bình yên 🍃"], city: ["City lights 🌃", "Urban vibes 🏙️"], general: ["Just vibing ✨"] };
const COMMENT_TEMPLATES = ["Đẹp quá! 😍", "Tuyệt vời! ✨", "Vibe quá trời! 🌈"];
const TAGS = { general: ['photooftheday', 'instagood', 'beautiful'] };
const BIOS = ["✨ Living life one photo at a time", "📸 Photography enthusiast", "🎨 Creative soul"];

// ============================================
// 🛠️ UTILITY FUNCTIONS
// ============================================
const randomInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;
const randomElement = (arr) => arr[Math.floor(Math.random() * arr.length)];
const randomElements = (arr, count) => [...arr].sort(() => 0.5 - Math.random()).slice(0, Math.min(count, arr.length));
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

function progressBar(current, total, label = '') {
    const percent = Math.round((current / total) * 100);
    const filled = Math.round(percent / 2);
    const bar = '█'.repeat(filled) + '░'.repeat(50 - filled);
    process.stdout.write(`\r${label} [${bar}] ${percent}% (${current}/${total})`);
}

function downloadImage(url, filepath) {
    return new Promise((resolve, reject) => {
        const protocol = url.startsWith('https') ? https : http;
        const request = protocol.get(url, (response) => {
            if (response.statusCode === 301 || response.statusCode === 302) {
                downloadImage(response.headers.location, filepath).then(resolve).catch(reject);
                return;
            }
            if (response.statusCode !== 200) return reject(new Error(`Status: ${response.statusCode}`));
            const file = fs.createWriteStream(filepath);
            response.pipe(file);
            file.on('finish', () => { file.close(); resolve(filepath); });
            file.on('error', (err) => { fs.unlink(filepath, () => {}); reject(err); });
        });
        request.on('error', reject);
        request.setTimeout(15000, () => { request.destroy(); reject(new Error('Timeout')); });
    });
}

// ============================================
// 🌐 API CLIENT
// ============================================

class ApiClient {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
        this.axios = axios.create({
            baseURL: baseUrl,
            timeout: 30000,
            headers: { 'Content-Type': 'application/json' }
        });
    }
    setToken(token) { this.axios.defaults.headers.common['Authorization'] = `Bearer ${token}`; }
    
    async register(username, email, password) {
        return (await this.axios.post('/auth/register', { username, email, password })).data;
    }
    async login(identifier, password) {
        return (await this.axios.post('/auth/login', { identifier, password })).data;
    }
    async updateProfile(bio, imageUrl) {
        return (await this.axios.put('/user/profile', { bio, imageUrl })).data;
    }
    async createPhoto(imagePath, caption, tags) {
        const form = new FormData();
        form.append('image', fs.createReadStream(imagePath));
        form.append('caption', caption);
        if (tags) tags.forEach(tag => form.append('tags', tag));
        return (await this.axios.post('/photos', form, { headers: { ...form.getHeaders() } })).data;
    }
    async follow(userId) { return (await this.axios.post(`/follows/follow/${userId}`)).data; }
    async toggleLike(photoId) { return (await this.axios.post(`/likes/toggle/photo/${photoId}`)).data; }
    async createComment(photoId, text) { 
        return (await this.axios.post(`/comments/photo/${photoId}`, { text, parentCommentId: null, mentionedUsernames: [] })).data; 
    }
}

// ============================================
// 🏗️ SEED FUNCTIONS
// ============================================

async function createUsers(api, count) {
    console.log('\n📦 Creating users...');
    const users = [];
    const errors = [];
    
    for (let i = 0; i < count; i++) {
        const username = `${fakerEn.person.firstName().toLowerCase()}${randomInt(100, 9999)}`;
        const email = `${username}@gmail.com`;
        
        try {
            // 1. Register
            await api.register(username, email, CONFIG.defaultPassword);
            
            // 2. Login
            const loginRes = await api.login(username, CONFIG.defaultPassword);
            
            // Response structure: { data: { jwt, id, username, email, role }, message: "..." }
            const token = loginRes.data?.jwt;
            const userId = loginRes.data?.id;

            if (!token) throw new Error("Không tìm thấy JWT trong response login");
            
            users.push({ id: userId, username, token });
            progressBar(i + 1, count, '   Users');
            await delay(CONFIG.delayBetweenRequests);
        } catch (error) {
            const errMsg = error.response?.data?.message || error.message;
            const errStatus = error.response?.status || 'N/A';
            const errData = JSON.stringify(error.response?.data || {});
            errors.push({ username, error: `[${errStatus}] ${errMsg} - ${errData}` });
            console.log(`\n   ⚠️ Error for ${username}: [${errStatus}] ${errMsg}`);
        }
    }
    
    console.log(`\n   ✅ Created ${users.length} users successfully.`);
    if (errors.length > 0) {
        console.log(`   ❌ Failed: ${errors.length} users. Chi tiết lỗi đầu tiên: ${errors[0].error}`);
    }
    return users;
}

async function createPhotos(api, users) {
    console.log('\n📸 Uploading photos to Cloudinary...');
    if (!fs.existsSync(CONFIG.tempDir)) fs.mkdirSync(CONFIG.tempDir);
    const photos = [];
    
    for (const user of users) {
        api.setToken(user.token);
        const numPhotos = randomInt(CONFIG.postsPerUser.min, CONFIG.postsPerUser.max);
        
        for (let j = 0; j < numPhotos; j++) {
            const category = randomElement(Object.keys(PICSUM_IDS.posts));
            const imgId = randomElement(PICSUM_IDS.posts[category]);
            const url = `https://picsum.photos/id/${imgId}/800/800`;
            const tempFile = path.join(CONFIG.tempDir, `post_${Date.now()}.jpg`);
            
            try {
                await downloadImage(url, tempFile);
                const res = await api.createPhoto(tempFile, randomElement(CAPTION_TEMPLATES[category] || CAPTION_TEMPLATES.general), ["seeded", category]);
                photos.push({ id: res.data.id, userId: user.id });
                fs.unlinkSync(tempFile);
                progressBar(photos.length, users.length * 5, '   Photos'); // Ước lượng
            } catch (e) { /* ignore photo error */ }
        }
    }
    console.log(`\n   ✅ Uploaded ${photos.length} photos.`);
    return photos;
}

async function seed() {
    const api = new ApiClient(CONFIG.apiBaseUrl);
    const startTime = Date.now();

    try {
        const users = await createUsers(api, CONFIG.totalUsers);
        if (users.length === 0) return console.log("❌ Không tạo được user nào, dừng seed.");

        const photos = await createPhotos(api, users);
        
        console.log(`\n🎉 HOÀN THÀNH TRONG ${Math.round((Date.now() - startTime)/1000)}s!`);
    } catch (err) {
        console.error("Fatal:", err.message);
    } finally {
        if (fs.existsSync(CONFIG.tempDir)) fs.rmSync(CONFIG.tempDir, { recursive: true, force: true });
    }
}

seed();