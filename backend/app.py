import secrets
from flask import Flask, request, jsonify
import database
import email_service
import os
import random
import hashlib
import csv
import os
import cv2
import numpy as np
from datetime import datetime
from flask import Flask, request, jsonify
from gaze_tracking import GazeTracking  # Assuming this is your gaze tracking library
from joblib import load
import numpy as np
import pandas as pd
from Features import extract_features
from Screening import process_image
from werkzeug.utils import secure_filename
from ScreeningRoutes import screening_results

app = Flask(__name__)

# Print environment variables
# print("MAIL_SERVER:", os.getenv('MAIL_SERVER'))
# print("MAIL_PORT:", os.getenv('MAIL_PORT'))
# print("MAIL_USERNAME:", os.getenv('MAIL_USERNAME'))
# print("MAIL_PASSWORD:", os.getenv('MAIL_PASSWORD'))
# print("MAIL_USE_TLS:", os.getenv('MAIL_USE_TLS'))
# print("MAIL_USE_SSL:", os.getenv('MAIL_USE_SSL'))

app.config['MAIL_SERVER'] = os.getenv('MAIL_SERVER')
app.config['MAIL_PORT'] = 465  # Using SSL
app.config['MAIL_USERNAME'] = os.getenv('MAIL_USERNAME')
app.config['MAIL_PASSWORD'] = os.getenv('MAIL_PASSWORD')
app.config['MAIL_USE_TLS'] = os.getenv('MAIL_USE_TLS') == 'True'
app.config['MAIL_USE_SSL'] = os.getenv('MAIL_USE_SSL') == 'True'

# print("Configured MAIL_PORT:", app.config['MAIL_PORT'])
# print("Configured MAIL_USE_TLS:", app.config['MAIL_USE_TLS'])
# print("Configured MAIL_USE_SSL:", app.config['MAIL_USE_SSL'])

email_service.init_email_service(app)  # Initialize email service with app
# print("Email service initialized")

@app.route('/signup', methods=['POST'])
def signup():
    print("Received signup request")
    data = request.json
    print("Request data:", data)

    email = data.get('email')
    name = data.get('name')
    username = data.get('username')
    age = data.get('age')
    password = data.get('password')

    print("Signup Data - Email:", email, "Name:", name, "Username:", username, "Age:", age)

    # Hash password
    hashed_password = hashlib.sha256(password.encode()).hexdigest()
    # print("Hashed Password:", hashed_password)

    if database.add_user(email, name, username, age, hashed_password):
        otp = random.randint(100000, 999999)
        print("Generated OTP:", otp)
        database.store_otp(email, str(otp))  # Store OTP
        email_service.send_otp_email(app, email, otp)  # Send OTP via email
        print("OTP sent via email")
        return jsonify({'message': 'OTP sent to email'}), 200
    else:
        print("User already exists or invalid data")
        return jsonify({'message': 'User already exists or invalid data'}), 400

@app.route('/verify', methods=['POST'])
def verify():
    print("Received verify request")
    data = request.json
    print("Verify Request Data:", data)

    email = data.get('email')
    otp = data.get('otp')

    print("Verify Data - Email:", email, "OTP:", otp)

    if database.verify_user(email, otp):
        print("User verified successfully")
        return jsonify({'message': 'User verified successfully'}), 200
    else:
        print("Invalid OTP or email")
        return jsonify({'message': 'Invalid OTP or email'}), 400
    

# @app.route('/signin', methods=['POST'])
# def signin():
#     data = request.json
#     email = data.get('email')
#     password = data.get('password')

#     # Hash the input password to compare with the stored hash
#     hashed_password = hashlib.sha256(password.encode()).hexdigest()

#     if database.authenticate_user(email, hashed_password):
#         return jsonify({'message': 'Sign in successful'}), 200
#     else:
#         return jsonify({'message': 'Invalid credentials or user not verified'}), 401
def generate_secure_token(user_id):
    # Generate a secure random token
    secure_token = secrets.token_hex(16)
    # Concatenate user_id and token
    token = f"{user_id}:{secure_token}"
    return token

