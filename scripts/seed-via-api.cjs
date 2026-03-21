/**
 * ============================================
 * 🌱 Photo Share App - API Seed Script
 * ============================================
 * Creates realistic users with avatars, mutual follows,
 * photo posts, likes, and comments.
 */

const axios = require('axios');
const FormData = require('form-data');
const { faker } = require('@faker-js/faker/locale/vi');
const { faker: fakerEn } = require('@faker-js/faker/locale/en');
const fs = require('fs');
const path = require('path');
const https = require('https');
const http = require('http');
const crypto = require('crypto');

// ============================================
// 📌 CONFIGURATION
// ============================================

const CONFIG = {
    apiBaseUrl: 'https://vibelens.me/api/v1',
    apiBaseUrlLocal: 'http://localhost:8080/api/v1',
    totalUsers: 50,
    postsPerUser: { min: 3, max: 6 },
    followsPerUser: { min: 5, max: 20 },
    mutualFollowChance: 0.6, // 60% chance to follow back
    likesPerUser: { min: 10, max: 30 },
    commentsPerUser: { min: 3, max: 10 },
    delayBetweenRequests: 500,
    concurrency: 1,
    defaultPassword: 'Password123!',
    tempDir: path.join(__dirname, 'temp_images')
};

// Realistic avatar sources (pravatar.cc provides real-looking portrait photos)
const AVATAR_IDS = Array.from({ length: 70 }, (_, i) => i + 1); // pravatar has img 1-70

const PICSUM_IDS = {
    posts: {
        nature: [10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20],
        city: [1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102],
        food: [292, 312, 326, 429, 431, 452, 488],
        travel: [164, 165, 166, 167, 168, 169, 170],
        aesthetic: [211, 212, 213, 214, 215, 216],
        lifestyle: [301, 302, 303, 304, 305, 306]
    }
};

const CAPTION_TEMPLATES = {
    nature: [
        "Thiên nhiên chữa lành 🌿", "Bình yên giữa thiên nhiên 🍃",
        "Khoảnh khắc bình yên 🌸", "Sáng nay đẹp quá trời ☀️",
        "Nature is the best therapy 🌿", "Peaceful morning 🌅"
    ],
    city: [
        "City lights 🌃", "Urban vibes 🏙️",
        "Sài Gòn đêm nay đẹp quá 🌆", "Phố phường náo nhiệt ✨",
        "City never sleeps 🌃", "Downtown vibes 🏙️"
    ],
    food: [
        "Ăn gì đây 😋", "Bữa nay ngon quá 🍜",
        "Foodie life 🍕", "Ăn là ghiền 😍",
        "Homemade cooking 🍳", "Street food Saigon 🍲"
    ],
    travel: [
        "Đi đâu đó thôi ✈️", "Wanderlust 🌍",
        "Travel is my therapy 🗺️", "Khám phá nơi mới 🧳",
        "Adventure awaits 🏔️", "New places, new memories 📸"
    ],
    aesthetic: [
        "Aesthetic vibes ✨", "Mood 🎨",
        "Golden hour ☀️", "Vibes 💫",
        "Living for this aesthetic 🌸", "Soft vibes 🦋"
    ],
    lifestyle: [
        "Just vibing ✨", "Ngày mới tích cực 💪",
        "Good vibes only 🌈", "Living my best life ✨",
        "Everyday moments 📸", "Simple things are the best 🌿"
    ]
};

const COMMENT_TEMPLATES = [
    "Đẹp quá! 😍", "Tuyệt vời! ✨", "Vibe quá trời! 🌈",
    "Ảnh đẹp ghê 📸", "Quá xịn luôn! 🔥", "Love this! ❤️",
    "Wow đẹp dữ 😍", "Chill quá! ☺️", "Nice shot! 👏",
    "Thích quá! 💕", "Xin info nơi này đi 📍", "Lung linh quá! ✨",
    "Siêu đẹp luôn 🤩", "Amazing! 🙌", "So aesthetic 🦋",
    "Quá đỉnh! 🏆", "Ước gì được đi cùng 😢", "Goals! 🎯",
    "Cưng quá đi 🥰", "Chụp bằng gì vậy? 📷"
];

