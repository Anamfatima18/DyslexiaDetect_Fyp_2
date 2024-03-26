import datetime
import sqlite3
import random

def create_database():
    conn = sqlite3.connect('dyslexiadetect.db')
    conn.row_factory = sqlite3.Row  
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY,
            email TEXT UNIQUE,
            name TEXT,
            username TEXT UNIQUE,
            age INTEGER,
            password TEXT,
            is_verified INTEGER DEFAULT 0
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS otps (
            email TEXT PRIMARY KEY,
            otp TEXT,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS rhyming_words (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            ComplexityLevel INTEGER NOT NULL,
            Word TEXT NOT NULL,
            Rhyme1 TEXT NOT NULL,
            Rhyme2 TEXT NOT NULL,
            NonRhyme1 TEXT NOT NULL,
            NonRhyme2 TEXT NOT NULL,
            NonRhyme3 TEXT NOT NULL
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS screening_paragraphs (
            id INTEGER PRIMARY KEY,
            age_group TEXT NOT NULL,
            paragraph TEXT NOT NULL,
            word_count INTEGER
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS word_harmony (
                ComplexityLevel INTEGER CHECK(ComplexityLevel BETWEEN 1 AND 3),
                Type TEXT CHECK(Type IN ('letter', 'word')),
                Word TEXT,
                SoundPath TEXT,
                PRIMARY KEY(Word, ComplexityLevel)
        )
    ''')
    conn.commit()
    conn.close()


########## Users ##########

# def add_user(email, name, username, age, password):
#     conn = sqlite3.connect('dyslexiadetect.db')
#     cursor = conn.cursor()
#     try:
#         cursor.execute('INSERT INTO users (email, name, username, age, password) VALUES (?, ?, ?, ?, ?)', 
#                        (email, name, username, age, password))
#         conn.commit()
#     except sqlite3.IntegrityError as e:
#         print(f"Integrity Error: {e}")  # Log the error
#         return False
#     except Exception as e:
#         print(f"General Error: {e}")  # Log any other error
#         return False
#     finally:
#         conn.close()
#     return True
# def generate_unique_user_id(cursor):
#     unique_id_found = False
#     unique_id = 0
#     while not unique_id_found:
#         # Generate a random 4-digit number
#         potential_id = random.randint(1000, 9999)
#         # Check if this ID exists in the users table
#         cursor.execute("SELECT id FROM users WHERE id = ?", (potential_id,))
#         result = cursor.fetchone()
#         if result is None:
#             # If the ID does not exist, it's unique
#             unique_id_found = True
#             unique_id = potential_id
#     return unique_id

# def add_user(email, name, username, age, password):
#     conn = sqlite3.connect('dyslexiadetect.db')
#     cursor = conn.cursor()
#     # Generate a unique user ID
#     user_id = generate_unique_user_id(cursor)
#     try:
#         cursor.execute('INSERT INTO users (id, email, name, username, age, password) VALUES (?, ?, ?, ?, ?, ?)', 
#                        (user_id, email, name, username, age, password))
#         conn.commit()
#     except sqlite3.IntegrityError as e:
#         print(f"Integrity Error: {e}")  # Log the error
#         return False
#     except Exception as e:
#         print(f"General Error: {e}")  # Log any other error
#         return False
#     finally:
#         conn.close()
#     return True
import sqlite3

def generate_sequential_user_id(cursor):
    # Find the maximum id value in the users table
    cursor.execute("SELECT MAX(id) FROM users")
    max_id = cursor.fetchone()[0]
    if max_id is None:
        # If the table is empty, start from 1000
        return 1000
    else:
        # Otherwise, add 1 to the maximum id to get the next id
        return max_id + 1

def add_user(email, name, username, age, password):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    # Generate a sequential user ID
    user_id = generate_sequential_user_id(cursor)
    try:
        cursor.execute('INSERT INTO users (id, email, name, username, age, password) VALUES (?, ?, ?, ?, ?, ?)', 
                       (user_id, email, name, username, age, password))
        conn.commit()
    except sqlite3.IntegrityError as e:
        print(f"Integrity Error: {e}")  # Log the error
        return False
    except Exception as e:
        print(f"General Error: {e}")  # Log any other error
        return False
    finally:
        conn.close()
    return True



def store_otp(email, otp):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    cursor.execute('REPLACE INTO otps (email, otp) VALUES (?, ?)', (email, otp))
    conn.commit()
    conn.close()

def verify_user(email, otp):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    cursor.execute("SELECT otp FROM otps WHERE email = ? AND datetime(timestamp, '+10 minutes') > CURRENT_TIMESTAMP", (email,))
    stored_otp = cursor.fetchone()
    if stored_otp and stored_otp[0] == otp:
        cursor.execute('UPDATE users SET is_verified = 1 WHERE email = ?', (email,))
        conn.commit()
        conn.close()
        return True
    conn.close()
    return False

# def authenticate_user(email, password):
#     conn = sqlite3.connect('dyslexiadetect.db')
#     cursor = conn.cursor()
#     try:
#         cursor.execute('SELECT password, is_verified FROM users WHERE email = ?', (email,))
#         user = cursor.fetchone()
#         if user and user[0] == password and user[1] == 1:
#             return True
#         else:
#             return False
#     except Exception as e:
#         print(f"Error during authentication: {e}")
#         return False
#     finally:
#         conn.close()
import sqlite3

def authenticate_user(email, password):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        # Modified query to select the user's ID as well
        cursor.execute('SELECT id, password, is_verified FROM users WHERE email = ?', (email,))
        user = cursor.fetchone()
        if user and user[1] == password and user[2] == 1:
            # Return the user ID and True for successful authentication
            return user[0], True
        else:
            # Return None for user ID and False for failed authentication
            return None, False
    except Exception as e:
        print(f"Error during authentication: {e}")
        # In case of an exception, return None for user ID and False for authentication
        return None, False
    finally:
        conn.close()
from datetime import datetime


def store_user_token(email, user_id, token):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        # Use REPLACE to update the token if the user_id already exists
        cursor.execute('REPLACE INTO user_tokens (email, user_id, token, timestamp) VALUES (?, ?, ?, ?)', 
                       (email, user_id, token, datetime.now()))
        conn.commit()
    except Exception as e:
        print(f"Error storing user token: {e}")
    finally:
        conn.close()


########## Paragraphs ##########

def add_paragraph(age_group, paragraph):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        cursor.execute('INSERT INTO screening_paragraphs (age_group, paragraph) VALUES (?, ?)',
                       (age_group, paragraph))
        conn.commit()
    except sqlite3.IntegrityError as e:
        print(f"Integrity Error: {e}")
        return False
    except Exception as e:
        print(f"General Error: {e}")
        return False
    finally:
        conn.close()
    return True

def get_paragraph(age_group):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        cursor.execute('SELECT paragraph, word_count FROM screening_paragraphs WHERE age_group = ? ORDER BY RANDOM() LIMIT 1', (age_group,))
        result = cursor.fetchone()
        return (result[0], result[1]) if result else (None, None)
    except Exception as e:
        print(f"Error in getting paragraph: {e}")
        return None, None
    finally:
        conn.close()



######### Rhyming Activity ##########


def get_rhyming_task(level):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()

    query = """
    SELECT Word, Rhyme1, NonRhyme1, NonRhyme2, NonRhyme3 
    FROM rhyming_words 
    WHERE ComplexityLevel = ? 
    ORDER BY RANDOM() 
    LIMIT 1
    """
    cursor.execute(query, (level,))
    task = cursor.fetchone()

    if task:
        # Include one rhyming word and three non-rhyming words in options
        options = [task[1], task[2], task[3], task[4]]
        random.shuffle(options)
        print(f"Fetched task: Word - {task[0]}, Options - {options}")
        return task[0], options
    else:
        print("No task found for the given level.")
        return None, None



def validate_rhyming_answer(word, chosen_option):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    cursor.execute("SELECT Rhyme1, Rhyme2 FROM rhyming_words WHERE Word = ?", (word,))
    correct_answers = cursor.fetchone()
    conn.close()

    return chosen_option in correct_answers


def update_user_score(user_id, level, score):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        cursor.execute('SELECT Score FROM scores WHERE UserID = ? AND Level = ?', (user_id, level))
        existing = cursor.fetchone()
        if existing:
            new_score = existing[0] + score
            cursor.execute('UPDATE scores SET Score = ? WHERE UserID = ? AND Level = ?', (new_score, user_id, level))
        else:
            cursor.execute('INSERT INTO scores (UserID, Level, Score) VALUES (?, ?, ?)', (user_id, level, score))
        conn.commit()
    except Exception as e:
        print(f"Error updating score: {e}")
        return False
    finally:
        conn.close()
    return True



######### Word Harmony Activity ##########


def get_word_harmony_task(level):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()

    type_of_task = 'letter' if level == 1 else 'word'
    correct_query = """
    SELECT Word, SoundPath 
    FROM word_harmony 
    WHERE ComplexityLevel = ? AND Type = ?
    ORDER BY RANDOM() 
    LIMIT 1
    """
    incorrect_query = """
    SELECT Word 
    FROM word_harmony 
    WHERE ComplexityLevel = ? AND Type = ? AND Word != ?
    ORDER BY RANDOM() 
    LIMIT 5
    """
    
    cursor.execute(correct_query, (level, type_of_task))
    correct_task = cursor.fetchone()

    if correct_task:
        correct_word, sound_path = correct_task
        cursor.execute(incorrect_query, (level, type_of_task, correct_word))
        incorrect_words = [row[0] for row in cursor.fetchall()]
        options = [correct_word] + incorrect_words
        random.shuffle(options)  # Shuffle to randomize the position of the correct word
        return correct_word, sound_path, options
    else:
        return None, None, None


def validate_word_harmony_answer(word, chosen_word):
    # This function assumes that the correct answer is directly provided for comparison
    return word == chosen_word


