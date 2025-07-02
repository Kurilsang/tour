insert into tour.banner (id, image_url, title, link_type, link_value, sort_order, created_by, updated_by)
values  (1, 'http://fimage.huaxiyou.cc/open/1744296424511', '环岛骑行', 1, '1', 0, '1', '1'),
        (2, 'http://fimage.huaxiyou.cc/open/1734851091257', '徒步路线', 1, '1', 0, '1', '1'),
        (3, 'http://fimage.huaxiyou.cc/open/1745143714328', '测试活动', 1, '2', 0, '1', '1'),
        (4, 'https://mmbiz.qpic.cn/mmbiz_jpg/2C4P2p1KPudpBOHjic7TOf3XolibZpsibtw2xpm7cicTBrEX4KkYhUibbAYgCQsKy2Vx2XBAgZg8JU4Ct27vvcwrAHw/640?wx_fmt=jpeg&from=appmsg', '小Y户外', 2, 'https://mp.weixin.qq.com/s?__biz=Mzk2NDU0NjMzNw==&mid=2247483732&idx=1&sn=11bc0b8e47571207f443be935d4c8e6b&clicktag=js_name&scene=319&fromweappid=&nearchildidx=0&nearchildpercent=0.02&subscene=1', -1, '1', '1');

INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (1,'ow6Cw7c2KZeU7sEPraExme5_0ttA','北陌','cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/avatar/1747241494125-4602.webp','super_admin','2025-05-11 14:35:38');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (2,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','Abin.','cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/avatar/1747411441204-8146.webp','super_admin','2025-05-11 14:05:01');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (3,'ow6Cw7RbbMlZ0tNjTRQYnMhKuRFQ','whisper','cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/avatar/1747233579404-7251.webp','super_admin','2025-05-11 15:34:09');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (4,'ow6Cw7Xg9G0HX3B1tGvchq7rCJyI','有为青年','https://mmbiz.qpic.cn/mmbiz_png/2C4P2p1KPudK97C0w0KqKwAUMFGxS7tKC82q0hAsHh9ibzBXnHlTDRicfGF11cxYwia6Yl3GwichNcsvJcEbpH8lWA/300?wx_fmt=png&wxfrom=18','super_admin','2025-05-12 00:18:37');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (5,'ow6Cw7VKoGfba7cQ9pBpeLGADj0I','炫煈','https://mmbiz.qpic.cn/mmbiz_png/2C4P2p1KPudK97C0w0KqKwAUMFGxS7tKC82q0hAsHh9ibzBXnHlTDRicfGF11cxYwia6Yl3GwichNcsvJcEbpH8lWA/300?wx_fmt=png&wxfrom=18','user','2025-05-14 22:49:52');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (6,'ow6Cw7XYTmkI8vERpJzZ-MLuhehE','u1','cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/avatar/1747234906663-7002.webp','user','2025-05-14 22:58:56');

insert into tour.achievement (id, title, icon_url, description, sign_in_code, sign_in_url, activity_id, created_at)
values  (1, '测试活动', 'https://pic.616pic.com/ys_img/00/75/70/bEsxK40KQN.jpg', '这是一个测试活动', 'dasgf', null, null, '2025-05-08 16:31:27'),
        (2, '测试活动2', 'https://pic.616pic.com/ys_img/00/94/93/NOkq7lYp6c.jpg', '这是一个测试活动', 'sdf', null, null, '2025-05-08 16:32:00'),
        (3, '测试活动3', 'https://pic.616pic.com/ys_img/00/94/93/NOkq7lYp6c.jpg', '这是一个测试活动', 'sdfased', null, null, '2025-05-08 16:32:00');

INSERT INTO tour.activity (id, title, cover_image, activity_position, early_bird_price, normal_price, early_bird_quota, normal_quota, reserved_early_bird, reserved_normal, total_sold, sign_end_time, start_time, end_time, create_time, created_by, updated_by, description, status) VALUES (1, '夏日狂欢活动', 'cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/activity-covers/1746880017636-9355.jpg', '城市中心广场A区', 99.99, 199.99, 100, 200, 0, 0, 0, '2025-05-30 18:00:00', '2025-06-01 09:00:00', '2025-06-01 18:00:00', '2025-05-10 20:27:31', 'oXwoK6b77fAxMJCnc5ukwhwT8AsU', 'oXwoK6b77fAxMJCnc5ukwhwT8AsU', null, 1);
INSERT INTO tour.activity (id, title, cover_image, activity_position, early_bird_price, normal_price, early_bird_quota, normal_quota, reserved_early_bird, reserved_normal, total_sold, sign_end_time, start_time, end_time, create_time, created_by, updated_by, description, status) VALUES (2, '夏日狂欢活动', 'cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/activity-covers/1746880287683-5498.jpg', '城市中心广场A区', 99.99, 199.99, 100, 200, 0, 0, 0, '2025-05-30 18:00:00', '2025-06-01 09:00:00', '2025-06-01 18:00:00', '2025-05-10 20:31:30', 'oXwoK6b77fAxMJCnc5ukwhwT8AsU', 'oXwoK6b77fAxMJCnc5ukwhwT8AsU', null, 1);
INSERT INTO tour.activity (id, title, cover_image, activity_position, early_bird_price, normal_price, early_bird_quota, normal_quota, reserved_early_bird, reserved_normal, total_sold, sign_end_time, start_time, end_time, create_time, created_by, updated_by, description, status) VALUES (3, '徒步旅行', 'cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/activity-covers/1746880340533-4128.jpg', '城市中心广场A区', 99.99, 199.99, 100, 200, 0, 0, 0, '2025-05-30 18:00:00', '2025-06-01 09:00:00', '2025-06-01 18:00:00', '2025-05-10 20:32:32', 'oXwoK6b77fAxMJCnc5ukwhwT8AsU', 'oXwoK6b77fAxMJCnc5ukwhwT8AsU', null, 1);
INSERT INTO tour.activity (id, title, cover_image, activity_position, early_bird_price, normal_price, early_bird_quota, normal_quota, reserved_early_bird, reserved_normal, total_sold, sign_end_time, start_time, end_time, create_time, created_by, updated_by, description, status) VALUES (5, '雪山行', 'cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/activity-covers/1747058755978-2056.jpg', '城市中心广场A区', 99.99, 199.99, 100, 200, 0, 0, 0, '2025-05-30 18:00:00', '2025-06-01 09:00:00', '2025-06-01 18:00:00', '2025-05-12 22:06:00', 'ow6Cw7c2KZeU7sEPraExme5_0ttA', 'ow6Cw7c2KZeU7sEPraExme5_0ttA', null, 1);
INSERT INTO tour.activity (id, title, cover_image, activity_position, early_bird_price, normal_price, early_bird_quota, normal_quota, reserved_early_bird, reserved_normal, total_sold, sign_end_time, start_time, end_time, create_time, created_by, updated_by, description, status) VALUES (6, '支付测试', 'cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/activity-covers/1747574303831-268.jpg', '城市中心广场A区', 0.01, 0.02, 100, 200, 0, 0, 0, '2025-05-30 18:00:00', '2025-06-01 09:00:00', '2025-06-01 18:00:00', '2025-05-18 21:18:42', 'ow6Cw7c2KZeU7sEPraExme5_0ttA', 'ow6Cw7c2KZeU7sEPraExme5_0ttA', null, 1);


INSERT INTO tour.activity_detail (id, activity_id, content, sort_order) VALUES (1, 1, '<p><img src="https://7072-prod-3goiaaiadf5a04cf-1358688773.tcb.qcloud.la/activity-content/1746880043054-4581.jpg" width="100%"></p><p>活动详情内容</p>', 1);
INSERT INTO tour.activity_detail (id, activity_id, content, sort_order) VALUES (2, 2, '<p>活动详情内容</p>', 1);
INSERT INTO tour.activity_detail (id, activity_id, content, sort_order) VALUES (3, 3, '<p>活动详情添加客服微信，提供“电话姓名身份等信息”登记报名，并缴费确认报名后，客服拉入当天活动群。(提供身份仅用于购买保险)</p><p><br></p><p><br></p><p><br></p><p><br></p><p><br></p><p><br></p><p>1</p><p><br></p><p>清远英西峰林介绍</p><p><br></p><p><br></p><p>清远英西峰林有“广东小桂林”的美誉</p><p><br></p><p>以喀斯特地貌闻名</p><p><br></p><p>拥有上千座形态各异的石灰岩山</p><p><br></p><p>如竹笋傲立或如骆驼卧伏</p><p><br></p><p><br></p><p><br></p><p><br></p><p>英西峰林走廊形成于二亿多年前，其地质结构和桂林大致相似，均属峰林、峰丛地貌。这里的地势北高南低，以丘陵沐地为主，土壤以黄泥土为主，少数为牛肝土和黑土。这些石灰岩山峰在漫长的地质历史中，经过风化、侵蚀和溶蚀作用，形成了今天我们所见的千姿百态的山峰和岩洞。</p><p><br></p><p><br></p><p><br></p><p>英西峰林走廊不仅自然景观丰富，还蕴含着深厚的人文风俗和文化内涵。这里的人们世代以农耕为生，形成了浓郁的乡土气息和田园风光。在英西峰林走廊中，你可以看到农田、流水、小桥、水牛和茅舍，这些元素共同构成了一幅幅如诗如画的田园画卷。</p><p><br></p><p><br></p><p>路线亮点</p><p><br></p><p>1.小赵州桥：桥身爬满蔓藤植物，与周围环境相得益彰，石桥历经岁月洗礼，却依然坚固如初。</p><p><br></p><p>2.洞天仙境：洞天仙境作为大自然的神来之笔，溶洞中钟乳石姿态万千，在五彩斑斓的水影映照下，如同梦幻的童话世界。</p><p><br></p><p>3.溶洞奇观：洞内钟乳石千姿百态，有的像宝剑倒悬，有的似云朵层层，还有的如仙女起舞，光影交织下，宛如异世界。</p><p><br></p><p><br></p><p><br></p><p><br></p><p>内容</p>', 1);
INSERT INTO tour.activity_detail (id, activity_id, content, sort_order) VALUES (5, 5, '<p>活动详情内容</p>', 1);
INSERT INTO tour.activity_detail (id, activity_id, content, sort_order) VALUES (6, 6, '<p>活动详情内容</p>', 1);



INSERT INTO tour.product (id, name, description, price, stock, status, reserve_stock, cover_image, created_by, updated_by, create_time, update_time) VALUES (3, '登山杖', '<p>这是一个商品</p>', 99.00, 99, 2, 0, 'cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/tool-images/1746880904511-4967.jpg', 'oXwoK6b77fAxMJCnc5ukwhwT8AsU', 'oXwoK6b77fAxMJCnc5ukwhwT8AsU', '2025-05-10 20:41:56', '2025-05-19 12:56:05');
INSERT INTO tour.product (id, name, description, price, stock, status, reserve_stock, cover_image, created_by, updated_by, create_time, update_time) VALUES (4, '头盔', '<p><img src="https://7072-prod-3goiaaiadf5a04cf-1358688773.tcb.qcloud.la/product-images/1747058466756-4121.jpg" alt="商品简介图片" width="100%"></p><p><br></p>', 50.00, 6, 2, 0, 'cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/tool-images/1747058482780-5305.jpg', 'ow6Cw7c2KZeU7sEPraExme5_0ttA', 'ow6Cw7c2KZeU7sEPraExme5_0ttA', '2025-05-12 22:01:31', '2025-05-19 12:56:03');
INSERT INTO tour.product (id, name, description, price, stock, status, reserve_stock, cover_image, created_by, updated_by, create_time, update_time) VALUES (5, '支付测试', '<p>支付测试</p>', 0.01, 888, 2, 0, 'cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/tool-images/1747555150962-1631.jpg', 'ow6Cw7c2KZeU7sEPraExme5_0ttA', 'ow6Cw7c2KZeU7sEPraExme5_0ttA', '2025-05-18 15:59:15', '2025-05-19 12:56:01');

insert into tour.location (id, name, address, latitude, longitude, create_time, updated_time, location_activity_id, location_product_id)
values  (1, '', '', 0.0000000, 0.0000000, '2025-05-10 20:00:28', '2025-05-10 20:00:28', 0, 1),
        (2, '', '', 0.0000000, 0.0000000, '2025-05-10 20:03:55', '2025-05-10 20:03:55', 0, 2),
        (3, '城市中心广场', '某市某区某路123号', 39.9088230, 116.3974700, '2025-05-10 20:27:31', '2025-05-10 20:27:31', 1, 0),
        (4, '城市中心广场', '某市某区某路123号', 39.9088230, 116.3974700, '2025-05-10 20:31:30', '2025-05-10 20:31:30', 2, 0),
        (5, '城市中心广场', '某市某区某路123号', 39.9088230, 116.3974700, '2025-05-10 20:32:32', '2025-05-10 20:32:32', 3, 0),
        (6, '', '', 21.6632900, 110.9252300, '2025-05-10 20:41:56', '2025-05-10 20:41:56', 0, 3),
        (7, ' ', ' ', 0.0000000, 0.0000000, '2025-05-13 00:46:21', '2025-05-13 00:46:21', 0, 0);

INSERT INTO tour.location (id, name, address, latitude, longitude, create_time, updated_time, location_activity_id, location_product_id) VALUES (10, '北京市政府(旧址)北', '北京市东城区新城东街19号院', 39.9046900, 116.4071700, '2025-05-18 15:59:15', '2025-05-18 15:59:15', 0, 5);

INSERT INTO tour.traveler (id, openid, name, phone, gender, id_card, birthday, emergency_name, emergency_phone, nickname, is_deleted, delete_time) VALUES (1, 'ow6Cw7c2KZeU7sEPraExme5_0ttA', '小明', '13113131313', 2, 'f3043af9ee7ae398dd99cf1fb18e5b7b0da752660533549aa15a0799815a1aa2', '1977-09-20', '小红', '13112121212', '明哥', 0, NULL);
INSERT INTO tour.traveler (id, openid, name, phone, gender, id_card, birthday, emergency_name, emergency_phone, nickname, is_deleted, delete_time) VALUES (2, 'ow6Cw7c2KZeU7sEPraExme5_0ttA', '钟点鲁强', '13466666666', 1, '6c4a7f000944099cc18bb8aa720e4023a94e64d3b02db3a8045834edd3286fa2', '1965-10-20', '子浩', '13677775555', '强哥', 0, NULL);
INSERT INTO tour.traveler (id, openid, name, phone, gender, id_card, birthday, emergency_name, emergency_phone, nickname, is_deleted, delete_time) VALUES (3, 'ow6Cw7c2KZeU7sEPraExme5_0ttA', '光头强', '14566667777', 1, 'd247cb13cf45fff39aea3c0f13595916a516d8f7865c1a46d159c5a31b94b5bc', '1969-01-03', '熊大', '14589426379', '强老板', 0, NULL);
INSERT INTO tour.traveler (id, openid, name, phone, gender, id_card, birthday, emergency_name, emergency_phone, nickname, is_deleted, delete_time) VALUES (4, 'ow6Cw7c2KZeU7sEPraExme5_0ttA', '刘雪敏', '13233119640', 1, '4eeee0e247d50e9d5165a4b4ab185551f3aff56a5cafb67b415a9d82c5cf275c', '1964-06-15', '吕小沛', '13233119640', '敏', 0, NULL);

insert into tour.wish_vote (id, wish_vote_id, user_openid, vote_time)
values  (1, 1, 'ow6Cw7Yo8GqLDEACsmrubVO8iW_s', null);
# 测试上车点数据
INSERT INTO tour.bus_location (id, bus_location, location_id, create_time) VALUES (1, '广州市大巴站', 8, '2025-05-14 16:06:14');
INSERT INTO tour.bus_location (id, bus_location, location_id, create_time) VALUES (2, '清远站', 9, '2025-05-14 16:06:38');

INSERT INTO tour.location (id, name, address, latitude, longitude, create_time, updated_time, location_activity_id, location_product_id) VALUES (8, '广州市大巴站', '广州市大巴站', 22.9885580, 113.2693230, '2025-05-14 16:06:14', '2025-05-14 16:06:14', 0, 0);
INSERT INTO tour.location (id, name, address, latitude, longitude, create_time, updated_time, location_activity_id, location_product_id) VALUES (9, '清远站', '清远站', 23.6979500, 113.0626900, '2025-05-14 16:06:38', '2025-05-14 16:06:38', 0, 0);

