from flask import Blueprint, request, jsonify

from ScreeningTable import add_initial_screening_result , update_final_screening_result # Make sure to import your secret key for JWT decoding

screening_results = Blueprint('screening_results', __name__)

@screening_results.route('/InitialScreeningResult', methods=['POST'])
def initial_screening_result():
    # Decode the JWT token to get the user ID
    token = request.headers.get('Authorization').split(" ")[1]
    print(token)
    # decoded = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
    user_id = token.split(":")[0]
    
    # Extract the result from the request data
    data = request.get_json()
    initial_label = data.get('result')

    # Use the add_initial_screening_result function to store the result
    try:
        add_initial_screening_result(user_id, initial_label)
        return jsonify({"message": "Initial screening result successfully recorded."}), 200
    except Exception as e:
        return jsonify({"error": "An error occurred while recording the screening result."}), 500



from flask import jsonify
import pandas as pd
import sqlite3
from Features import extract_features
from joblib import load

# @screening_results.route('/predict', methods=['GET'])
# def predict():
   
#     # Construct table name dynamically
#     token = request.headers.get('Authorization').split(" ")[1]
#     print(token)
#     # decoded = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
#     user_id = token.split(":")[0]
#     table_name = f"SCR_DATA_USER_{user_id}" 
#     # Connect to the SQLite database
#     conn = sqlite3.connect('dyslexiadetect.db')
#     cursor = conn.cursor()
    
#     # Query data from the user-specific table
#     query = f"SELECT * FROM {table_name}"
#     data_df = pd.read_sql_query(query, conn)
    
#     # Ensure you close the connection to the database
#     conn.close()

#     if not data_df.empty:
#         extracted_features = extract_features(data_df)  # Assuming you have a function to extract features
#         features_array = [list(extracted_features.values())]  # Prepare features for the model
#         svm_model = load('svm_model.joblib')  # Load your SVM model (adjust path as needed)
#         prediction = svm_model.predict(features_array)
#         update_final_screening_result(user_id, prediction.tolist()[0])
#         return jsonify({'prediction': prediction.tolist()})
#     else:
#         return jsonify({'error': 'No data found for prediction'}), 404
@screening_results.route('/predict', methods=['GET'])
def predict():
    # Extract token from the Authorization header
    token = request.headers.get('Authorization').split(" ")[1]
    user_id = token.split(":")[0]  # Assuming the user_id is the first part of the token
    
    # Connect to the SQLite database
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    
    # Construct the table name dynamically based on the user_id
    table_name = f"SCR_DATA_USER_{user_id}"
    
    # Query data from the user-specific table
    query = f"SELECT * FROM {table_name}"
    data_df = pd.read_sql_query(query, conn)
    
    # Ensure you close the connection to the database
    conn.close()

    # Check if data is not empty
    if not data_df.empty:
        # Adjust DataFrame column names to match those expected by extract_features
        data_df.rename(columns={
            'recording_time': 'RecordingTime [ms]',
            'point_of_regard_left_x': 'Point of Regard Left X [px]',
            'point_of_regard_left_y': 'Point of Regard Left Y [px]',
            'point_of_regard_right_x': 'Point of Regard Right X [px]',
            'point_of_regard_right_y': 'Point of Regard Right Y [px]',
            'gaze_direction': 'Gaze Direction',
            'category_left': 'Category Left',
            'category_right': 'Category Right'
        }, inplace=True)
        
        extracted_features = extract_features(data_df)  # Assuming you have a function to extract features
        features_array = [list(extracted_features.values())]  # Prepare features for the model
        svm_model = load('svm_model.joblib')  # Load your SVM model (adjust path as needed)
        prediction = svm_model.predict(features_array)
        update_final_screening_result(user_id, prediction.tolist()[0])
        return jsonify({'prediction': prediction.tolist()})
    else:
        return jsonify({'error': 'No data found for prediction'}), 404