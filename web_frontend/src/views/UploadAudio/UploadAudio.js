import React, { Component } from 'react';
// import { ReactMic } from 'react-mic';
// import Recorder from 'react-recorder'
import AudioRecorder from 'react-audio-recorder';
import Modal from 'react-responsive-modal';

class UploadAudio extends Component {
  constructor(props) {
    super(props)
    this.handleUploadImage = this.handleUploadImage.bind(this);
    this.onChange = this.onChange.bind(this)
    this.state = {
      open: false,
      gunAlert: false
    };

    this.handleOpenModal = this.handleOpenModal.bind(this);
    this.handleCloseModal = this.handleCloseModal.bind(this);
  }

  handleOpenModal (gunAlert) {
    this.setState({ open: true, gunAlert });
  }

  handleCloseModal () {
    this.setState({ open: false });
  }

  handleUploadImage(ev) {
    ev.preventDefault();
    const data = new FormData();
    data.append('file', this.uploadInput.files[0]);
    console.log(this.uploadInput.files[0])
    fetch('http://127.0.0.1:8000/upload-audio', {
      method: 'POST',
      body: data,
    }).then((response) => {
      console.log("This is the response (promise) from upload: ", response["status"]);
      if (response["status"] == 200) {
        this.handleOpenModal(true)
      } else {
        this.handleOpenModal(false)
      }
      return response.json();
    }).then((data) => {
        console.log("Here 1: ", data.status)
      }).then(res => {
        console.log("Here2: ")
      })
  }



  onChange(e) {
  }


  render() {
    return (
      <div>
        <Modal open={this.state.open} onClose={this.handleCloseModal} little>
          <h2>{this.state.gunAlert ? "Significant gun fire was detected!" : "No gun fire was detected"}</h2>
        </Modal>

        <AudioRecorder onChange={this.onChange} />
      <form onSubmit={this.handleUploadImage}>
        <div>
          <input ref={(ref) => { this.uploadInput = ref; }} type="file" accept=".wav" />
        </div>
        <br />
        <div>
          <button>Upload</button>
        </div>
      </form>
    </div>
    );
  }
}

export default UploadAudio;