@app.route('/signin', methods=['POST'])
def signin():
    data = request.json
    email = data.get('email')
    password = data.get('password')

    hashed_password = hashlib.sha256(password.encode()).hexdigest()
    user_id, auth_success = database.authenticate_user(email, hashed_password)  # Modified to return user_id
    print(user_id)

    if auth_success:
        # Generate a token that includes the user ID
        token = generate_secure_token(user_id)
        print(token)
        
        # Store the token in the database
        database.store_user_token(email, user_id , token)

        # Return the token to the client
        return jsonify({'message': 'Sign in successful', 'token': token}), 200
    else:
        return jsonify({'message': 'Invalid credentials or user not verified'}), 401



@app.route('/get_paragraph', methods=['POST'])
def get_paragraph():
    data = request.json
    age = data.get('age')

    # Determine age group based on age
    if age:
        if 4 <= age <= 6:
            age_group = '4-6'
        elif 7 <= age <= 8:
            age_group = '7-8'
        elif 9 <= age <= 12:
            age_group = '9-12'
        else:
            return jsonify({'error': 'Age not in valid range'}), 400

        paragraph, word_count = database.get_paragraph(age_group)
        if paragraph:
            return jsonify({'paragraph': paragraph, 'word_count': word_count})
        else:
            return jsonify({'error': 'No paragraph found for this age group'}), 404
    else:
        return jsonify({'error': 'Age is required'}), 400


######## Rhyming Activity ############


@app.route('/get_rhyming_task/<int:level>', methods=['GET'])
def get_rhyming_task(level):
    word, options = database.get_rhyming_task(level)
    if word:
        response = {'word': word, 'options': options}
        print(f"Sending response: {response}")  # Debugging log
        return jsonify(response), 200
    else:
        print("Sending error response: No task available for this level")  # Debugging log
        return jsonify({'message': 'No task available for this level'}), 404

@app.route('/submit_rhyming_answer', methods=['POST'])
def submit_rhyming_answer():
    data = request.json
    word = data.get('word')
    chosen_option = data.get('chosen_option')

    if database.validate_rhyming_answer(word, chosen_option):
        return jsonify({'result': 'correct'}), 200
    else:
        return jsonify({'result': 'incorrect'}), 200
    

@app.route('/update_score', methods=['POST'])
def update_score():
    data = request.json
    user_id = data.get('user_id')
    level = data.get('level')
    score = data.get('score')

    if database.update_user_score(user_id, level, score):
        return jsonify({'message': 'Score updated successfully'}), 200
    else:
        return jsonify({'message': 'Error updating score'}), 500
    

########## Word Harmony Activity ############

@app.route('/get_word_harmony_task/<int:level>', methods=['GET'])
def get_word_harmony_task(level):
    correct_word, sound_path, options = database.get_word_harmony_task(level)
    if correct_word:
        response = {
            'word': correct_word,
            'sound_path': sound_path,
            'options': options  # This is the new line to include options
        }
        return jsonify(response), 200
    else:
        return jsonify({'message': 'No task available for this level'}), 404


@app.route('/submit_word_harmony_answer', methods=['POST'])
def submit_word_harmony_answer():
    data = request.json
    word = data.get('word')
    chosen_word = data.get('chosen_word')

    if word == chosen_word:  # Simple equality check for validation
        return jsonify({'result': 'correct'}), 200
    else:
        return jsonify({'result': 'incorrect'}), 200


# Global variables for device screen width and height
device_screen_width = None
prev_pupil_data = {'left': None, 'right': None}
device_screen_height = None

# Directory setup for CSV storage
csv_directory = 'data'
csv_filename = 'new_gaze_data.csv'
csv_filepath = os.path.join(csv_directory, csv_filename)
if not os.path.exists(csv_directory):
    os.makedirs(csv_directory)

def write_to_csv(data):
    """Write the provided data to the CSV file."""
    file_exists = os.path.isfile(csv_filepath)
    is_empty = os.stat(csv_filepath).st_size == 0 if file_exists else False

    with open(csv_filepath, 'a', newline='') as csvfile:
        fieldnames = ['RecordingTime [ms]', 'Point of Regard Left X [px]', 'Point of Regard Left Y [px]', 'Point of Regard Right X [px]', 'Point of Regard Right Y [px]', 'gaze_direction', 'Category left', 'Category right']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        if not file_exists or is_empty:
            writer.writeheader()
        writer.writerow(data)
    
