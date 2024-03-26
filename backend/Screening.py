# import csv
# import os
# import cv2
# import numpy as np
# from flask import Flask, request, jsonify
# from gaze_tracking import GazeTracking

# app = Flask(__name__)

# # Initialize previous pupil coordinates
# prev_pupil_data = {'left': None, 'right': None}

# def write_to_csv(data):
#     csv_file = 'gaze_data.csv'
#     file_exists = os.path.isfile(csv_file)
#     with open(csv_file, 'a', newline='') as csvfile:
#         fieldnames = ['left_pupil', 'right_pupil', 'gaze_direction', 'status_L', 'status_R', 'marked_frame']
#         writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
#         if not file_exists:
#             writer.writeheader()
#         writer.writerow(data)

# def process_eye_movement(current, previous):
#     if previous is None:
#         return "Unknown"  # Initial state, no previous data to compare
#     if current == previous:
#         return "Fixation"
#     return "Saccade"

# @app.route('/process_frame', methods=['POST'])
# def process_frame():
#     global prev_pupil_data
    
#     # Check if an image was uploaded
#     if 'image' not in request.files:
#         return jsonify({'error': 'No file part'}), 400

#     file = request.files['image']
#     in_memory_file = file.read()
#     nparr = np.frombuffer(in_memory_file, np.uint8)
#     frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    
#     gaze = GazeTracking()
#     gaze.refresh(frame)

#     left_pupil = gaze.pupil_left_coords()
#     right_pupil = gaze.pupil_right_coords()

#     # Determine the gaze direction
#     if gaze.is_blinking():
#         text = "Blinking"
#     elif gaze.is_right():
#         text = "Looking right"
#     elif gaze.is_left():
#         text = "Looking left"
#     elif gaze.is_center():
#         text = "Looking center"
#     else:
#         text = "Looking direction not determined"

#     status_L = process_eye_movement(left_pupil, prev_pupil_data['left'])
#     status_R = process_eye_movement(right_pupil, prev_pupil_data['right'])

#     prev_pupil_data['left'] = left_pupil
#     prev_pupil_data['right'] = right_pupil

#     # Save the frame with marked pupils (if applicable)
#     # Assuming saving logic is implemented here

#     data = {
#         'left_pupil': str(left_pupil),
#         'right_pupil': str(right_pupil),
#         'gaze_direction': text,  # Update with actual gaze direction
#         'status_L': status_L,
#         'status_R': status_R,
#         'marked_frame': "path/to/marked_frame"  # Adjust with actual path
#     }

#     write_to_csv(data)

#     response = {
#         'status': 'success',
#         'data': data
#     }
#     return jsonify(response)




# if __name__ == '__main__':
#     app.run(debug=True, host='0.0.0.0', port=5000)  # Adjust host and port as needed
import csv
import os
import cv2
import numpy as np
from flask import Flask, request, jsonify
from gaze_tracking import GazeTracking

# Global variable to store previous pupil coordinates for comparison
prev_pupil_data = {'left': None, 'right': None}

# Ensure the directory for the CSV exists
csv_directory = 'data'
csv_filename = 'gaze_data2.csv'
csv_filepath = os.path.join(csv_directory, csv_filename)

if not os.path.exists(csv_directory):
    os.makedirs(csv_directory)

def write_to_csv(data):
    # Determine if the CSV file needs headers
    file_exists = os.path.isfile(csv_filepath)
    with open(csv_filepath, 'a', newline='') as csvfile:
        fieldnames = ['left_pupil_x', 'left_pupil_y', 'right_pupil_x', 'right_pupil_y', 'gaze_direction', 'status_L', 'status_R']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        if not file_exists:
            writer.writeheader()
        writer.writerow(data)

def calculate_euclidean_distance(p1, p2):
  """
  Calculates the Euclidean distance between two points.

  Args:
      p1: A tuple representing the coordinates of the first point (x, y).
      p2: A tuple representing the coordinates of the second point (x, y).

  Returns:
      The Euclidean distance between the two points.
  """
  if not p1 or not p2:
    return np.inf  # Return infinity if either point is missing
  return np.sqrt(np.sum(np.square(np.subtract(p1, p2))))

