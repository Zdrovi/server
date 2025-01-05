DROP SCHEMA public CASCADE;
CREATE SCHEMA IF NOT EXISTS public;

-- User table
CREATE TABLE "user" (
                        id SERIAL PRIMARY KEY,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE
);

-- Label table
CREATE TABLE label (
                     id SERIAL PRIMARY KEY,
                     name TEXT NOT NULL UNIQUE
);

-- Course table
CREATE TABLE course (
                        id SERIAL PRIMARY KEY,
                        stages INTEGER NOT NULL
);

-- Content table
CREATE TABLE content (
                         id SERIAL PRIMARY KEY,
                         path TEXT NOT NULL,
                         title TEXT NOT NULL,
                         mail_content TEXT NOT NULL
);

-- UserLabels (junction table between User and Label)
CREATE TABLE user_labels (
                           id SERIAL PRIMARY KEY,
                           user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
                           label_id INTEGER REFERENCES label(id) ON DELETE CASCADE,
                           matching INT2 NOT NULL,
                           UNIQUE(user_id, label_id)
);

-- UserCourses (junction table between User and Course)
CREATE TABLE user_courses (
                              id SERIAL PRIMARY KEY,
                              user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
                              course_id INTEGER REFERENCES course(id) ON DELETE CASCADE,
                              stage INTEGER NOT NULL,
                              UNIQUE(user_id, course_id)
);

-- CourseLabels (junction table between Course and Label)
CREATE TABLE content_labels (
                             id SERIAL PRIMARY KEY,
                             content_id INTEGER REFERENCES content(id) ON DELETE CASCADE,
                             label_id INTEGER REFERENCES label(id) ON DELETE CASCADE,
                             matching INT2 NOT NULL,
                             UNIQUE(content_id, label_id)
);

-- CourseContents (junction table between Course and Content)
CREATE TABLE course_contents (
                                 id SERIAL PRIMARY KEY,
                                 course_id INTEGER REFERENCES course(id) ON DELETE CASCADE,
                                 content_id INTEGER REFERENCES content(id) ON DELETE CASCADE,
                                 stage INTEGER NOT NULL,
                                 UNIQUE(course_id, content_id)
);