from datetime import datetime


#     """
#     Extracts and converts the timestamp in milliseconds from the request form.

#     Args:
#         request (flask.Request): The Flask request object.

#     Returns:
#         int: The timestamp in milliseconds or None if not found in the form.
#     """
#     timestamp_string = request.form.get('timestamp')
#     if timestamp_string is None:
#         # Use current time as fallback if timestamp not found
#         return int(datetime.now().timestamp() * 1000)

#     try:
#         # Parse the timestamp string assuming it's in a format like "HH:MM:SS.fff"
#         timestamp = datetime.strptime(timestamp_string, "%H:MM:SS.%f")
#         timestamp_in_ms = timestamp.timestamp() * 1000  # Convert to milliseconds
#         return int(timestamp_in_ms)
#     except ValueError:
#         print(f"Error: Invalid timestamp format. Expected format: HH:MM:SS.fff")
#         return None
def get_timestamp_in_milliseconds(request):
    """
    Extracts and converts the timestamp in milliseconds from the request body.

    Args:
        request (flask.Request): The Flask request object.

    Returns:
        int: The timestamp in milliseconds or None if not found in the body.
    """
    try:
        # Extract timestamp from the request body directly
        timestamp = request.get('timestamp')
        if timestamp is None:
            # Use current time as fallback if timestamp not found
            return int(datetime.now().timestamp() * 1000)
        return int(timestamp)
    except ValueError:
        print(f"Error: Invalid timestamp format.")
        return None


# Example usage (assuming your route has access to the request object)



@app.route('/sendDeviceMetrics', methods=['POST'])
def receive_device_metrics():
    """Endpoint to receive device metrics like screen width and height."""
    global device_screen_width, device_screen_height
    data = request.json
    device_screen_width = data['width']
    device_screen_height = data['height']
    return jsonify({"message": "Metrics received successfully"}), 200

def calculate_euclidean_distance(p1, p2):
    if not p1 or not p2:
        return np.inf  # Return infinity if either point is missing
    return np.sqrt((p1[0] - p2[0]) ** 2 + (p1[1] - p2[1]) ** 2)

def calculate_status(current, previous, threshold=5):
    if not previous:
        return "Unknown"
    distance = calculate_euclidean_distance(current, previous)
    return "Fixation" if distance <= threshold else "Saccade"
from ScreeningTable import insert_screening_data , insert_raw_screening_data
# @app.route('/process_frame', methods=['POST'])
# def process_frame():
#     """Process the uploaded frame for gaze tracking and save the data."""
#     if 'image' not in request.files:
#         return jsonify({'error': 'No file part'}), 400

#     timestamp = get_timestamp_in_milliseconds(request)
#     print(timestamp)
#     # file = request.files['image']
    
#     # nparr = np.frombuffer(file.read(), np.uint8)
#     # frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
#     # process_image(frame)
#     file = request.files['image']
#     in_memory_file = file.read()
#     nparr = np.frombuffer(in_memory_file, np.uint8)
#     frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
#     process_image(frame)
#     gaze = GazeTracking()
#     gaze.refresh(frame)
   

#     left_pupil = gaze.pupil_left_coords()
#     right_pupil = gaze.pupil_right_coords()
#     text = "Unable to determine gaze direction"
#     if gaze.is_blinking():
#         text = "Blinking"
#     elif gaze.is_right():
#         text = "Looking right"
#     elif gaze.is_left():
#         text = "Looking left"
#     elif gaze.is_center():
#         text = "Looking center"
    
#     # Validate device metrics availability
#     if device_screen_width is None or device_screen_height is None:
#         return jsonify({'error': 'Device metrics not received'}), 400

#     # Scale factors for target resolution
#     target_width = 1280
#     target_height = 1024
#     scaleX = target_width / device_screen_width
#     scaleY = target_height / device_screen_height
#     left_pupil = gaze.pupil_left_coords()
#     right_pupil = gaze.pupil_right_coords()
#     # Scale pupil coordinates
#     scaled_left_x = int(left_pupil[0] * scaleX) if left_pupil else -1
#     scaled_left_y = int(left_pupil[1] * scaleY) if left_pupil else -1
#     scaled_right_x = int(right_pupil[0] * scaleX) if right_pupil else -1
#     scaled_right_y = int(right_pupil[1] * scaleY) if right_pupil else -1
    