def calculate_status(current, previous, threshold=5):
  """
  Calculates the status (fixation or saccade) based on Euclidean distance.

  Args:
      current: A tuple representing the coordinates of the current pupil (x, y).
      previous: A tuple representing the coordinates of the previous pupil (x, y).
      threshold: The threshold distance for considering fixation (default: 5 pixels).

  Returns:
      "Fixation" if the distance is less than or equal to the threshold, "Saccade" otherwise.
  """
  if not previous:
    return "Unknown"
  distance = calculate_euclidean_distance(current, previous)
  return "Fixation" if distance <= threshold else "Saccade"

# @app.route('/process_frame', methods=['POST'])
# def process_frame():
#     global prev_pupil_data
#     if 'image' not in request.files:
#         return jsonify({'error': 'No file part'}), 400

#     file = request.files['image']
#     in_memory_file = file.read()
#     nparr = np.frombuffer(in_memory_file, np.uint8)
#     frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

#     gaze = GazeTracking()
#     gaze.refresh(frame)

#     left_pupil = gaze.pupil_left_coords()
#     right_pupil = gaze.pupil_right_coords()

#     # Extracting X, Y coordinates for left and right pupils
#     left_x, left_y = left_pupil if left_pupil else (-1, -1)
#     right_x, right_y = right_pupil if right_pupil else (-1, -1)

#     # Calculate the status based on Euclidean distance
#     threshold = 30  # Adjust this fixation threshold as needed (in pixels)
#     status_L = calculate_status(left_pupil, prev_pupil_data['left'], threshold)
#     status_R = calculate_status(right_pupil, prev_pupil_data['right'], threshold)

#     prev_pupil_data = {'left': left_pupil, 'right': right_pupil}

#     # Determine gaze direction based on available methods (assuming it's still needed)
#     gaze_direction = "Unknown"  # Default value
#     if gaze.is_blinking():
#         gaze_direction = "Blinking"
#     elif gaze.is_right():
#         gaze_direction = "Looking right"
#     elif gaze.is_left():
#         gaze_direction = "Looking left"
#     elif gaze.is_center():
#         gaze_direction = "Looking center"

#     data = {
#         'left_pupil_x': left_x, 'left_pupil_y': left_y,
#         'right_pupil_x': right_x, 'right_pupil_y': right_y,
#         'gaze_direction': gaze_direction,
#         'status_L': status_L, 'status_R': status_R,
#     }

#     write_to_csv(data)

#     return jsonify({'status': 'success', 'data': data})

def process_image(image_data):
   
    # nparr = np.frombuffer(image_data, np.uint8)
    # frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    gaze = GazeTracking()
    gaze.refresh(image_data)

    left_pupil = gaze.pupil_left_coords()
    right_pupil = gaze.pupil_right_coords()

    left_x, left_y = left_pupil if left_pupil else (-1, -1)
    right_x, right_y = right_pupil if right_pupil else (-1, -1)

    threshold = 30  # Adjust this fixation threshold as needed (in pixels)
    status_L = calculate_status(left_pupil, prev_pupil_data['left'], threshold)
    status_R = calculate_status(right_pupil, prev_pupil_data['right'], threshold)

    prev_pupil_data['left'] = left_pupil
    prev_pupil_data['right'] = right_pupil

    gaze_direction = "Unknown"  # Default value based on gaze detection logic
    if gaze.is_blinking():
        gaze_direction = "Blinking"
    elif gaze.is_right():
        gaze_direction = "Looking right"
    elif gaze.is_left():
        gaze_direction = "Looking left"
    elif gaze.is_center():
        gaze_direction = "Looking center"

    data = {
        'left_pupil_x': left_x, 'left_pupil_y': left_y,
        'right_pupil_x': right_x, 'right_pupil_y': right_y,
        'gaze_direction': gaze_direction,
        'status_L': status_L, 'status_R': status_R,
    }
    write_to_csv(data)

    # Here, instead of writing to CSV or sending a response, we return the data for further use
    return data