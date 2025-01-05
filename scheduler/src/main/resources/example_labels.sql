INSERT INTO label (name)
VALUES 
    ('sleep'),
    ('stress'),
    ('food'),
    ('health'),
    ('stimulants');

INSERT INTO user_labels (user_id, label_id, matching)
SELECT 
    u.id AS user_id,
    l.id AS label_id,
    FLOOR(RANDOM() * 101)::INT2 AS matching
FROM "user" u
CROSS JOIN label l;

INSERT INTO content_labels (content_id, label_id, matching)
SELECT 
    c.id AS content_id,
    l.id AS label_id,
    FLOOR(RANDOM() * 101)::INT2 AS matching
FROM content c
CROSS JOIN label l;