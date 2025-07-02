INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (1,'ow6Cw7c2KZeU7sEPraExme5_0ttA','北陌','cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/avatar/1747241494125-4602.webp','super_admin','2025-05-11 14:35:38');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (2,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','Abin.','cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/avatar/1747411441204-8146.webp','super_admin','2025-05-11 14:05:01');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (3,'ow6Cw7RbbMlZ0tNjTRQYnMhKuRFQ','whisper','cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/avatar/1747233579404-7251.webp','super_admin','2025-05-11 15:34:09');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (4,'ow6Cw7Xg9G0HX3B1tGvchq7rCJyI','有为青年','https://mmbiz.qpic.cn/mmbiz_png/2C4P2p1KPudK97C0w0KqKwAUMFGxS7tKC82q0hAsHh9ibzBXnHlTDRicfGF11cxYwia6Yl3GwichNcsvJcEbpH8lWA/300?wx_fmt=png&wxfrom=18','super_admin','2025-05-12 00:18:37');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (5,'ow6Cw7VKoGfba7cQ9pBpeLGADj0I','炫煈','https://mmbiz.qpic.cn/mmbiz_png/2C4P2p1KPudK97C0w0KqKwAUMFGxS7tKC82q0hAsHh9ibzBXnHlTDRicfGF11cxYwia6Yl3GwichNcsvJcEbpH8lWA/300?wx_fmt=png&wxfrom=18','user','2025-05-14 22:49:52');
INSERT INTO `user` (`id`,`openid`,`nickname`,`avatar`,`role`,`create_time`) VALUES (6,'ow6Cw7XYTmkI8vERpJzZ-MLuhehE','u1','cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/avatar/1747234906663-7002.webp','user','2025-05-14 22:58:56');

INSERT INTO `banner` (`id`,`image_url`,`title`,`link_type`,`link_value`,`sort_order`,`created_by`,`updated_by`) VALUES (1,'https://mmbiz.qpic.cn/mmbiz_jpg/2C4P2p1KPudpBOHjic7TOf3XolibZpsibtw2xpm7cicTBrEX4KkYhUibbAYgCQsKy2Vx2XBAgZg8JU4Ct27vvcwrAHw/640?wx_fmt=jpeg&from=appmsg','小Y户外',2,'https://mp.weixin.qq.com/s?__biz=Mzk2NDU0NjMzNw==&mid=2247483732&idx=1&sn=11bc0b8e47571207f443be935d4c8e6b&clicktag=js_name&scene=319&fromweappid=&nearchildidx=0&nearchildpercent=0.02&subscene=1',-1,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','ow6Cw7Yo8GqLDEACsmrubVO8iW_s');

INSERT INTO `location` (`id`,`name`,`address`,`latitude`,`longitude`,`create_time`,`updated_time`,`location_activity_id`,`location_product_id`) VALUES (10,'清远市坐标系旅游文化有限公司','广东省清远市清城区真一村50号','23.6913720','113.0835950','2025-05-14 22:35:23','2025-05-14 22:35:23',0,0);
INSERT INTO `location` (`id`,`name`,`address`,`latitude`,`longitude`,`create_time`,`updated_time`,`location_activity_id`,`location_product_id`) VALUES (11,'南峡','广东省清远市清城区','23.6972290','113.1434980','2025-05-14 22:39:02','2025-05-14 22:39:02',0,0);
INSERT INTO `location` (`id`,`name`,`address`,`latitude`,`longitude`,`create_time`,`updated_time`,`location_activity_id`,`location_product_id`) VALUES (12,'阳山河坪莫六公游客接待中心','广东省清远市阳山县阳城镇河坪村委会(原河坪小学教学楼)','24.3576700','112.7811050','2025-05-14 23:00:16','2025-05-14 23:00:16',0,0);

INSERT INTO `wish` (`id`,`user_openid`,`title`,`description`,`location_id`,`image_urls`,`status`,`vote_count`,`comment_count`,`create_time`) VALUES (1,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','坐标系旅游','想去玩[哇][哇]',10,'[\"cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/wish/1747233277661-2033.webp\",\"cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/wish/1747233277659-518.webp\",\"cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/wish/1747233277657-4219.webp\",\"cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/wish/1747233277660-5161.webp\"]',1,5,1,'2025-05-14 22:35:24');
INSERT INTO `wish` (`id`,`user_openid`,`title`,`description`,`location_id`,`image_urls`,`status`,`vote_count`,`comment_count`,`create_time`) VALUES (2,'ow6Cw7RbbMlZ0tNjTRQYnMhKuRFQ','南峡','因为没有去过，这周去',11,'[\"cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/wish/1747233520820-1250.webp\"]',0,2,2,'2025-05-14 22:39:02');
INSERT INTO `wish` (`id`,`user_openid`,`title`,`description`,`location_id`,`image_urls`,`status`,`vote_count`,`comment_count`,`create_time`) VALUES (3,'ow6Cw7XYTmkI8vERpJzZ-MLuhehE','我是u1，我命令去莫六公','因为我要做莫六公的山主',12,'[\"cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/wish/1747234805407-4388.webp\"]',0,3,5,'2025-05-14 23:00:16');

INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (2,1,'ow6Cw7RbbMlZ0tNjTRQYnMhKuRFQ','2025-05-14 22:39:07');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (3,1,'ow6Cw7Xg9G0HX3B1tGvchq7rCJyI','2025-05-14 22:41:20');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (5,2,'ow6Cw7RbbMlZ0tNjTRQYnMhKuRFQ','2025-05-14 22:45:16');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (6,1,'ow6Cw7c2KZeU7sEPraExme5_0ttA','2025-05-14 22:56:11');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (11,3,'ow6Cw7c2KZeU7sEPraExme5_0ttA','2025-05-15 00:36:10');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (17,3,'ow6Cw7RbbMlZ0tNjTRQYnMhKuRFQ','2025-05-15 00:40:02');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (27,1,'ow6Cw7XYTmkI8vERpJzZ-MLuhehE','2025-05-16 08:55:56');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (53,2,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','2025-05-19 00:54:36');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (54,3,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','2025-05-19 00:54:46');
INSERT INTO `wish_vote` (`id`,`wish_vote_id`,`user_openid`,`vote_time`) VALUES (55,1,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','2025-05-19 00:54:50');

INSERT INTO `wish_comment` (`id`,`wish_id`,`user_openid`,`content`,`create_time`) VALUES (3,2,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','羡慕嘞','2025-05-14 22:40:09');
INSERT INTO `wish_comment` (`id`,`wish_id`,`user_openid`,`content`,`create_time`) VALUES (4,3,'ow6Cw7Xg9G0HX3B1tGvchq7rCJyI','不想去','2025-05-14 23:02:24');
INSERT INTO `wish_comment` (`id`,`wish_id`,`user_openid`,`content`,`create_time`) VALUES (5,3,'ow6Cw7RbbMlZ0tNjTRQYnMhKuRFQ','想去','2025-05-14 23:37:01');
INSERT INTO `wish_comment` (`id`,`wish_id`,`user_openid`,`content`,`create_time`) VALUES (6,2,'ow6Cw7Xg9G0HX3B1tGvchq7rCJyI','真这周去吗？','2025-05-15 00:16:14');
INSERT INTO `wish_comment` (`id`,`wish_id`,`user_openid`,`content`,`create_time`) VALUES (7,3,'ow6Cw7c2KZeU7sEPraExme5_0ttA','马上出发','2025-05-15 00:36:31');
INSERT INTO `wish_comment` (`id`,`wish_id`,`user_openid`,`content`,`image_urls`,`create_time`) VALUES (8,3,'ow6Cw7Yo8GqLDEACsmrubVO8iW_s','gogogo出发咯','[\"cloud://prod-3goiaaiadf5a04cf.7072-prod-3goiaaiadf5a04cf-1358688773/comment/1747240875701-2957.webp\"]','2025-05-15 00:41:18');
INSERT INTO `wish_comment` (`id`,`wish_id`,`user_openid`,`content`,`create_time`) VALUES (9,3,'ow6Cw7XYTmkI8vERpJzZ-MLuhehE','底下这三个是谁','2025-05-16 08:55:30');
INSERT INTO `wish_comment` (`id`,`wish_id`,`user_openid`,`content`,`create_time`) VALUES (10,1,'ow6Cw7XYTmkI8vERpJzZ-MLuhehE','你过去上班就可以天天去了','2025-05-16 08:56:09');

