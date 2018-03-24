# Imported the Flask class
from flask import Flask
# import sys
# sys.path.append('../')
import initial_histogram as histogram
import setup
import random
# Made a new instance of the Flask class, with name of the module being the
# variable so Flask knows attributes, files, etc. The reason we use '__name__'
# instead of '__main__' is because if this file was imported, it wouldn't be
# main anymore, which would cause a whole lot of errors. '__name__' is dynamic
app = Flask(__name__)

# Creating a new base/home http route
@app.route("/")
def random_sentence():
    return setup.test(1)
    # index = random.randint(1, 10)
    # return histogram.random_word_histogram_with_word_frequency_factor(index)

def hello_world():
    return 'Hello, World!'

if __name__ == "__main__":
    app.run()
