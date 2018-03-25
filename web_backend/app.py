# Imported the Flask class
from flask import Flask, request, jsonify
import numpy as np
import librosa
import sys
from keras.models import load_model
import time
import os
from werkzeug.utils import secure_filename
from flask_cors import CORS, cross_origin
# sys.path.append('../')
# import initial_histogram as histogram
# import setup
# import random
# Made a new instance of the Flask class, with name of the module being the
# variable so Flask knows attributes, files, etc. The reason we use '__name__'
# instead of '__main__' is because if this file was imported, it wouldn't be
# main anymore, which would cause a whole lot of errors. '__name__' is dynamic
app = Flask(__name__)
CORS(app)

UPLOAD_FOLDER = './uploads/'
ALLOWED_EXTENSIONS = set(['wav'])

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Creating a new base/home http route
@app.route("/")
def random_sentence():
    print("Root was called")
    return hello_world()
    # return setup.test(1)
    # index = random.randint(1, 10)
    # return histogram.random_word_histogram_with_word_frequency_factor(index)

def hello_world():
    return 'Hello, World!'

@app.route("/upload-audio", methods=["POST", "PUT"])
@cross_origin(origin="http://localhost:9090")
def get_classification():
    # print(request, file=sys.stderr)
    if request.method == 'GET':
        return "Wrong location!"
    elif request.method == 'POST':
        wave_file = request.files['file']
        model = load_model("my_model.h5")
        filename = str(time.time()).replace('.', '')
        filename = secure_filename(filename + ".wav")
        location = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        wave_file.save(location)
        result = predict(location, model=model)
        os.remove(location)
        data = {
            'classification': result
        }
        print(result)
        if result == 'gun':
            return jsonify(data), 200
        else:
            return jsonify(data), 201

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

# Predicts one sample
def predict(filepath, model):
    sample = wav2mfcc(filepath)
    print("wav2mfcc worked")
    feature_dim_2 = 11
    feature_dim_1 = 20
    channel = 1
    sample_reshaped = sample.reshape(1, feature_dim_1, feature_dim_2, channel)
    prediction = model.predict(sample_reshaped)
    final_prediction = np.argmax(prediction)
    print(prediction)
    print(final_prediction)
    return get_labels()[0][(final_prediction)]

def get_labels():
    return (['gun', 'notgun', 'notgunshot'], [0, 1, 2], [[1., 0., 0.],
       [0., 1., 0.],
       [0., 0., 1.]])

# Handy function to convert wav2mfcc
def wav2mfcc(file_path, max_len=11):
    wave, sr = librosa.load(file_path, mono=True, sr=None)
    wave = wave[::3]
    mfcc = librosa.feature.mfcc(wave, sr=16000)

    # If maximum length exceeds mfcc lengths then pad the remaining ones
    if (max_len > mfcc.shape[1]):
        pad_width = max_len - mfcc.shape[1]
        mfcc = np.pad(mfcc, pad_width=((0, 0), (0, pad_width)), mode='constant')

    # Else cutoff the remaining parts
    else:
        mfcc = mfcc[:, :max_len]

    return mfcc

if __name__ == "__main__":
    app.run()