#     # Calculate the status based on Euclidean distance
#     threshold = 30  # Adjust this fixation threshold as needed (in pixels)
#     status_L = calculate_status(left_pupil, prev_pupil_data['left'], threshold)
#     status_R = calculate_status(right_pupil, prev_pupil_data['right'], threshold)
#     # At the end of the process_frame function, before returning the response, add:
#     prev_pupil_data['left'] = left_pupil
#     prev_pupil_data['right'] = right_pupil

#     token = request.headers.get('Authorization', '').split(" ")[1]
#     user_id = token.split(":")[0]
#     print(f"Token: {token}, User ID: {user_id}")
     
#     data = {
#             'recording_time': timestamp,
#             'point_of_regard_left_x': scaled_left_x, 
#             'point_of_regard_left_y': scaled_left_y,
#             'point_of_regard_right_x': scaled_right_x, 
#             'point_of_regard_right_y': scaled_right_y,
#             'gaze_direction': text,
#             'category_left': status_L,
#             'category_right': status_R,
#         }
#     print(f"Attempting to insert data for user {user_id}: {data}")
#     insert_screening_data(user_id, data)  # Assuming this function is defined elsewhere
#     return jsonify({'status': 'success', 'timestamp': timestamp})
#  except Exception as e:
#         print(f"Error: Failed to process frame and insert data: {e}")
#         return jsonify({'error': f'Failed to insert data: {str(e)}'}), 500
    # Determine category (e.g., fixation, saccade) based on gaze_tracking library output or additional logic
     # Get user ID from request headers (assuming it's provided as a token)
    
   
#     data = {
#     'recording_time': timestamp,
#     'point_of_regard_left_x': scaled_left_x, 
#     'point_of_regard_left_y': scaled_left_y,
#     'point_of_regard_right_x': scaled_right_x, 
#     'point_of_regard_right_y': scaled_right_y,
#     'gaze_direction': text,
#     'category_left': status_L,
#     'category_right': status_R,
# }
#     token = request.headers.get('Authorization').split(" ")[1]
#     print(token)
#     # decoded = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
#     user_id = token.split(":")[0]
#     print(user_id)
#     try:
#         insert_screening_data(user_id, data)
#         return jsonify({'status': 'success', 'timestamp': timestamp})
#     except Exception as e:
#         return jsonify({'error': f'Failed to insert data: {str(e)}'}), 500
#     # write_to_csv(data)
            

    # return jsonify({'status': 'success', 'data': data, 'timestamp': timestamp})

# @app.route('/process_frame', methods=['POST'])
# def process_frame():
#     """Process the uploaded frame for gaze tracking and save the data."""
#     try:
#        if 'frame' not in request.files or 'screenshot' not in request.files:
#             return jsonify({'error': 'No file part'}), 400

#        timestamp = get_timestamp_in_milliseconds(request)
#        save_screenshot_from_request(request)
#        print(timestamp)
#         # file = request.files['image']
        
#         # nparr = np.frombuffer(file.read(), np.uint8)
#         # frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
#         # process_image(frame)
#         file = request.files['image']
#         in_memory_file = file.read()
#         nparr = np.frombuffer(in_memory_file, np.uint8)
#         frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
#         process_image(frame)
#         gaze = GazeTracking()
#         gaze.refresh(frame)
       
       
#         left_pupil = gaze.pupil_left_coords()
#         right_pupil = gaze.pupil_right_coords()
#         text = "Unable to determine gaze direction"
#         if gaze.is_blinking():
#             text = "Blinking"
#         elif gaze.is_right():
#             text = "Looking right"
#         elif gaze.is_left():
#             text = "Looking left"
#         elif gaze.is_center():
#             text = "Looking center"
        
#         # Validate device metrics availability
#         if device_screen_width is None or device_screen_height is None:
#             return jsonify({'error': 'Device metrics not received'}), 400

