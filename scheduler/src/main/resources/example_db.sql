INSERT INTO "user" (name, email)
VALUES
    ('Damian', 'damianwk1999@gmail.com'),
    ('Wojciech', 'wggajda@gmail.com');


INSERT INTO content (path, title, mail_content)
VALUES
    ('UNKNOWN', 'Stress & Coffee: The Link', 'Curious if your coffee habit adds to your stress? Find out how caffeine impacts your anxiety in this insightful video. Watch it now: https://www.youtube.com/watch?v=sazR-VTwJzo'),
    ('UNKNOWN', 'Alcohol, Sleep, & Stress', 'Learn how alcohol, sleep, and stress create a self-fulfilling prophecy. Don’t miss this eye-opening discussion by experts. Watch here: https://www.youtube.com/watch?v=z_lzPtwzX-E'),
    ('UNKNOWN', 'Sleep Apnea & Weight', 'Discover the surprising link between sleep apnea and weight changes. This video might be life-changing! Watch now: https://www.youtube.com/watch?v=9mVm0rxEbsU'),
    ('UNKNOWN', 'Deep Sleep Benefits', 'Maximize the benefits of deep sleep and boost your health. Find actionable tips in this engaging talk. Watch here: https://www.youtube.com/watch?v=1U2qMRGihGg'),
    ('UNKNOWN', 'Treating Anorexia in Poland', 'See how anorexia is treated in Poland and learn about the experiences of those affected. A powerful story awaits: https://www.youtube.com/watch?v=QSyzlKaG7qQ'),
    ('UNKNOWN', 'Workplace Stress Costs', 'Workplace stress costs more than you think. Find strategies to reduce it in this practical guide. Watch here: https://youtu.be/QE8kNh52EeU?si=PzTTe1viIMDw596C'),
    ('UNKNOWN', '6 Tips for Better Sleep', 'Struggling with sleep? Try these 6 science-backed tips to improve your rest and overall well-being. Watch now: https://www.youtube.com/watch?v=t0kACis_dJE'),
    ('UNKNOWN', 'Work-Life Balance Rules', 'Learn 3 actionable rules to achieve better work-life balance. Start your journey toward harmony here: https://www.youtube.com/watch?v=4c_xYLwOx-g');

DO $$
DECLARE
    new_course_id INT;
BEGIN
    INSERT INTO course (stages) VALUES (5) RETURNING id INTO new_course_id;

    WITH random_contents AS (
        SELECT id
        FROM content
        ORDER BY RANDOM()
        LIMIT 5
    )
    INSERT INTO course_contents (course_id, content_id, stage)
    SELECT new_course_id, id, ROW_NUMBER() OVER ()
    FROM random_contents;

    INSERT INTO user_courses (user_id, course_id, stage)
    SELECT id, new_course_id, 0
    FROM "user";
END $$;
