import React, { Component } from 'react';

class UploadAudio extends Component {
  constructor(props) {
    super(props)
    this.handleUploadImage = this.handleUploadImage.bind(this);
  }

  handleUploadImage(ev) {
    ev.preventDefault();

    const data = new FormData();
    data.append('file', this.uploadInput.files[0]);
    data.append('filename', this.fileName.value);

    fetch('https://scouterscout-backend.herokuapp.com/upload-audio', {
      method: 'POST',
      body: data,
    }).then((response) => {
      console.log("This is the response from upload: ", response);
    });
  }


  render() {
    return (
      <form onSubmit={this.handleUploadImage}>
        <div>
          <input ref={(ref) => { this.uploadInput = ref; }} type="file" accept=".wav" />
        </div>
        <br />
        <div>
          <button>Upload</button>
        </div>
      </form>
    );
  }
}

export default UploadAudio;