const BIOS = [
    "✨ Living life one photo at a time",
    "📸 Photography enthusiast | Saigon",
    "🎨 Creative soul | Art lover",
    "🌿 Nature lover | Traveler",
    "☕ Coffee addict | Dreamer",
    "🎵 Music & Photography",
    "📍 Exploring Vietnam",
    "💫 Just a girl/guy with a camera",
    "🌏 Travel | Food | Life",
    "🎬 Content creator | Photographer",
    "🌸 Living for beautiful moments",
    "📷 Capturing everyday magic",
    "🏙️ City explorer | Night owl",
    "🍜 Foodie | Sài Gòn life",
    "✈️ Wanderlust | 20+ countries",
    "🎨 Design | Photography | Coffee",
    "🌅 Chasing golden hours",
    "💡 Creative mind | Visual storyteller",
    "🧘 Mindful living | Photography",
    "🎸 Music lover | Photo vibes"
];

const TAGS = {
    nature: ['nature', 'landscape', 'green', 'outdoors', 'peaceful'],
    city: ['city', 'urban', 'architecture', 'street', 'nightlife'],
    food: ['food', 'foodie', 'yummy', 'cooking', 'streetfood'],
    travel: ['travel', 'wanderlust', 'explore', 'adventure', 'vacation'],
    aesthetic: ['aesthetic', 'mood', 'vibes', 'goldenhour', 'art'],
    lifestyle: ['lifestyle', 'daily', 'life', 'instagood', 'photooftheday'],
    general: ['photooftheday', 'instagood', 'beautiful', 'photography', 'saigon']
};

// ============================================
// 🛠️ UTILITY FUNCTIONS
// ============================================
const randomInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;
const randomElement = (arr) => arr[Math.floor(Math.random() * arr.length)];
const randomElements = (arr, count) => [...arr].sort(() => 0.5 - Math.random()).slice(0, Math.min(count, arr.length));
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const buildUniquePostImageUrl = (category, userId, postIndex, attempt = 0) => {
    const nonce = `${Date.now()}_${Math.random().toString(36).slice(2, 10)}_${attempt}`;
    const seed = `${category}_${userId}_${postIndex}_${nonce}`;
    return `https://picsum.photos/seed/${seed}/800/800`;
};

const fileSha1 = (filePath) =>
    crypto.createHash('sha1').update(fs.readFileSync(filePath)).digest('hex');

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
    async updateProfile(bio, imagePath) {
        const form = new FormData();
        if (bio) form.append('bio', bio);
        if (imagePath) form.append('image', fs.createReadStream(imagePath));
        return (await this.axios.put('/users/me', form, { headers: { ...form.getHeaders() } })).data;
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
    console.log('\n👤 Creating users with avatars...');
    if (!fs.existsSync(CONFIG.tempDir)) fs.mkdirSync(CONFIG.tempDir);
    const users = [];
    const errors = [];
    const usedAvatarIds = [];

    for (let i = 0; i < count; i++) {
        const firstName = fakerEn.person.firstName().toLowerCase();
        const username = `${firstName}${randomInt(100, 9999)}`;
        const email = `${username}@gmail.com`;
        const bio = randomElement(BIOS);

        // Pick a unique-ish avatar ID
        let avatarId;
        if (usedAvatarIds.length < AVATAR_IDS.length) {
            do { avatarId = randomElement(AVATAR_IDS); } while (usedAvatarIds.includes(avatarId));
            usedAvatarIds.push(avatarId);
        } else {
            avatarId = randomElement(AVATAR_IDS);
        }

        try {
            // 1. Register
            await api.register(username, email, CONFIG.defaultPassword);

            // 2. Login
            const loginRes = await api.login(username, CONFIG.defaultPassword);
            const token = loginRes.data?.jwt;
            const userId = loginRes.data?.id;
            if (!token) throw new Error("JWT not found in login response");

            api.setToken(token);

            // 3. Download avatar and update profile
            const avatarUrl = `https://i.pravatar.cc/300?img=${avatarId}`;
            const avatarPath = path.join(CONFIG.tempDir, `avatar_${username}.jpg`);
            try {
                await downloadImage(avatarUrl, avatarPath);
                await api.updateProfile(bio, avatarPath);
                fs.unlinkSync(avatarPath);
            } catch (avatarErr) {
                // If avatar fails, still update bio
                try { await api.updateProfile(bio, null); } catch (_) {}
                console.log(`\n   ⚠️ Avatar failed for ${username}, bio still set`);
            }

            users.push({ id: userId, username, token });
            progressBar(i + 1, count, '   Users');
            await delay(CONFIG.delayBetweenRequests);
        } catch (error) {
            const errMsg = error.response?.data?.message || error.message;
            const errStatus = error.response?.status || 'N/A';
            errors.push({ username, error: `[${errStatus}] ${errMsg}` });
            console.log(`\n   ❌ Error for ${username}: [${errStatus}] ${errMsg}`);
        }
    }

    console.log(`\n   ✅ Created ${users.length} users with avatars.`);
    if (errors.length > 0) {
        console.log(`   ❌ Failed: ${errors.length} users`);
    }
    return users;
}

