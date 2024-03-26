
from flask import Flask, request, jsonify
from joblib import load
import numpy as np
import pandas as pd



def extract_features(data):
    """
    Extracts features from eye-tracking data, handling missing columns and calculating various metrics.
    Data is sorted by 'RecordingTime [ms]' to ensure chronological order for accurate calculation of differences.
    Args:
        data (pd.DataFrame): DataFrame containing eye-tracking data.
    Returns:
        dict: A dictionary of extracted features or None if essential data is missing.
    """
    # Sort the DataFrame by recording time to ensure chronological order
    data = data.sort_values(by="RecordingTime [ms]", ascending=True)
    data['RecordingTime [ms]'] = pd.to_numeric(data['RecordingTime [ms]'], errors='coerce')
    
    features = {}

    # Fixation features
    try:
        left_fixations = data[data["Category Left"] == "Fixation"]
        right_fixations = data[data["Category Right"] == "Fixation"]
        features["left_fixations_count"] = len(left_fixations)
        features["right_fixations_count"] = len(right_fixations)
        features["left_avg_fixation_duration"] = left_fixations["RecordingTime [ms]"].diff().dropna().mean() if len(left_fixations) > 1 else 0
        features["right_avg_fixation_duration"] = right_fixations["RecordingTime [ms]"].diff().dropna().mean() if len(right_fixations) > 1 else 0
    except KeyError as e:
        print(f"Warning: Missing fixation data for analysis. Error: {e}")

    # Saccade features
    try:
        left_saccades = data[data["Category Left"] == "Saccade"]
        right_saccades = data[data["Category Right"] == "Saccade"]
        features["left_saccades_count"] = len(left_saccades)
        features["right_saccades_count"] = len(right_saccades)
        features["left_avg_saccade_length"] = np.sqrt((left_saccades["Point of Regard Right X [px]"].diff() ** 2 + left_saccades["Point of Regard Right Y [px]"].diff() ** 2).mean()) if len(left_saccades) > 1 else 0
        features["right_avg_saccade_length"] = np.sqrt((right_saccades["Point of Regard Right X [px]"].diff() ** 2 + right_saccades["Point of Regard Right Y [px]"].diff() ** 2).mean()) if len(right_saccades) > 1 else 0
        features["age"] = 10
    except KeyError as e:
        print(f"Warning: Missing saccade data for analysis. Error: {e}")

    if features:  # Check if any features were extracted
        return features
    else:
        return None
    

csv_file_path = 'data/gaze_data.csv'  # Replace with your CSV file path
data = pd.read_csv(csv_file_path)

# Extract features from the data
extracted_features = extract_features(data)
print(extract_features(data))
svm_model = load('svm_model.joblib')

# Assuming extracted_features is a dictionary with your features
features_array = np.array(list(extracted_features.values())).reshape(1, -1) # Reshape for a single sample

# Predict using the SVM model
predicted_label = svm_model.predict(features_array)
print(f"Predicted Label: {predicted_label[0]}")