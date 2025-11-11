import tensorflow as tf
import numpy as np

# Load the model
interpreter = tf.lite.Interpreter(model_path='app/src/main/assets/model1.tflite')
interpreter.allocate_tensors()

# Get input and output details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

print("Model Input Details:")
for detail in input_details:
    print(detail)

print("\nModel Output Details:")
for detail in output_details:
    print(detail)

# Try to get metadata if available
try:
    metadata = interpreter.get_metadata()
    print("\nMetadata:")
    for key, value in metadata.items():
        print(f"{key}: {value}")
except:
    print("No metadata found")

# For object detection, labels might be in associated files, but check if there's a labelmap.txt
try:
    with open('app/src/main/assets/labelmap.txt', 'r') as f:
        labels = f.read().splitlines()
        print("\nLabels from labelmap.txt:")
        for i, label in enumerate(labels):
            print(f"{i}: {label}")
except FileNotFoundError:
    print("No labelmap.txt found")

# If it's a task library model, labels might be embedded
# But for basic inspection, this is it