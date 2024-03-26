import sqlite3

def create_screening_results_table():
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS screening_results (
            result_id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            screening_id TEXT UNIQUE,
            initial_screening_label TEXT,
            final_screening_result TEXT, 
            screening_date DATE DEFAULT CURRENT_DATE,
            FOREIGN KEY (user_id) REFERENCES users(id)
        )
    ''')
    conn.commit()
    conn.close()


# ----------- Adding Initial Screening label ----------- # 
def add_initial_screening_result(user_id, initial_label):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    screening_id = f"SCR_{user_id}"
    try:
        cursor.execute('''
            INSERT INTO screening_results 
            (user_id, screening_id, initial_screening_label) 
            VALUES (?, ?, ?)
        ''', (user_id, screening_id, initial_label))
        conn.commit()
    except sqlite3.IntegrityError:
        print("User ID already exists. Updating the existing record.")
    except Exception as e:
        print(f"Error adding initial screening result: {e}")
        conn.rollback()
    finally:
        conn.close()



# ----------- Adding dyslexia label ----------- # 
def update_final_screening_result(user_id, final_result):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        cursor.execute('''
            UPDATE screening_results
            SET final_screening_result = ?
            WHERE user_id = ?
        ''', (final_result, user_id))
        conn.commit()
    except Exception as e:
        print(f"Error updating final screening result: {e}")
        conn.rollback()
    finally:
        conn.close()




# ------------------SCALED SCREENING DATA HANDLING-------------------------

import sqlite3

def get_db_connection():
    # Adjust the path to your SQLite database as necessary
    return sqlite3.connect('dyslexiadetect.db')

def create_user_screening_table(user_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    table_name = f'SCR_DATA_USER_{user_id}'
    cursor.execute(f'''
        CREATE TABLE IF NOT EXISTS {table_name} (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            recording_time INTEGER,
            point_of_regard_left_x INTEGER,
            point_of_regard_left_y INTEGER,
            point_of_regard_right_x INTEGER,
            point_of_regard_right_y INTEGER,
            gaze_direction TEXT,
            category_left TEXT,
            category_right TEXT
        )
    ''')
    conn.commit()
    conn.close()
    return table_name

def associate_user_table(user_id, table_name):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        INSERT INTO Screening_data_table (user_id, table_name) VALUES (?, ?)
    ''', (user_id, table_name))
    conn.commit()
    conn.close()

def insert_screening_data(user_id, data):
    table_name = create_user_screening_table(user_id)
    associate_user_table(user_id, table_name)
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(f'''
        INSERT INTO {table_name} 
        (recording_time, point_of_regard_left_x, point_of_regard_left_y,
         point_of_regard_right_x, point_of_regard_right_y, gaze_direction,
         category_left, category_right)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ''', (data['recording_time'], data['point_of_regard_left_x'], data['point_of_regard_left_y'],
          data['point_of_regard_right_x'], data['point_of_regard_right_y'], data['gaze_direction'],
          data['category_left'], data['category_right']))
    conn.commit()
    conn.close()

# Ensure the Screening_data_table exists
def initialize_screening_data_table():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS Screening_data_table (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            table_name TEXT
        )
    ''')
    conn.commit()
    conn.close()

def initialize_raw_screening_data_table():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS RawScreening_data_table (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            table_name TEXT
        )
    ''')
    conn.commit()
    conn.close()


# Call this function at the start of your application to ensure the table exists
initialize_screening_data_table()
initialize_raw_screening_data_table()

# Example usage
# user_id = 1005
# data = {
#     'recording_time': 1711080000,
#     'point_of_regard_left_x': 653,
#     'point_of_regard_left_y': 729,
#     'point_of_regard_right_x': 1133,
#     'point_of_regard_right_y': 725,
#     'gaze_direction': 'Looking center',
#     'category_left': 'Unknown',
#     'category_right': 'Unknown'
# }
# insert_screening_data(user_id, data)



# ----------------------RAW DATA HANDLING------------------

def create_user_raw_screening_table(user_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    table_name = f'SCR_RAW_DATA_USER_{user_id}'
    cursor.execute(f'''
        CREATE TABLE IF NOT EXISTS {table_name} (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            recording_time INTEGER,
            point_of_regard_left_x INTEGER,
            point_of_regard_left_y INTEGER,
            point_of_regard_right_x INTEGER,
            point_of_regard_right_y INTEGER,
            gaze_direction TEXT,
            category_left TEXT,
            category_right TEXT
        )
    ''')
    conn.commit()
    conn.close()
    return table_name

def insert_raw_screening_data(user_id, data):
    table_name = create_user_raw_screening_table(user_id)
    associate_raw_user_table(user_id, table_name)
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(f'''
        INSERT INTO {table_name} 
        (recording_time, point_of_regard_left_x, point_of_regard_left_y,
         point_of_regard_right_x, point_of_regard_right_y, gaze_direction,
         category_left, category_right)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ''', (data['recording_time'], data['point_of_regard_left_x'], data['point_of_regard_left_y'],
          data['point_of_regard_right_x'], data['point_of_regard_right_y'], data['gaze_direction'],
          data['category_left'], data['category_right']))
    conn.commit()

def associate_raw_user_table(user_id, table_name):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        INSERT INTO RawScreening_data_table (user_id, table_name) VALUES (?, ?)
    ''', (user_id, table_name))
    conn.commit()
    conn.close()


   