async function createFollows(api, users) {
    console.log('\n🤝 Creating follow relationships...');
    let totalFollows = 0;
    let mutualFollows = 0;
    const followMap = new Map(); // userId -> Set of followedUserIds

    // Initialize follow map
    for (const user of users) {
        followMap.set(user.id, new Set());
    }

    for (let i = 0; i < users.length; i++) {
        const user = users[i];
        api.setToken(user.token);

        // Each user follows a random number of other users
        const numFollows = randomInt(CONFIG.followsPerUser.min, Math.min(CONFIG.followsPerUser.max, users.length - 1));
        const othersToFollow = randomElements(
            users.filter(u => u.id !== user.id),
            numFollows
        );

        for (const target of othersToFollow) {
            if (followMap.get(user.id).has(target.id)) continue; // Already following

            try {
                await api.follow(target.id);
                followMap.get(user.id).add(target.id);
                totalFollows++;
                await delay(200);
            } catch (e) {
                // ignore follow errors (e.g. already following)
            }
        }

        progressBar(i + 1, users.length, '   Follows');
    }

    // Mutual follow pass: if A follows B, B has a chance to follow A back
    console.log('\n   🔄 Creating mutual follows...');
    for (const user of users) {
        const followers = [];
        // Find who follows this user
        for (const [followerId, followingSet] of followMap) {
            if (followingSet.has(user.id)) {
                followers.push(followerId);
            }
        }

        api.setToken(user.token);
        for (const followerId of followers) {
            // If this user doesn't already follow back, maybe follow back
            if (!followMap.get(user.id).has(followerId) && Math.random() < CONFIG.mutualFollowChance) {
                try {
                    await api.follow(followerId);
                    followMap.get(user.id).add(followerId);
                    totalFollows++;
                    mutualFollows++;
                    await delay(200);
                } catch (e) {
                    // ignore
                }
            }
        }
    }

    console.log(`   ✅ Created ${totalFollows} follows (${mutualFollows} mutual follow-backs)`);
}

async function createPhotos(api, users) {
    console.log('\n📸 Uploading photos...');
    if (!fs.existsSync(CONFIG.tempDir)) fs.mkdirSync(CONFIG.tempDir);
    const photos = [];
    const uploadedPhotoHashes = new Set();
    const totalEstimate = users.length * ((CONFIG.postsPerUser.min + CONFIG.postsPerUser.max) / 2);

    for (const user of users) {
        api.setToken(user.token);
        const numPhotos = randomInt(CONFIG.postsPerUser.min, CONFIG.postsPerUser.max);

        for (let j = 0; j < numPhotos; j++) {
            const category = randomElement(Object.keys(PICSUM_IDS.posts));
            let uploaded = false;

            for (let attempt = 0; attempt < 5; attempt++) {
                const url = buildUniquePostImageUrl(category, user.id, j, attempt);
                const tempFile = path.join(CONFIG.tempDir, `post_${user.id}_${j}_${Date.now()}_${attempt}.jpg`);

                try {
                    await downloadImage(url, tempFile);

                    const hash = fileSha1(tempFile);
                    if (uploadedPhotoHashes.has(hash)) {
                        fs.unlinkSync(tempFile);
                        continue;
                    }

                    const caption = randomElement(CAPTION_TEMPLATES[category]);
                    const postTags = [
                        ...randomElements(TAGS[category] || [], 2),
                        ...randomElements(TAGS.general, 2),
                        'seeded'
                    ];
                    const res = await api.createPhoto(tempFile, caption, postTags);
                    photos.push({ id: res.data.id, userId: user.id, category });
                    uploadedPhotoHashes.add(hash);
                    fs.unlinkSync(tempFile);
                    progressBar(photos.length, Math.round(totalEstimate), '   Photos');
                    await delay(CONFIG.delayBetweenRequests);
                    uploaded = true;
                    break;
                } catch (e) {
                    if (fs.existsSync(tempFile)) fs.unlinkSync(tempFile);
                }
            }

            if (!uploaded) {
                console.log(`\n   ⚠️ Skip duplicated/failed image for user ${user.username}, post #${j + 1}`);
            }
        }
    }
    console.log(`\n   ✅ Uploaded ${photos.length} photos.`);
    return photos;
}

