INSERT INTO killrvideo.videos(videoid, userid, description, location, location_type,
added_date, name, preview_image_location)
VALUES(${videoid}, 00000000-1111-0000-0000-000000000000, 'untagged video', 'file:///tmp/video.mp4', 0,
toTimestamp(now()), 'untagged.mp4', 'http://xxx');