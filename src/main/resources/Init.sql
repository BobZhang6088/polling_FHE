-- Create the polls table with an end_time field to indicate when the poll ends
CREATE TABLE polls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    end_time DATETIME NOT NULL  -- Add an end_time field to store the poll's end time
);

-- Create the questions table with a question_order field to indicate the order of questions in a poll
CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    poll_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    question_order INT NOT NULL, -- Field to define the order of questions
    FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE
);

-- Create the options table with an option_order field to indicate the order of options in a question
CREATE TABLE options (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    option_order INT NOT NULL, -- Field to define the order of options
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Create the results table to store encrypted selection counts
CREATE TABLE results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    poll_id INT NOT NULL,
    question_id INT NOT NULL,
    encrypted_option_id VARBINARY(1024),
    encrypted_result VARBINARY(1024), -- Store the encrypted selection count
    FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES options(id) ON DELETE CASCADE
);