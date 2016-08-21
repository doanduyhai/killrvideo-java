INSERT INTO killrvideo.videos(videoid, userid, description, location, location_type,
added_date, name, preview_image_location, tags)
VALUES(${videoid1}, 00000000-1111-0000-0000-000000000000, 'Lego Blactron collection', 'file:///tmp/blacktron_collection.mp4', 0,
toTimestamp(now()), 'blacktron_collection.mp4', 'http://xxx', {'lego', 'blacktron', 'toys', 'moc', 'afol'});

INSERT INTO killrvideo.videos_by_tag(tag, videoid, userid, added_date, tagged_date,
name, preview_image_location)
VALUES ('lego', ${videoid2}, 00000000-1111-0000-0000-000000000000, toTimestamp(now()), toTimestamp(now()),
'new_lego_collection.mp4', 'http://xxx');

INSERT INTO killrvideo.videos_by_tag(tag, videoid, userid, added_date, tagged_date,
name, preview_image_location)
VALUES ('blacktron', ${videoid3}, 00000000-1111-0000-0000-000000000000, toTimestamp(now()), toTimestamp(now()),
'allied_avenger.mp4', 'http://xxx');

INSERT INTO killrvideo.videos_by_tag(tag, videoid, userid, added_date, tagged_date,
name, preview_image_location)
VALUES ('toys', ${videoid4}, 00000000-1111-0000-0000-000000000000, toTimestamp(now()), toTimestamp(now()),
'vintage_toys.mp4', 'http://xxx');

INSERT INTO killrvideo.videos_by_tag(tag, videoid, userid, added_date, tagged_date,
name, preview_image_location)
VALUES ('moc', ${videoid5}, 00000000-1111-0000-0000-000000000000, toTimestamp(now()), toTimestamp(now()),
'my_custom_lego_builds.mp4', 'http://xxx');

INSERT INTO killrvideo.videos_by_tag(tag, videoid, userid, added_date, tagged_date,
name, preview_image_location)
VALUES ('afol', ${videoid6}, 00000000-1111-0000-0000-000000000000, toTimestamp(now()), toTimestamp(now()),
'brickcon-2016.mp4', 'http://xxx');