#         # Scale factors for target resolution
#         target_width = 1280
#         target_height = 1024
#         scaleX = target_width / device_screen_width
#         scaleY = target_height / device_screen_height
#         left_pupil = gaze.pupil_left_coords()
#         right_pupil = gaze.pupil_right_coords()
#         # Scale pupil coordinates
#         scaled_left_x = int(left_pupil[0] * scaleX) if left_pupil else -1
#         scaled_left_y = int(left_pupil[1] * scaleY) if left_pupil else -1
#         scaled_right_x = int(right_pupil[0] * scaleX) if right_pupil else -1
#         scaled_right_y = int(right_pupil[1] * scaleY) if right_pupil else -1
        
#         # Calculate the status based on Euclidean distance
#         threshold = 30  # Adjust this fixation threshold as needed (in pixels)
#         status_L = calculate_status(left_pupil, prev_pupil_data['left'], threshold)
#         status_R = calculate_status(right_pupil, prev_pupil_data['right'], threshold)
#         # At the end of the process_frame function, before returning the response, add:
#         prev_pupil_data['left'] = left_pupil
#         prev_pupil_data['right'] = right_pupil

#         token = request.headers.get('Authorization', '').split(" ")[1]
#         user_id = token.split(":")[0]
#         print(f"Token: {token}, User ID: {user_id}")
         
#         data = {
#            'recording_time': timestamp,
#             'point_of_regard_left_x': scaled_left_x, 
#     'point_of_regard_left_y': scaled_left_y,
#     'point_of_regard_right_x': scaled_right_x, 
#     'point_of_regard_right_y': scaled_right_y,
#     'gaze_direction': text,
#     'category_left': status_L,
#     'category_right': status_R,
# }
#         print(f"Attempting to insert data for user {user_id}: {data}")
#         insert_screening_data(user_id, data)  # Assuming this function is defined elsewhere
#         return jsonify({'status': 'success', 'timestamp': timestamp})
#     except Exception as e:
#         print(f"Error: Failed to process frame and insert data: {e}")
#         return jsonify({'error': f'Failed to insert data: {str(e)}'}), 500

