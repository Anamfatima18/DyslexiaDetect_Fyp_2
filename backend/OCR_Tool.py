
from PIL import Image
import pytesseract

def perform_ocr(left_eye_x, left_eye_y, right_eye_x, right_eye_y, screenshot_path):
    combined_x = (left_eye_x + right_eye_x) / 2
    combined_y = (left_eye_y + right_eye_y) / 2

    image_data = Image.open(screenshot_path)

    line = extract_line_at_point(image_data, combined_x, combined_y)
    print(line)
    return line

def extract_line_at_point(image, x, y):
    text = pytesseract.image_to_string(image)
    lines = text.split('\n')
    for line in lines:
        if line.strip() and line_contains_point(line, x, y):
            return line.strip()

    return "Line containing the point not found"

def line_contains_point(line, x, y):
    # Your implementation here
    return True  # Placeholder

# # Example usage:
# left_eye_x = 100
# left_eye_y = 200
# right_eye_x = 300
# right_eye_y = 200
# screenshot_path = 'path/to/screenshot.jpg'

# line = perform_ocr(left_eye_x, left_eye_y, right_eye_x, right_eye_y, screenshot_path)
# print(line)


def extract_fixation_data_from_raw_table(user_id):
    try:
        # Read the raw data table for the specified user
        table_name = f"SCR_RAW_DATA_USER_{user_id}"
        raw_data_rows = read_raw_data_table(table_name)

        # Initialize a list to store fixation data
        fixation_data = []

        # Iterate through each row in the raw data table
        for row in raw_data_rows:
            if row['category_left'] == 'fixation' or row['category_right'] == 'fixation':
                fixation_data.append(row)

        return fixation_data
    except Exception as e:
        print(f"Error: Failed to extract fixation data from raw table: {e}")
        return []

import sqlite3

# def get_fixation_rows(user_id, db_path):
#     # Dynamically create the table name
#     table_name = f"SCR_RAW_DATA_USER_{user_id}"

#     # SQL query to select rows where either category_left or category_right is "Fixation"
#     query = f"""
#     SELECT *
#     FROM {table_name}
#     WHERE category_left = 'Fixation' OR category_right = 'Fixation';
#     """

#     # Connect to the database
#     conn = sqlite3.connect(db_path)
#     cursor = conn.cursor()

#     try:
#         # Execute the query
#         cursor.execute(query)

#         # Fetch all the results
#         fixation_rows = cursor.fetchall()

#         # Process the results (or return them)
#         for row in fixation_rows:
#             print(row)  # Print out each row that matches the condition
#             # You can replace this with any other processing you need

#         return fixation_rows

#     except sqlite3.Error as e:
#         print(f"Database error: {e}")
#     except Exception as e:
#         print(f"Exception in query: {e}")
#     finally:
#         # Close the cursor and connection
#         cursor.close()
#         conn.close()

# # Replace 'path_to_your_db' with the actual path to your SQLite database file
# db_path = 'dyslexiadetect.db'

# # Example usage:
# user_id = 1004  # Replace with the actual user_id
# fixation_data = get_fixation_rows(user_id, db_path)

from sqlalchemy import create_engine, MetaData
from sqlalchemy.orm import sessionmaker

def get_fixation_data(user_id, db_path):
    # Initialize the engine and sessionmaker
    engine = create_engine(db_path)
    Session = sessionmaker(bind=engine)
    session = Session()
    
    # Reflect the database schema into MetaData
    metadata = MetaData()
    metadata.reflect(bind=engine)
    
    # Construct the table name based on user_id
    table_name = f"SCR_RAW_DATA_USER_{user_id}"
    table = metadata.tables.get(table_name)
    
    # Check if the table exists
    if table is None:
        raise ValueError(f"Table {table_name} does not exist in the database.")
    
    # Prepare the query to get rows where either left or right category is Fixation
    query = session.query(table).filter(
        (table.c.category_left == 'Fixation') | (table.c.category_right == 'Fixation')
    )
    
    try:
        # Execute the query and return the results
        results = query.all()
        return results
    except Exception as e:
        session.rollback()
        raise
    finally:
        session.close()

# Example usage:
db_path = 'sqlite:///dyslexiadetect.db'  # Replace with your database path
user_id = 1004  # Replace with the user ID
try:
    fixation_data = get_fixation_data(user_id, db_path)
    for data in fixation_data:
        print(data)
except ValueError as e:
    print(e)
except Exception as e:
    print(f"An error occurred: {e}")

import os
db_path = 'sqlite:///dyslexiadetect.db'  # Replace with your database path
user_id = 1004  # Replace with the user ID
try:
    fixation_data = get_fixation_data(user_id, db_path)
    for data in fixation_data:
        print(data)
except ValueError as e:
    print(e)
except Exception as e:
    print(f"An error occurred: {e}")


def process_screenshots_for_ocr(data_folder, user_id, entries):
    # Assuming data_folder is the path to the directory containing user folders with screenshots
    user_folder = os.path.join(data_folder, f"{user_id}")
    
    for entry in entries:
        id, timestamp, left_x, left_y, right_x, right_y, gaze_direction, category_left, category_right = entry
        screenshot_filename = f"{timestamp}.jpg"  # Assuming screenshots are saved as '.png'
        screenshot_path = os.path.join(user_folder, screenshot_filename)
        
        # Check if screenshot file exists
        if os.path.isfile(screenshot_path):
            # Call your OCR function here with the screenshot and coordinates
            perform_ocr(left_x, left_y, right_x, right_y ,screenshot_path)
        else:
            print(f"Screenshot not found for timestamp {timestamp}")

# Example usage
data_folder = 'data'  # Replace with the path to your data folder
user_id = 1004  # Replace with the user ID

# Assuming 'fixation_data' contains your entries, e.g., from a database query


process_screenshots_for_ocr(data_folder, user_id, fixation_data)