async function createLikesAndComments(api, users, photos) {
    console.log('\n❤️ Creating likes and comments...');
    let totalLikes = 0;
    let totalComments = 0;

    for (let i = 0; i < users.length; i++) {
        const user = users[i];
        api.setToken(user.token);

        // Like random photos (not own photos)
        const otherPhotos = photos.filter(p => p.userId !== user.id);
        const numLikes = randomInt(CONFIG.likesPerUser.min, Math.min(CONFIG.likesPerUser.max, otherPhotos.length));
        const photosToLike = randomElements(otherPhotos, numLikes);

        for (const photo of photosToLike) {
            try {
                await api.toggleLike(photo.id);
                totalLikes++;
                await delay(100);
            } catch (e) { /* ignore */ }
        }

        // Comment on random photos
        const numComments = randomInt(CONFIG.commentsPerUser.min, Math.min(CONFIG.commentsPerUser.max, otherPhotos.length));
        const photosToComment = randomElements(otherPhotos, numComments);

        for (const photo of photosToComment) {
            try {
                await api.createComment(photo.id, randomElement(COMMENT_TEMPLATES));
                totalComments++;
                await delay(100);
            } catch (e) { /* ignore */ }
        }

        progressBar(i + 1, users.length, '   Interactions');
    }

    console.log(`\n   ✅ Created ${totalLikes} likes and ${totalComments} comments.`);
}

// ============================================
// 🚀 MAIN SEED
// ============================================

async function seed() {
    const api = new ApiClient(CONFIG.apiBaseUrlLocal);
    const startTime = Date.now();

    console.log('🌱 Starting seed...');
    console.log(`   Target: ${CONFIG.totalUsers} users, ${CONFIG.postsPerUser.min}-${CONFIG.postsPerUser.max} posts each`);
    console.log(`   Follows per user: ${CONFIG.followsPerUser.min}-${CONFIG.followsPerUser.max}, mutual chance: ${CONFIG.mutualFollowChance * 100}%`);

    try {
        // Step 1: Create users with avatars and bios
        const users = await createUsers(api, CONFIG.totalUsers);
        if (users.length === 0) return console.log("❌ No users created, stopping seed.");

        // Step 2: Create follow relationships (including mutual follows)
        await createFollows(api, users);

        // Step 3: Upload photos
        const photos = await createPhotos(api, users);

        // Step 4: Create likes and comments
        if (photos.length > 0) {
            await createLikesAndComments(api, users, photos);
        }

        const duration = Math.round((Date.now() - startTime) / 1000);
        console.log(`\n🎉 SEED COMPLETED IN ${duration}s!`);
        console.log(`   👤 ${users.length} users (with avatars & bios)`);
        console.log(`   📸 ${photos.length} photos`);
        console.log(`   🤝 Follow relationships with ${CONFIG.mutualFollowChance * 100}% mutual rate`);
    } catch (err) {
        console.error("Fatal:", err.message);
    } finally {
        if (fs.existsSync(CONFIG.tempDir)) fs.rmSync(CONFIG.tempDir, { recursive: true, force: true });
    }
}

seed();
