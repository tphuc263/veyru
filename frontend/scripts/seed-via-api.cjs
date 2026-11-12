const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');

const rawUrl = (process.env.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const configuredUrl =
  rawUrl === 'https://veyru.dev' || rawUrl === 'https://www.veyru.dev'
    ? 'https://api.veyru.dev'
    : rawUrl;
const baseUrl = configuredUrl.endsWith('/api/v1') ? configuredUrl : `${configuredUrl}/api/v1`;
const defaultImageDir = path.join(os.homedir(), 'Downloads', 'image-mock-data-veyru');
const imageRootDir = process.env.IMAGE_DIR
  ? path.resolve(process.env.IMAGE_DIR.replace(/^~(?=$|\/|\\)/, os.homedir()))
  : defaultImageDir;
const password = process.env.SEED_PASSWORD || 'Veyru@2026!';
const concurrencyLimit = Number(process.env.SEED_CONCURRENCY || 4);

const CATEGORIES = [
  'coffee_food',
  'fashion_streetwear',
  'pets',
  'tech_setup',
  'travel_nature'
];

// 36 realistic user personas organized in a 3-tier pyramid (10 Top Creators, 14 Active, 12 Casual)
const USER_PERSONAS = [
  // ================= TIER 1: 10 TOP CREATORS =================
  {
    username: 'la.ca.saigon',
    tier: 1,
    primary: 'coffee_food',
    secondary: 'travel_nature',
    bio: '☕ Góc nhỏ lưu giữ những quán cafe đẹp cuối tuần | Specialty Coffee & Pastry 🥐 | 📍 Saigon'
  },
  {
    username: 'coffeeholic.vn',
    tier: 1,
    primary: 'coffee_food',
    secondary: 'tech_setup',
    bio: 'Barista life & slow mornings ✨ | Pour-over enthusiast | Chuyện kể bên tách cà phê mỗi ngày'
  },
  {
    username: 'chii.ootd',
    tier: 1,
    primary: 'fashion_streetwear',
    secondary: 'coffee_food',
    bio: '🖤 Daily fits & visual diary | Streetwear / Minimalism | DM for collab & lookbook 📩'
  },
  {
    username: 'archive.by.tuan',
    tier: 1,
    primary: 'fashion_streetwear',
    secondary: 'travel_nature',
    bio: 'Fashion stylist & film photography 🎞️ | Wearing black 90% of the time | SG - HN'
  },
  {
    username: 'moka.the.corgi',
    tier: 1,
    primary: 'pets',
    secondary: 'coffee_food',
    bio: '🐶 Nhật ký của Moka và những người bạn bốn chân | 100% năng lượng tích cực mỗi ngày 🐾'
  },
  {
    username: 'tiem.meo.nho',
    tier: 1,
    primary: 'pets',
    secondary: 'travel_nature',
    bio: '🐱 Con sen chính hiệu | Lưu giữ mọi khoảnh khắc ngáo ngơ của hoàng thượng ở nhà ✨'
  },
  {
    username: 'minh.khang.dev',
    tier: 1,
    primary: 'tech_setup',
    secondary: 'coffee_food',
    bio: '💻 Software Engineer | Minimalist Desk Setup & Custom Keyboards ⌨️ | Building cool stuff'
  },
  {
    username: 'desk.by.huy',
    tier: 1,
    primary: 'tech_setup',
    secondary: 'coffee_food',
    bio: 'Workspace inspiration & tech gear review 🖥️ | Late night coding sessions ☕ | 📍 HCMC'
  },
  {
    username: 'linh.wanderlust',
    tier: 1,
    primary: 'travel_nature',
    secondary: 'coffee_food',
    bio: '🌿 Chữa lành ở những vùng đất mới | Chasing sunsets & mountain breeze 🏔️ | Fujifilm XT-4'
  },
  {
    username: 'nam.ontheroad',
    tier: 1,
    primary: 'travel_nature',
    secondary: 'fashion_streetwear',
    bio: '✈️ Travel photographer | Đi để thấy mình còn trẻ | Việt Nam qua lăng kính nhỏ 📸'
  },

  // ================= TIER 2: 14 ACTIVE USERS =================
  {
    username: 'thao.vy_',
    tier: 2,
    primary: 'coffee_food',
    secondary: 'fashion_streetwear',
    bio: 'Matcha latte & bánh sừng bò qua ngày 🍵🥐 | Lifestyle & little things'
  },
  {
    username: 'bep.cua.may',
    tier: 2,
    primary: 'coffee_food',
    secondary: 'pets',
    bio: 'Ăn ngon mặc đẹp, yêu đời mỗi sớm mai 🍳✨ | Foodie in Saigon'
  },
  {
    username: 'hoang.nam.99',
    tier: 2,
    primary: 'fashion_streetwear',
    secondary: 'coffee_food',
    bio: 'Vintage & streetwear collector 👟 | Good vibes only'
  },
  {
    username: 'ngoc.han.studio',
    tier: 2,
    primary: 'fashion_streetwear',
    secondary: 'travel_nature',
    bio: 'Graphic Designer 🎨 | Aesthetic seeker | Less is more 🤍'
  },
  {
    username: 'kenji.street',
    tier: 2,
    primary: 'fashion_streetwear',
    secondary: 'tech_setup',
    bio: 'Sneakerhead & urban explorer 🏙️ | Capturing city lights'
  },
  {
    username: 'linh.dan.fits',
    tier: 2,
    primary: 'fashion_streetwear',
    secondary: 'coffee_food',
    bio: 'OOTD & Cafe hopping 🎀 | Lưu giữ thanh xuân qua từng khung hình'
  },
  {
    username: 'boss.nha.bau',
    tier: 2,
    primary: 'pets',
    secondary: 'coffee_food',
    bio: '🐾 Nhà có hai chiếc đuôi nhỏ luôn vẫy chào mỗi chiều đi làm về 🐕'
  },
  {
    username: 'meo.luoi.official',
    tier: 2,
    primary: 'pets',
    secondary: 'coffee_food',
    bio: 'Ngủ 18 tiếng một ngày, 6 tiếng còn lại đòi ăn pate 🐟🐾'
  },
  {
    username: 'paws.and.chill',
    tier: 2,
    primary: 'pets',
    secondary: 'travel_nature',
    bio: 'Đưa boss đi khắp thế gian 🐶🌿 | Pet friendly spots reviewer'
  },
  {
    username: 'sunny.petdiary',
    tier: 2,
    primary: 'pets',
    secondary: 'fashion_streetwear',
    bio: 'Một chút đáng yêu chữa lành ngày dài của bạn 🧸🐾'
  },
  {
    username: 'code.with.dat',
    tier: 2,
    primary: 'tech_setup',
    secondary: 'travel_nature',
    bio: 'Fullstack Developer 🚀 | Clean code & clean desk | Coffee powered ☕'
  },
  {
    username: 'ha.my.travels',
    tier: 2,
    primary: 'travel_nature',
    secondary: 'coffee_food',
    bio: '🌊 Biển xanh, cát trắng và những chuyến đi ngẫu hứng cùng hội bạn thân'
  },
  {
    username: 'dalat.dreamer',
    tier: 2,
    primary: 'travel_nature',
    secondary: 'coffee_food',
    bio: '🌲 Phải lòng sương mù và những con dốc nhỏ | Slow living'
  },
  {
    username: 'khoa.analog',
    tier: 2,
    primary: 'travel_nature',
    secondary: 'fashion_streetwear',
    bio: '🎞️ 35mm film diary | Lưu lại những ngày nắng đẹp trên khắp mọi miền'
  },

  // ================= TIER 3: 12 CASUAL USERS =================
  {
    username: 'an.nhien_',
    tier: 3,
    primary: 'coffee_food',
    secondary: 'travel_nature',
    bio: 'Những điều bình dị nhỏ bé mỗi ngày 🌱'
  },
  {
    username: 'quynh.huong.01',
    tier: 3,
    primary: 'coffee_food',
    secondary: 'pets',
    bio: 'Sài Gòn những ngày mưa và một tách trà ấm 🍵'
  },
  {
    username: 'duc.anh.le',
    tier: 3,
    primary: 'fashion_streetwear',
    secondary: 'travel_nature',
    bio: 'Just living my best life ⚡ | HCMC'
  },
  {
    username: 'my.linh.vo',
    tier: 3,
    primary: 'fashion_streetwear',
    secondary: 'coffee_food',
    bio: '✨ Keep shining | Life in plastic, it is fantastic'
  },
  {
    username: 'gia.bao.tran',
    tier: 3,
    primary: 'fashion_streetwear',
    secondary: 'pets',
    bio: 'Weekend warrior 🏀 | Photography amateur'
  },
  {
    username: 'phuong.nghi_',
    tier: 3,
    primary: 'pets',
    secondary: 'coffee_food',
    bio: 'Yêu chó mèo và những buổi chiều lộng gió 🐾🍃'
  },
  {
    username: 'khanh.vy.pet',
    tier: 3,
    primary: 'pets',
    secondary: 'travel_nature',
    bio: 'Adopt, do not shop 🐶❤️ | Сollecting happy moments'
  },
  {
    username: 'binh.minh.98',
    tier: 3,
    primary: 'pets',
    secondary: 'coffee_food',
    bio: 'Cuộc sống của một người hướng nội thích chơi với mèo 🐈'
  },
  {
    username: 'tuan.kiet.it',
    tier: 3,
    primary: 'tech_setup',
    secondary: 'coffee_food',
    bio: 'DevOps & Cloud ☁️ | Work hard, chill harder'
  },
  {
    username: 'minh.triet.fe',
    tier: 3,
    primary: 'tech_setup',
    secondary: 'fashion_streetwear',
    bio: 'Frontend craftsman ✨ | UI/UX & Mechanical Keyboards'
  },
  {
    username: 'thanh.truc.wander',
    tier: 3,
    primary: 'travel_nature',
    secondary: 'coffee_food',
    bio: 'Đi thật xa để trở về 🌤️ | Nature lover'
  },
  {
    username: 'hai.dang.photo',
    tier: 3,
    primary: 'travel_nature',
    secondary: 'pets',
    bio: 'Bắt trọn từng khoảnh khắc hoàng hôn 🌅'
  }
];

const CAPTION_POOL = {
  coffee_food: [
    'Khởi đầu ngày mới bằng một tách flat white thơm lừng ☕✨',
    'Cuối tuần trốn phố tìm một góc quán nhỏ yên tĩnh đọc nốt cuốn sách dở 📖☕',
    'Mùi cà phê mới xay buổi sáng đúng là liều thuốc tinh thần đỉnh nhất 🥐🤍',
    'Slow morning. Không vội vã, chỉ có nắng sớm và hương cà phê quen thuộc 🌤️',
    'Góc quán quen mỗi chiều chủ nhật. Ngồi đây ngắm đường phố thôi cũng thấy bình yên 🌿',
    'Bánh sừng bò mới ra lò giòn rụm kết hợp cùng cold brew cam sả — 10 điểm không có nhưng! 🥐🍊',
    'Hạnh phúc đôi khi chỉ đơn giản là tìm được một quán hợp gu từ nhạc đến nước uống 🎶☕',
    'Chiều mưa Sài Gòn mà có ly cacao nóng này thì còn gì bằng 🌧️🍫',
    'Caffeine loading... Chuẩn bị năng lượng chạy deadline xuyên màn đêm 💻☕',
    'Một chút ngọt ngào cho ngày giữa tuần bớt căng thẳng 🍰🍓',
    'Tiệm cà phê nấp trong con hẻm nhỏ mà vibe ấm cúng cực kỳ, chắc chắn sẽ quay lại nhiều lần ✨',
    'Sunday brunch cùng hội bạn thân sau cả tháng trời ai cũng bận rộn 🥞🥑',
    'Pour-over hạt Ethiopia hôm nay lên hương hoa quả rõ rệt, uống một ngụm tỉnh cả người ☕🍒',
    'Chữa lành tâm hồn bằng đồ ăn ngon và những cuộc trò chuyện không hồi kết 🍽️💛',
    'Ánh nắng chiếu qua khung cửa sổ quán lúc 9 giờ sáng đẹp như một thước phim điện ảnh 🎞️☕',
    'Tự thưởng cho bản thân một bữa sáng thật chỉnh chu trước khi bắt đầu tuần mới 🍳🥖',
    'Ai bảo đi cà phê một mình là buồn? Ngồi chill nghe nhạc một mình hơi bị nghiện đấy 🎧☕',
    'Matcha latte ít ngọt cho một buổi chiều dịu dàng 🍵🌿',
    'Món tráng miệng xinh xắn đến mức ngắm mãi không nỡ ăn 🍮✨',
    'Cà phê ngon nhất là cà phê uống cùng người hợp chuyện ☕💬',
    'Hẹn hò cuối tuần ở một không gian ngập tràn mùi bơ bánh nướng thơm phức 🧁🤍',
    'Thử một quán mới mở, ấn tượng đầu tiên là decor gỗ tối giản siêu ấm áp 🪵☕',
    'Mood hôm nay: Cần 200% lượng đường và caffeine để vượt qua ngày thứ Hai 🧋⚡',
    'Góc nhỏ bình yên giữa lòng thành phố nhộn nhịp 🏙️☕',
    'Một chiều thu dịu mát, ngồi ban công nhâm nhi tách trà bá tước 🍂🫖',
    'Đi làm cả tuần chỉ mong tới thứ Bảy để đi la cà hết các quán đẹp quanh Sài Gòn 🛵☕',
    'Bữa tối ấm cúng tự tay chuẩn bị sau một ngày dài bận rộn 🍝🕯️',
    'Vị đắng nhẹ đầu lưỡi nhưng hậu vị ngọt thanh — giống hệt những ngày trưởng thành ☕✨',
    'Lại phát hiện thêm một hidden gem cho hội mê đồ ngọt và không gian yên tĩnh 🍰🤫',
    'Sáng nào cũng phải làm một ly espresso đá mới bắt đầu làm việc nổi 🧊☕'
  ],
  fashion_streetwear: [
    'Fit check cho một buổi chiều dạo phố 🖤✨',
    'Less is more. Cứ tone đen trắng tối giản là tự tin bước ra đường 👟🤍',
    'Mọi người chấm outfit hôm nay mấy điểm đây? 👀🔥',
    'Streetwear vibes. Phối đồ ngẫu hứng mà lên hình ưng quá chừng 📸🖤',
    'Golden hour + chiếc áo khoác yêu thích = combo bất bại 🌅🧥',
    'Đơn giản nhưng không đơn điệu. Một chút nhấn nhá ở phụ kiện cho ngày cuối tuần 🕶️⛓️',
    'Lâu lâu đổi gió sang style bụi bặm một chút nhìn cũng ra gì phết 🛹⚡',
    'All black everything. Tủ đồ có 10 món thì hết 8 món màu đen rồi 🖤🐈‍⬛',
    'Ánh sáng ở góc phố này đỉnh thật sự, giơ máy lên là có ảnh đẹp ngay 🏙️📸',
    'Casual Friday. Mặc thoải mái nhất để tan làm là đi cháy phố luôn 👟✨',
    'Mùa này diện layering là chuẩn bài nhất. Vừa ấm vừa lên form cực nét 🧥🍂',
    'Đôi sneaker cày cuốc suốt 2 năm trời vẫn là đôi giày phối đồ hợp nhất 👟🔥',
    'Vibe đường phố những ngày cuối tuần. Nhịp sống vừa đủ nhanh, vừa đủ chất 🎞️🖤',
    'Confidence is the best outfit you can wear ✨',
    'Một chút hoài cổ với chiếc áo vintage săn được ở chợ đồ si tuần trước 🕰️👕',
    'Chiều chủ nhật thảnh thơi, lên đồ đi dạo vòng quanh trung tâm thành phố 🚶‍♂️🏙️',
    'Lookbook ngẫu hứng được chụp bởi đứa bạn thân có tâm nhất hệ mặt trời 📸🙌',
    'Không chạy theo xu hướng, chỉ mặc những gì khiến bản thân thấy thoải mái nhất 🤍✨',
    'Oversized tee + cargo pants: Công thức cứu cánh cho mọi buổi sáng lười nghĩ đồ 👖👟',
    'Góc nghiêng thần thánh dưới ánh đèn đường lúc chập tối 🌃⚡',
    'Màu ảnh film cộng thêm set đồ tone trầm nhìn cuốn không rời mắt được 🎞️🤎',
    'New pickup! Cuối cùng cũng săn được món đồ ưng ý sau bao ngày chờ restock 🛍️🔥',
    'Moody portrait. Đôi khi một bức ảnh không cần cười lại có chiều sâu nhất 🖤📸',
    'Chuẩn bị lên đồ đi cafe mà chụp ở hành lang chung cư đã hết nửa dung lượng máy 😂📱',
    'Tối giản trong từng đường cắt may. Đầu tư vào chất liệu không bao giờ là lỗ 🧵🤍',
    'Nắng chiều Sài Gòn làm bộ trang phục hôm nay thêm phần rực rỡ ☀️✨',
    'Urban explorer. Mỗi con hẻm nhỏ đều là một sàn diễn nếu bạn biết chọn góc máy 🏙️👟',
    'Thử nghiệm bảng màu trung tính cho tủ đồ thu đông năm nay 🍂🧥',
    'Vừa ngầu vừa thoải mái — tiêu chí chọn đồ đi chơi cuối tuần của mình 🕶️⚡',
    'Giữ cho tinh thần luôn trẻ trung như chính phong cách mình theo đuổi mỗi ngày 🔥✨'
  ],
  pets: [
    'Gương mặt mỗi khi nghe tiếng xé gói súp thưởng từ cách đó 3 phòng 🐶👀',
    'Ngủ nguyên ngày không biết mệt mà cứ tới 2 giờ sáng là chạy parkour khắp nhà 🐱💨',
    'Một cục bông tròn xoe sưởi ấm cả buổi chiều mưa 🐾🤍',
    'Đi làm về mệt cỡ nào chỉ cần nhìn thấy chiếc đuôi vẫy rối rít này là tan biến hết 🐕💛',
    'Góc nghiêng suy tư của hoàng thượng khi nhìn chim bay ngoài cửa sổ 🐱🌿',
    '100% năng lượng tích cực được gói gọn trong bức ảnh này 🐶✨',
    'Xin giới thiệu nhân viên giám sát chất lượng giấc ngủ chăm chỉ nhất nhà 🐾😴',
    'Đưa boss đi dạo công viên cuối tuần, chạy một vòng mà lưỡi dài tới đất 🐕🌱',
    'Ánh mắt ngây thơ vô số tội sau khi vừa cắn rách đôi dép mới mua của sen 🥺🐶',
    'Chữa lành mọi tâm hồn đang mệt mỏi bằng chiếc mũi ướt nhẹp đáng yêu này 🐾💕',
    'Nắng sớm chiếu lên bộ lông mềm mượt nhìn chỉ muốn ôm vào lòng mãi thôi ☀️🐱',
    'Tư thế ngủ kỳ lạ nhất hệ mặt trời nhưng vẫn nhất quyết không chịu đổi chỗ 😂🐾',
    'Bạn nhỏ đồng hành cùng mình qua biết bao mùa nắng mưa 🐶🤍',
    'Khi bạn cố chụp một tấm hình nghiêm túc nhưng mẫu ảnh chỉ quan tâm tới đồ ăn 🦴📸',
    'Đôi mắt tròn xoe long lanh thế này thì ai mà giận cho nổi cơ chứ 🥺🐱',
    'Cuối tuần không đi đâu xa, chỉ ở nhà nằm dài cùng cục cưng này là đủ hạnh phúc 🛋️🐾',
    'Khoảnh khắc bình yên nhất trong ngày: Boss nằm gọn trong lòng nghe nhạc chill 🎶🐱',
    'Mới tắm xong thơm phức nên phải lên hình khoe ngay với các cô chú 🛁🐶✨',
    'Biểu cảm mỗi sáng thứ Hai khi thấy sen xách balo chuẩn bị đi làm 😿🐾',
    'Nhỏ nhắn, xinh xắn và chuyên gia chiếm trọn nửa cái giường mỗi đêm 🛏️🐶',
    'Chạy nhảy mệt nhoài trên bãi cỏ xanh mướt rồi nằm thở phì phò 🌿🐕',
    'Gương mặt đại diện cho sự lười biếng nhưng vẫn được cưng chiều nhất nhà 👑🐱',
    'Mỗi ngày có thêm một người bạn bốn chân là mỗi ngày ngập tràn tiếng cười 🐾☀️',
    'Bắt quả tang đang lén trèo lên bàn làm việc để nằm đè lên bàn phím 💻🐱',
    'Chiếc tai cụp thương hiệu không lẫn vào đâu được 🐶💛',
    'Trời lạnh rồi, ai chưa có người yêu thì ôm boss cho ấm nha 🧣🐾',
    'Nụ cười tỏa nắng làm tan chảy cả những trái tim khó tính nhất 🐕✨',
    'Hoàng thượng hôm nay rất hợp tác cho sen chụp hẳn một bộ lookbook xịn sò 📸🐱',
    'Dù thế giới ngoài kia có ồn ào thế nào, về nhà vẫn luôn có một ánh mắt chờ đợi 🏠🐾',
    'Chỉ cần một cái chạm tay nhỏ xíu cũng đủ thấy lòng dịu lại biết bao nhiêu 🤍🐶'
  ],
  tech_setup: [
    'Nâng cấp nhẹ góc làm việc cho tuần mới chạy deadline mượt mà hơn 💻✨',
    'Clean desk, clear mind. Giữ bàn làm việc gọn gàng giúp mình tập trung gấp đôi 🖥️🌿',
    'Late night coding session cùng ánh đèn warm light dịu mắt 🌙⌨️',
    'Chiếc bàn phím cơ custom vừa build xong, gõ tới đâu nghe tiếng thocky sướng tai tới đó ⌨️🔥',
    'Góc nhỏ nơi những dòng code và ý tưởng mới ra đời mỗi ngày 🚀💻',
    'Minimalist setup. Cắt giảm tối đa dây nhợ thừa để không gian thoáng đãng nhất 🖥️🤍',
    'Combo bất bại của dân lập trình: Màn hình ultrawide + cà phê đặc + nhạc Lofi 🎧☕',
    'Ánh sáng tự nhiên chiếu vào góc bàn làm việc mỗi buổi sáng nhìn mê thực sự 🌤️💻',
    'Đầu tư cho góc làm việc tại nhà là khoản đầu tư xứng đáng nhất năm qua 🪑🖥️',
    'Sunday refactor: Dọn lại cả codebase lẫn góc bàn làm việc cho thật ngăn nắp ✨⌨️',
    'Dark mode từ trong IDE ra tới ngoài đời thực 🖤🖥️',
    'Một chút cây xanh trên bàn làm việc để cân bằng lại sau hàng giờ nhìn màn hình 🌱💻',
    'Setup gọn nhẹ cho những ngày làm việc remote tại nhà ⚡🏠',
    'Mỗi món đồ trên chiếc bàn này đều được chọn lựa kỹ lưỡng sau nhiều tháng tìm hiểu 🖱️⌨️',
    'Đèn màn hình + подсветка ambient nhẹ nhàng giúp làm việc đêm không bị mỏi mắt 💡🖥️',
    'Góc chiến thần cày dự án cá nhân mỗi tối sau giờ hành chính 🚀🔥',
    'Sự kết hợp hoàn hảo giữa chất liệu gỗ ấm áp và đồ công nghệ hiện đại 🪵💻',
    'Bàn phím nhỏ gọn 65% giúp tiết kiệm diện tích mặt bàn đáng kể ⌨️✨',
    'Khi đam mê công nghệ gặp gỡ phong cách sống tối giản 🤍🖥️',
    'Năng suất làm việc tăng hẳn từ ngày chuyển sang dùng màn hình phụ dọc để đọc tài liệu 📄💻'
  ],
  travel_nature: [
    'Hít một hơi thật sâu bầu không khí trong lành của núi rừng buổi sớm 🏔️🌿',
    'Đi tới nơi không có khói bụi thành phố, chỉ có tiếng gió và mây trời ☁️✨',
    'Hoàng hôn trên biển luôn có một sức hút kỳ lạ khiến người ta quên hết muộn phiền 🌅🌊',
    'Chuyến đi ngẫu hứng không lên kế hoạch trước lại mang về những kỷ niệm đẹp nhất 🎒✈️',
    'Lạc bước giữa thiên nhiên hùng vĩ mới thấy mình nhỏ bé biết nhường nào 🌲⛰️',
    'Đà Lạt những ngày sương mù giăng kín lối, lạnh nhưng mà tình vô cùng 🌫️🌲',
    'Thu trọn cả bầu trời bình yên vào trong một khung hình nhỏ 📸🌤️',
    'Vitamin Sea cho tâm hồn đang cần được sạc lại năng lượng sau những ngày làm việc căng thẳng 🌊🐚',
    'Con đường nhỏ dẫn lối ra biển ngập tràn nắng vàng rực rỡ ☀️🌴',
    'Thức dậy ở một nơi xa, mở cửa sổ ra là thấy mây trôi ngang tầm mắt ☁️🏡',
    'Mỗi chuyến đi là một trang nhật ký sống động nhất của tuổi trẻ 🎞️✈️',
    'Tiếng sóng vỗ rì rào bên bờ đá như bản nhạc chữa lành dịu êm nhất 🌊🎶',
    'Chinh phục đỉnh đồi lúc 5 giờ sáng để kịp đón những tia nắng đầu tiên trong ngày 🌄🥾',
    'Màu xanh của rừng cây và mặt hồ phẳng lặng làm dịu đi mọi bộn bề 🌲🏞️',
    'Đôi chân còn khỏe là còn muốn đi khám phá hết những cung đường đẹp nhất Việt Nam 🛵🇻🇳',
    'Chiều buông trên vịnh nhỏ, mặt nước lấp lánh như dát vàng ✨🌅',
    'Tạm gác lại email và thông báo công việc để hòa mình vào thiên nhiên vài ngày 🌿📴',
    'Khung cảnh đẹp như tranh vẽ khiến cả nhóm đứng ngắm mãi không muốn về 🎨⛰️',
    'Gió biển mặn mòi và bầu trời trong vắt không một gợn mây 🌊💙',
    'Những cung đường đèo uốn lượn quanh co giữa đại ngàn xanh thẳm 🛣️🌲',
    'Bình yên không ở đâu xa, bình yên nằm ở khoảnh khắc ta biết chậm lại ngắm nhìn thiên nhiên 🍃✨',
    'Nắng chiều nhuộm cam cả một góc trời nơi miền đất lạ 🌇✈️',
    'Lưu giữ lại chút hơi thở của rừng già và tiếng suối chảy róc rách 🏞️🌱',
    'Chuyến camping cuối tuần dưới bầu trời đầy sao không thể nào quên ⛺🌌',
    'Cảm giác đứng trên cao phóng tầm mắt ra biển lớn thật sự tự do vô cùng 🌊🦅',
    'Mùa đẹp nhất trong năm để xách balo lên và đi tới những miền đất mới 🎒🌤️',
    'Một góc nhỏ tĩnh lặng nơi thời gian như ngừng trôi ⏳🌿',
    'Đi thật xa để thấy thế giới này rộng lớn và tươi đẹp biết bao 🌍✨',
    'Sương sớm còn đọng trên tán lá thông, se lạnh đúng chất vùng cao 🌲❄️',
    'Hẹn gặp lại vùng đất đầy nắng và gió này vào một ngày gần nhất nhé ☀️🌊'
  ]
};

const TAG_POOL = {
  coffee_food: ['coffee', 'saigoncafe', 'cafehopping', 'foodlover', 'morningvibes', 'specialtycoffee', 'weekendchill', 'pastry'],
  fashion_streetwear: ['ootd', 'streetwear', 'outfitoftheday', 'minimalstyle', 'fashion', 'dailyfits', 'streetstyle', 'visuals'],
  pets: ['petlover', 'cutepets', 'dogsofinstagram', 'catsofinstagram', 'paws', 'dailyfluff', 'petdiary', 'healing'],
  tech_setup: ['desksetup', 'workspace', 'minimalsetup', 'codinglife', 'developer', 'mechkeys', 'techgear', 'workstation'],
  travel_nature: ['travel', 'wanderlust', 'nature', 'vietnam', 'sunset', 'explore', 'landscape', 'healingvibes']
};

const COMMENT_CONVERSATIONS = {
  coffee_food: [
    { q: 'Quán này ở đâu vậy bạn ơi? Nhìn không gian mê quá 😍', a: 'Ở khu Thảo Điền Quận 2 nha @{user}, buổi sáng nắng đẹp lắm luôn!' },
    { q: 'Màu ảnh ấm áp thực sự, nhìn là thèm đi cafe liền ☕✨', a: 'Cảm ơn @{user} nha, cuối tuần rảnh ghé thử đi bạn, nước uống ổn áp lắm!' },
    { q: 'Chiếc bánh nhìn ngon xuất sắc luôn á 🥐🔥', a: 'Vỏ giòn rụm mà thơm mùi bơ cực kỳ luôn @{user} ơi!' },
    { q: 'Góc này ngồi chạy deadline hay đọc sách đều hết nước chấm 📖☕', a: 'Chuẩn luôn @{user}, nhạc ở quán cũng nhẹ nhàng dễ tập trung nữa!' },
    { q: 'Cho mình xin tên món nước trong hình với ạ 👀', a: 'Mình gọi Cold Brew cam sả nha @{user}, uống thanh mát cực!' }
  ],
  fashion_streetwear: [
    { q: 'Outfit hôm nay cháy quá bạn ơi 🔥🖤', a: 'Cảm ơn @{user} nhiều nha, phối nhanh gọn đi dạo phố ấy mà!' },
    { q: 'Xin info chiếc áo khoác với ạ, form lên đẹp thực sự 🧥✨', a: 'Áo này mình săn ở local brand tuần trước nha @{user}, để mình nhắn link cho!' },
    { q: 'Tone màu cùng góc chụp đỉnh không chỗ chê luôn 📸👏', a: 'Nhờ nắng chiều hôm đó đẹp sẵn đó @{user} ơi 😄' },
    { q: '10 điểm thanh lịch mà vẫn cực kỳ cá tính luôn 💯', a: 'Quá khen rồi @{user} ơi, cảm ơn bạn nhiều nhé 🤍' },
    { q: 'Đôi giày phối với quần này hợp dã man 👟⚡', a: 'Đôi này êm chân cực kỳ, mình mang đi bộ cả ngày không mỏi luôn @{user}!' }
  ],
  pets: [
    { q: 'Trời ơi nhìn cái mặt cưng xỉu luôn á 🥺🐾', a: 'Ở nhà quậy số một luôn đó @{user} ơi, lên hình mới giả bộ ngoan thôi 😂' },
    { q: 'Bé nhà bạn bao nhiêu tháng rồi? Nhìn bụ bẫm đáng yêu quá 🐶💛', a: 'Bé vừa tròn 1 tuổi tuần trước đó @{user}, trộm vía ăn ngủ khỏe lắm!' },
    { q: 'Muốn nựng hai cái má phúng phính kia quá đi mất 😭💕', a: 'Ai qua nhà chơi cũng bị dụ bởi cái mặt này hết trơn @{user} ạ!' },
    { q: 'Nhìn ảnh này xong tự nhiên thấy hết mệt mỏi cả ngày dài luôn ✨', a: 'Gửi ngàn năng lượng tích cực từ boss tới @{user} nha 🐾🤍' },
    { q: 'Đôi mắt tròn xoe nhìn sen đòi ăn không lệch đi đâu được 👀🦴', a: 'Cứ nghe tiếng mở tủ lạnh là có mặt trong vòng 1 giây @{user} ơi 🤣' }
  ],
  tech_setup: [
    { q: 'Góc setup gọn gàng và tối giản đỉnh quá bác ơi 🖥️🔥', a: 'Cảm ơn @{user} nhé, mình mất cả buổi chiều đi lại dây bên dưới bàn đó!' },
    { q: 'Bàn phím dùng switch gì mà nhìn build chất vậy bạn? ⌨️✨', a: 'Mình đang lắp Vertex V1 lubed sẵn nha @{user}, âm trầm ấm cực thích!' },
    { q: 'Xin mã màn hình với đèn treo màn hình được không bác? 👀', a: 'Màn Dell Ultrasharp 27 inch kèm đèn BenQ ScreenBar nha @{user}!' },
    { q: 'Ngồi góc này code chắc năng suất tăng gấp đôi mất 🚀💻', a: 'Cũng tùy hôm nha @{user}, có hôm ngồi ngắm bàn xong lười code luôn 😂' },
    { q: 'Ánh sáng ambient phía sau dịu mắt thật sự 🌙👏', a: 'Bậtไฟ vàng nhẹ lúc làm đêm đỡ mỏi mắt hẳn luôn @{user} ạ!' }
  ],
  travel_nature: [
    { q: 'Cảnh đẹp như tranh vẽ vậy, bạn chụp ở đâu thế? 🏔️😍', a: 'Chuyến đi Đà Lạt - săn mây tuần vừa rồi của tụi mình đó @{user} ơi!' },
    { q: 'Màu ảnh trong trẻo thực sự, bạn chụp bằng máy gì vậy ạ? 📸✨', a: 'Mình chụp bằng Fujifilm XT-4 kèm giả lập màu Classic Chrome nha @{user}!' },
    { q: 'Nhìn ảnh mà chỉ muốn xách balo lên đi trốn phố ngay lập tức 🎒🌊', a: 'Làm một chuyến liền đi @{user}, thời tiết mùa này đang đẹp cực kỳ!' },
    { q: 'Hoàng hôn đỉnh quá, canh được khoảnh khắc này quý giá thật 🌅🧡', a: 'Đứng đợi hơn 40 phút mới bắt được đúng lúc mặt trời xuống đó @{user}!' },
    { q: 'Bình yên quá đỗi, đúng chuẩn đi chữa lành luôn 🌿🤍', a: 'Về lại thành phố rồi mà hồn vẫn còn vương vấn ở trên đó @{user} ơi!' }
  ]
};

const SHARE_CAPTIONS = [
  'Save lại liền để cuối tuần này rủ hội bạn đi thử mới được 😍✨',
  'Vibe đỉnh thực sự, đúng kiểu mình đang tìm kiếm bấy lâu nay 🤍',
  'Chia sẻ chút năng lượng tích cực cho bảng tin ngày hôm nay 🌿✨',
  'Góc chụp và màu sắc xuất sắc quá, phải lưu về học hỏi ngay 📸🔥',
  'Nhìn mà mê mẩn không lối thoát luôn mọi người ơi 💯',
  'Cảm hứng cho tuần mới tràn đầy năng lượng là đây chứ đâu ⚡🤍'
];

class Session {
  constructor() {
    this.cookies = new Map();
    this.csrf = null;
    this.credentials = null;
    this.reauthPromise = null;
  }

  setCredentials(identifier, pwd) {
    this.credentials = { identifier, password: pwd };
  }

  async reauthenticate() {
    if (!this.credentials) return;
    if (!this.reauthPromise) {
      this.reauthPromise = (async () => {
        this.csrf = (await this.request('/csrf', {}, 1, true)).token;
        await this.request(
          '/sessions',
          {
            method: 'POST',
            headers: { 'content-type': 'application/json' },
            body: JSON.stringify(this.credentials)
          },
          1,
          true
        );
      })().finally(() => {
        this.reauthPromise = null;
      });
    }
    return this.reauthPromise;
  }

  async request(endpoint, options = {}, retries = 2, isInternalAuth = false) {
    const headers = new Headers(options.headers);
    if (this.cookies.size) {
      headers.set('cookie', [...this.cookies].map(([key, value]) => `${key}=${value}`).join('; '));
    }
    if (this.csrf && !['GET', 'HEAD'].includes(options.method || 'GET')) {
      headers.set('X-XSRF-TOKEN', this.csrf);
    }

    try {
      const response = await fetch(`${baseUrl}${endpoint}`, { ...options, headers });
      const setCookies = response.headers.getSetCookie?.() || [];
      for (const value of setCookies) {
        const [pair] = value.split(';');
        const separator = pair.indexOf('=');
        const name = pair.slice(0, separator).trim();
        const val = pair.slice(separator + 1).trim();
        if (val) {
          this.cookies.set(name, val);
          if (name === 'XSRF-TOKEN') {
            this.csrf = val;
          }
        }
      }
      const text = await response.text();
      if (response.status === 401 && !isInternalAuth && this.credentials && endpoint !== '/sessions') {
        await this.reauthenticate();
        return this.request(endpoint, options, retries, true);
      }
      if (!response.ok) {
        throw new Error(`${options.method || 'GET'} ${baseUrl}${endpoint} -> ${response.status}: ${text.slice(0, 300)}`);
      }
      if (!text) return null;
      try {
        return JSON.parse(text);
      } catch {
        throw new Error(
          `Expected JSON from ${baseUrl}${endpoint} (${response.status}), but received HTML/text: ${text.slice(0, 120)}`
        );
      }
    } catch (error) {
      if (retries > 0) {
        await new Promise(resolve => setTimeout(resolve, 600));
        return this.request(endpoint, options, retries - 1, isInternalAuth);
      }
      throw error;
    }
  }

  async initializeCsrf() {
    this.csrf = (await this.request('/csrf', {}, 2, true)).token;
  }

  async json(endpoint, method, body) {
    return this.request(endpoint, {
      method,
      headers: { 'content-type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body)
    });
  }
}

// Deterministic pseudo-random generator so distribution is natural & reproducible
function createRng(seed = 20261112) {
  let state = seed >>> 0;
  return function next() {
    state = (1664525 * state + 1013904223) >>> 0;
    return state / 0x100000000;
  };
}

function shuffle(array, rng) {
  const copy = [...array];
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(rng() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

async function runPool(items, concurrency, worker) {
  const results = new Array(items.length);
  let cursor = 0;
  async function runner() {
    while (cursor < items.length) {
      const index = cursor;
      cursor += 1;
      results[index] = await worker(items[index], index);
    }
  }
  await Promise.all(Array.from({ length: Math.min(concurrency, items.length) }, () => runner()));
  return results;
}

function loadCategoryImages(rootDir, rng) {
  const pools = {};
  let totalFiles = 0;

  for (const category of CATEGORIES) {
    const categoryDir = path.join(rootDir, category);
    if (!fs.existsSync(categoryDir)) {
      throw new Error(`Missing category folder: ${categoryDir}`);
    }
    const files = fs
      .readdirSync(categoryDir)
      .filter(name => /\.(jpe?g|png|webp)$/i.test(name))
      .sort()
      .map(name => ({
        category,
        filePath: path.join(categoryDir, name),
        fileName: name
      }));
    pools[category] = shuffle(files, rng);
    totalFiles += files.length;
  }

  return { pools, totalFiles };
}

function buildCaptionAndTags(category, indexInCategory, rng) {
  const baseCaptions = CAPTION_POOL[category];
  const baseCaption = baseCaptions[indexInCategory % baseCaptions.length];
  const allTags = shuffle(TAG_POOL[category], rng);
  const selectedTags = allTags.slice(0, 3 + (indexInCategory % 3));
  const hashtagsInline = selectedTags.map(t => `#${t}`).join(' ');
  const caption =
    indexInCategory % 3 === 0 ? `${baseCaption}\n.\n${hashtagsInline}` : baseCaption;

  return { caption, tags: selectedTags };
}

function inferCategoryFromTags(tags = [], fallbackCategory = 'coffee_food') {
  for (const [cat, pool] of Object.entries(TAG_POOL)) {
    if (tags.some(tag => pool.includes(tag))) {
      return cat;
    }
  }
  return fallbackCategory;
}

async function registerAndSetupUser(persona, avatarFile) {
  const session = new Session();
  const email = `${persona.username.replace(/[^a-z0-9.]/gi, '')}@veyru.dev`;
  await session.initializeCsrf();

  let authUser = null;
  try {
    // Check if user already exists from a previous run
    authUser = await session.request(
      '/sessions',
      {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ identifier: email, password })
      },
      0
    );
  } catch {
    // User does not exist yet -> register and login
    await session.json('/users', 'POST', {
      username: persona.username,
      email,
      password
    });
    authUser = await session.json('/sessions', 'POST', {
      identifier: email,
      password
    });
  }

  session.setCredentials(email, password);

  let profile = await session.request('/users/me');
  if (!profile.imageUrl || !profile.bio) {
    // Update bio + avatar via PATCH /users/me only if not already set
    const form = new FormData();
    form.append('username', persona.username);
    form.append('bio', persona.bio);
    if (avatarFile) {
      const buffer = fs.readFileSync(avatarFile.filePath);
      form.append('image', new Blob([buffer], { type: 'image/jpeg' }), avatarFile.fileName);
    }
    profile = await session.request('/users/me', {
      method: 'PATCH',
      body: form
    });
  }

  return {
    ...persona,
    email,
    session,
    user: {
      id: authUser.id,
      username: profile.username || authUser.username,
      imageUrl: profile.imageUrl,
      followingCount: profile.stats?.following ?? profile.followingCount ?? 0,
      followerCount: profile.stats?.followers ?? profile.followerCount ?? 0,
      photoCount: profile.stats?.posts ?? profile.photoCount ?? 0
    }
  };
}

async function uploadPhoto(account, imageItem, captionIndex, rng) {
  const { caption, tags } = buildCaptionAndTags(imageItem.category, captionIndex, rng);
  const buffer = fs.readFileSync(imageItem.filePath);
  const form = new FormData();
  form.append('image', new Blob([buffer], { type: 'image/jpeg' }), imageItem.fileName);
  form.append('caption', caption);
  for (const tag of tags) {
    form.append('tags', tag);
  }
  const photo = await account.session.request('/photos', {
    method: 'POST',
    body: form
  });
  return {
    ...photo,
    category: imageItem.category,
    authorAccount: account
  };
}

async function main() {
  const rng = createRng(20261112);

  if (!fs.existsSync(imageRootDir)) {
    throw new Error(
      `Image directory not found at ${imageRootDir}.\nPlease pass IMAGE_DIR=~/Downloads/image-mock-data-veyru`
    );
  }

  const { pools, totalFiles } = loadCategoryImages(imageRootDir, rng);
  console.log(`Target API Base URL: ${baseUrl}`);
  console.log(`Found ${totalFiles} images across ${CATEGORIES.length} categories in ${imageRootDir}`);

  // Reserve 36 unique portrait/streetwear images strictly for user avatars
  const avatarImages = pools.fashion_streetwear.splice(0, USER_PERSONAS.length);
  const remainingPostImagesCount = CATEGORIES.reduce((sum, cat) => sum + pools[cat].length, 0);
  console.log(
    `Allocated ${avatarImages.length} unique images for Avatars and ${remainingPostImagesCount} unique images for Posts (0 duplicates).`
  );

  // ================= STEP 1: REGISTER 36 USERS & UPLOAD AVATARS =================
  console.log(`\n[1/4] Registering ${USER_PERSONAS.length} users and uploading avatars & bios...`);
  let completedUsers = 0;
  const accounts = await runPool(USER_PERSONAS, concurrencyLimit, async (persona, idx) => {
    const account = await registerAndSetupUser(persona, avatarImages[idx]);
    completedUsers += 1;
    if (completedUsers % 6 === 0 || completedUsers === USER_PERSONAS.length) {
      console.log(`  -> Users ready: ${completedUsers}/${USER_PERSONAS.length}`);
    }
    return account;
  });

  // ================= STEP 2: BUILD PYRAMID SOCIAL GRAPH (FOLLOWS) =================
  console.log('\n[2/4] Building Pyramid Social Graph (Follows)...');
  const followPairs = [];
  for (const follower of accounts) {
    for (const target of accounts) {
      if (follower.user.id === target.user.id) continue;
      // Power-law follow probability based on target's tier + shared niche
      const sameNiche =
        follower.primary === target.primary || follower.secondary === target.primary;
      let prob = 0.12;
      if (target.tier === 1) prob = sameNiche ? 0.85 : 0.68;
      else if (target.tier === 2) prob = sameNiche ? 0.52 : 0.28;
      else prob = sameNiche ? 0.25 : 0.10;

      if (rng() < prob) {
        followPairs.push({ follower, target });
      }
    }
  }

  const existingFollowsSum = accounts.reduce((sum, a) => sum + (a.user.followingCount || 0), 0);
  if (existingFollowsSum >= followPairs.length * 0.8) {
    console.log(
      `  -> Social graph already present (${existingFollowsSum} follows found), skipping follow requests.`
    );
  } else {
    await runPool(followPairs, concurrencyLimit * 2, async ({ follower, target }) => {
      await follower.session.request(`/users/me/following/${target.user.id}`, { method: 'PUT' });
    });
    console.log(`  -> Created ${followPairs.length} follow relationships.`);
  }

  // ================= STEP 3: ASSIGN & INTERLEAVE ALL REMAINING IMAGES =================
  console.log(`\n[3/4] Allocating & uploading all ${remainingPostImagesCount} unique post images...`);
  const userQueues = new Map(accounts.map(acc => [acc.user.username, []]));

  // Distribute every single remaining image in every category pool to matching users (80% primary, 20% secondary)
  for (const category of CATEGORIES) {
    const pool = pools[category];
    const primaryAccounts = accounts.filter(a => a.primary === category);
    const secondaryAccounts = accounts.filter(a => a.secondary === category);

    // Weighted slots by tier so Tier 1 gets the most posts, Tier 2 medium, Tier 3 casual
    const weightedTargets = [];
    for (const acc of primaryAccounts) {
      const weight = acc.tier === 1 ? 6 : acc.tier === 2 ? 4 : 2;
      for (let w = 0; w < weight; w += 1) weightedTargets.push(acc);
    }
    for (const acc of secondaryAccounts) {
      const weight = acc.tier === 1 ? 2 : 1;
      for (let w = 0; w < weight; w += 1) weightedTargets.push(acc);
    }

    let cursor = 0;
    while (pool.length > 0) {
      const imageItem = pool.shift();
      const targetAcc = weightedTargets[cursor % weightedTargets.length];
      userQueues.get(targetAcc.user.username).push(imageItem);
      cursor += 1;
    }
  }

  // Check existing photos per user so we can resume without re-uploading 415 photos to Cloudinary
  const existingPhotosByUser = new Map();
  let totalExistingPhotos = 0;
  await runPool(accounts, concurrencyLimit, async acc => {
    const pageResp = await acc.session.request(
      `/photos?userId=${acc.user.id}&page=0&size=100`
    );
    const items = pageResp?.items || [];
    existingPhotosByUser.set(acc.user.username, items);
    totalExistingPhotos += items.length;
  });

  if (totalExistingPhotos > 0) {
    console.log(
      `  -> Found ${totalExistingPhotos} existing photos on server! Reusing existing photos without re-uploading.`
    );
  }

  // Round-robin interleave uploads across users so the global timeline mixes authors naturally
  const uploadTasks = [];
  const captionCounters = {
    coffee_food: 0,
    fashion_streetwear: 0,
    pets: 0,
    tech_setup: 0,
    travel_nature: 0
  };

  let stillHasPhotos = true;
  while (stillHasPhotos) {
    stillHasPhotos = false;
    for (const acc of shuffle(accounts, rng)) {
      const queue = userQueues.get(acc.user.username);
      if (queue.length > 0) {
        stillHasPhotos = true;
        const imageItem = queue.shift();
        const captionIndex = captionCounters[imageItem.category];
        captionCounters[imageItem.category] += 1;
        const existingList = existingPhotosByUser.get(acc.user.username) || [];
        const existingPhoto = existingList.shift();
        uploadTasks.push({ account: acc, imageItem, captionIndex, existingPhoto });
      }
    }
  }

  let uploadedCount = 0;
  const photos = await runPool(uploadTasks, concurrencyLimit, async task => {
    let created;
    if (task.existingPhoto) {
      created = {
        ...task.existingPhoto,
        category: inferCategoryFromTags(task.existingPhoto.tags, task.imageItem.category),
        authorAccount: task.account
      };
    } else {
      created = await uploadPhoto(task.account, task.imageItem, task.captionIndex, rng);
    }
    uploadedCount += 1;
    if (uploadedCount % 25 === 0 || uploadedCount === uploadTasks.length) {
      console.log(`  -> Photos ready: ${uploadedCount}/${uploadTasks.length}`);
    }
    return created;
  });

  // ================= STEP 4: LIKES, NESTED COMMENTS, SHARES, FAVORITES & USER TAGS =================
  console.log('\n[4/4] Seeding Likes, Nested Comments + Replies, Shares, Saves & User Tags...');
  let totalLikes = 0;
  let totalComments = 0;
  let totalShares = 0;
  let totalFavorites = 0;
  let totalUserTags = 0;
  let engagedPhotosCount = 0;

  await runPool(photos, concurrencyLimit, async (photo, photoIdx) => {
    const author = photo.authorAccount;
    const otherAccounts = accounts.filter(a => a.user.id !== author.user.id);
    const shuffledOthers = shuffle(otherAccounts, rng);

    // Power-law Likes: viral (16-28), popular (8-15), normal (3-7), fresh (1-3)
    const roll = rng();
    let targetLikes;
    if (author.tier === 1 && roll < 0.35) {
      targetLikes = 16 + Math.floor(rng() * 13);
    } else if (roll < 0.75) {
      targetLikes = 6 + Math.floor(rng() * 9);
    } else {
      targetLikes = 2 + Math.floor(rng() * 4);
    }
    const likers = shuffledOthers.slice(0, Math.min(targetLikes, shuffledOthers.length));
    const alreadyEngaged = (photo.likeCount || 0) >= likers.length;

    if (alreadyEngaged) {
      totalLikes += photo.likeCount || 0;
    } else {
      await runPool(likers, 6, async liker => {
        await liker.session.request(`/photos/${photo.id}/likes/me`, { method: 'PUT' });
        totalLikes += 1;
      });
    }

    // Favorites (Bookmarks) on ~35% of photos -> PUT /users/me/favorites/{photoId}
    if (rng() < 0.35 && shuffledOthers.length > 0 && !alreadyEngaged) {
      const saverCount = 1 + Math.floor(rng() * 3);
      const savers = shuffledOthers.slice(0, saverCount);
      await Promise.all(
        savers.map(async saver => {
          await saver.session.request(`/users/me/favorites/${photo.id}`, {
            method: 'PUT'
          });
          totalFavorites += 1;
        })
      );
    }

    // Comments + Nested Replies + Comment Likes on ~65% of photos (skip if photo already has comments)
    if (rng() < 0.65 && shuffledOthers.length >= 2 && !(photo.commentCount > 0)) {
      const templates = COMMENT_CONVERSATIONS[photo.category] || COMMENT_CONVERSATIONS.coffee_food;
      const threadCount = author.tier === 1 && rng() < 0.45 ? 2 : 1;

      for (let t = 0; t < threadCount; t += 1) {
        const commenter = shuffledOthers[t];
        const template = templates[(photoIdx + t) % templates.length];

        // Top-level comment
        const parentComment = await commenter.session.json(`/photos/${photo.id}/comments`, 'POST', {
          text: template.q,
          mentionedUsernames: []
        });
        totalComments += 1;

        // Author likes the comment
        await author.session.request(`/comments/${parentComment.id}/likes/me`, { method: 'PUT' });

        // Author replies to the comment (nested reply with @mention)
        if (rng() < 0.8) {
          const replyText = template.a.replace('@{user}', `@${commenter.user.username}`);
          await author.session.json(`/photos/${photo.id}/comments`, 'POST', {
            text: replyText,
            parentCommentId: parentComment.id,
            mentionedUsernames: [commenter.user.username]
          });
          totalComments += 1;
        }
      }
    }

    // User Tags on ~20% of fashion/coffee/travel photos
    if (
      ['fashion_streetwear', 'coffee_food', 'travel_nature'].includes(photo.category) &&
      rng() < 0.2 &&
      shuffledOthers.length > 0
    ) {
      const taggedAccount = shuffledOthers[0];
      try {
        await author.session.request(
          `/photos/${photo.id}/user-tags`,
          {
            method: 'POST',
            headers: { 'content-type': 'application/json' },
            body: JSON.stringify({
              taggedUserId: taggedAccount.user.id,
              positionX: Number((25 + rng() * 50).toFixed(1)),
              positionY: Number((25 + rng() * 50).toFixed(1))
            })
          },
          0
        );
        totalUserTags += 1;
      } catch {
        // Ignore 409 if already tagged
      }
    }

    // Shares on top viral posts (~18 total shares)
    if (
      author.tier === 1 &&
      photoIdx % 22 === 0 &&
      shuffledOthers.length > 0 &&
      !(photo.shareCount > 0)
    ) {
      const sharer = shuffledOthers[photoIdx % shuffledOthers.length];
      const shareCaption = SHARE_CAPTIONS[totalShares % SHARE_CAPTIONS.length];
      await sharer.session.json(`/photos/${photo.id}/shares`, 'POST', {
        caption: shareCaption
      });
      totalShares += 1;
    }

    engagedPhotosCount += 1;
    if (engagedPhotosCount % 25 === 0 || engagedPhotosCount === photos.length) {
      console.log(`  -> Engaged photos: ${engagedPhotosCount}/${photos.length}`);
    }
  });

  console.log('\n============================================================');
  console.log('SEED COMPLETED SUCCESSFULLY!');
  console.log('============================================================');
  console.log(
    JSON.stringify(
      {
        baseUrl,
        passwordForAllSeedUsers: password,
        stats: {
          users: accounts.length,
          avatarsUsed: avatarImages.length,
          photosUploaded: photos.length,
          totalUniqueImagesUsed: avatarImages.length + photos.length,
          follows: followPairs.length,
          likes: totalLikes,
          commentsAndReplies: totalComments,
          favorites: totalFavorites,
          userTags: totalUserTags,
          shares: totalShares
        },
        topCreatorsSample: accounts
          .filter(a => a.tier === 1)
          .map(a => ({ username: a.user.username, email: a.email }))
      },
      null,
      2
    )
  );
}

main().catch(error => {
  console.error('\n[SEED FAILED]:', error.message);
  process.exitCode = 1;
});