# 
@app.route('/process_frame', methods=['POST'])
def process_frame():
    try:
        if 'image' not in request.files or 'screenshot' not in request.files:
            return jsonify({'error': 'No file part'}), 400

        # timestamp = get_timestamp_in_milliseconds(request)
        timestamp = int(datetime.now().timestamp() * 1000)
        print("Timestamp generated on the server:", timestamp)

        # Your existing code continues here...
        message = save_screenshot_from_request(request , timestamp)
        print(message) 
        print(timestamp)
        
        frame_file = request.files['image']
        in_memory_file = frame_file.read()
        nparr = np.frombuffer(in_memory_file, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        process_image(frame)
        gaze = GazeTracking()
        gaze.refresh(frame)

        left_pupil = gaze.pupil_left_coords()
        right_pupil = gaze.pupil_right_coords()
        text = "Unable to determine gaze direction"
        if gaze.is_blinking():
            text = "Blinking"
        elif gaze.is_right():
            text = "Looking right"
        elif gaze.is_left():
            text = "Looking left"
        elif gaze.is_center():
            text = "Looking center"
        
        # Mocked device screen metrics
        device_screen_width = 1920
        device_screen_height = 1080
        
        # Scale factors for target resolution
        target_width = 1280
        target_height = 1024
        scaleX = target_width / device_screen_width
        scaleY = target_height / device_screen_height
        
        # Scale pupil coordinates
        scaled_left_x = int(left_pupil[0] * scaleX) if left_pupil else -1
        scaled_left_y = int(left_pupil[1] * scaleY) if left_pupil else -1
        scaled_right_x = int(right_pupil[0] * scaleX) if right_pupil else -1
        scaled_right_y = int(right_pupil[1] * scaleY) if right_pupil else -1
        
        # Calculate the status based on Euclidean distance
        threshold = 30  # Adjust this fixation threshold as needed (in pixels)
        status_L = calculate_status(left_pupil, prev_pupil_data['left'], threshold)
        status_R = calculate_status(right_pupil, prev_pupil_data['right'], threshold)
        
        # At the end of the process_frame function, before returning the response, add:
        prev_pupil_data['left'] = left_pupil
        prev_pupil_data['right'] = right_pupil

        token = request.headers.get('Authorization', '').split(" ")[1]
        user_id = token.split(":")[0]
        print(f"Token: {token}, User ID: {user_id}")
         
        # Raw data before scaling
        raw_data = {
            'recording_time': timestamp,
            'point_of_regard_left_x': left_pupil[0] if left_pupil else -1,  # Raw left pupil X
            'point_of_regard_left_y': left_pupil[1] if left_pupil else -1,  # Raw left pupil Y
            'point_of_regard_right_x': right_pupil[0] if right_pupil else -1,  # Raw right pupil X
            'point_of_regard_right_y': right_pupil[1] if right_pupil else -1,  # Raw right pupil Y
            'gaze_direction': text,
            'category_left': status_L,  # Assuming status_L can be determined without scaling
            'category_right': status_R,  # Assuming status_R can be determined without scaling
        }

        data = {
            'recording_time': timestamp,
            'point_of_regard_left_x': scaled_left_x, 
            'point_of_regard_left_y': scaled_left_y,
            'point_of_regard_right_x': scaled_right_x, 
            'point_of_regard_right_y': scaled_right_y,
            'gaze_direction': text,
            'category_left': status_L,
            'category_right': status_R,
        }
        print(f"Attempting to insert data for user {user_id}: {data}")
        insert_screening_data(user_id, data)  # Assuming this function is defined elsewhere
        print(f"Attempting to insert RAW data for user {user_id}: {raw_data}")
        insert_raw_screening_data(user_id, raw_data)  # Assuming this function is defined elsewhere
        return jsonify({'status': 'success', 'timestamp': timestamp})
    except Exception as e:
        print(f"Error: Failed to process frame and insert data: {e}")
        return jsonify({'error': f'Failed to insert data: {str(e)}'}), 500



# @app.route('/predict', methods=['GET'])
# def predict():
#     csv_file_path = 'data/gaze_data.csv'  # Specify the path to your CSV file
#     data_df = pd.read_csv(csv_file_path)  # Read data from CSV into DataFrame
#     extracted_features = extract_features(data_df)  # Extract features from the DataFrame
#     features_array = [list(extracted_features.values())]  # Prepare features for the model
#     svm_model = load('svm_model.joblib')  # Load your SVM model
#     prediction = svm_model.predict(features_array)
#     print(prediction)  # Make prediction
#     return jsonify({'prediction': prediction.tolist()})





app.register_blueprint(screening_results)


# ------------------screenshot fucntion ------------------------

from flask import request
import os
from datetime import datetime
import werkzeug

def save_screenshot_from_request(req , timestamp):
    """
    Extracts a screenshot from the request and saves it in a directory based on the user ID.

    :param req: The request object from Flask
    :return: A message indicating the success/failure of the operation
    """
    try:
        # Check if the screenshot part is in the request
        if 'screenshot' not in req.files:
            return "Missing screenshot part in the request.", 400

        # Extract the screenshot file from the request
        screenshot_file = req.files['screenshot']
        
        # Ensure the file is not empty
        if screenshot_file.filename == '':
            return "No selected file.", 400

        # Ensure the file is a valid image
        if not (screenshot_file and allowed_file(screenshot_file.filename)):
            return "Invalid file format.", 400

        # Extract the user ID from the request's header
        token = request.headers.get('Authorization', '').split(" ")[1]
        user_id = token.split(":")[0]
        print(f"Token: {token}, User ID: {user_id}")

        # Define the directory path based on the user ID
        user_dir = os.path.join('data', user_id)
        os.makedirs(user_dir, exist_ok=True)  # Create the directory if it doesn't exist

        # Define the file path for the screenshot
        # timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
        screenshot_path = os.path.join(user_dir, f"{timestamp}.jpg")

        # Save the screenshot
        screenshot_file.save(screenshot_path)

        return f"Screenshot saved successfully at {screenshot_path}.", 200
    except Exception as e:
        return f"Error saving screenshot: {str(e)}", 500

def allowed_file(filename):
    """
    Checks if the uploaded file is allowed based on its extension.

    :param filename: The name of the file
    :return: True if the file is allowed, False otherwise
    """
    ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.before_request
def before_request_func():
    print("Received request: ", request.url, request.method)



# -------------------------------------------------------------------


if __name__ == '__main__':
    print("Starting Flask app")
    database.create_database()  # Ensure the database is set up

    print("Database created")
    app.run(debug=True , host='0.0.0.0' , port='5000')

