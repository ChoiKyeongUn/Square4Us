<template>
  <!-- <div v-if="!state.isReady">
    <BeforeMeeting @enter="enter" @switchSession="switchSession" @exit="exit" />
    선택한 세부 세션 : {{ state.sessionNum }}
  </div> -->
  <h1 id="session-title" class="title">{{ state.mySessionId }}</h1>
  <div>
    <div id="session" class="meeting-window">
      <div id="session-header">
        
      </div>
      <div id="video-container" class="col-md-6">
        <UserVideo :stream-manager="state.publisher" />
        <UserVideo
          v-for="sub in state.subscribers"
          :key="sub.stream.connection.connectionId"
          :stream-manager="sub"
        />
      </div>
      <div class="meeting-button-box">
        <button type="button" class="green-button" @click="videoOnAndOff()">video</button>
        <button type="button" class="green-button" @click="audioOnAndOff()">audio</button>
        <button class="green-button" @click="exit">나가기</button>
        <button class="green-button" @click="state.isOpenedChattingWindow = !state.isOpenedChattingWindow">채팅</button>
      </div>
    </div>

    <div v-show="state.isOpenedChattingWindow" class="chatting-window">
      <ul id="chatting-content">
      </ul>
      <div class="">
        <input type="text" v-model="state.message" @keyup.enter="sendChat()" />
        <button type="button" @click="sendChat()">입력</button>
      </div>
    </div>
  </div>
</template>

<script>
// import BeforeMeeting from "@/components/meeting/BeforeMeeting.vue";
import { onMounted, reactive } from "@vue/runtime-core";
import router from "@/router";
import store from "@/store";
import { OpenVidu } from "openvidu-browser";
import UserVideo from "../components/Openvidu/UserVideo";
import axios from "axios";

axios.defaults.headers.post["Content-Type"] = "application/json";

const OPENVIDU_SERVER_URL = "https://i5b308.p.ssafy.io:4443";
const OPENVIDU_SERVER_SECRET = "MY_SECRET";

export default {
  name: "Meeting",
  props: {
    studyId: {
      type: String,
      required: true,
    },
    meetingName: {
      type: String,
      required: true,
    },
  },
  components: {
    // BeforeMeeting,
    UserVideo,
  },
  setup(props) {
    const state = reactive({
      isReady: true,
      OV: undefined,
      session: undefined,
      mainStreamManager: undefined,
      publisher: undefined,
      subscribers: [],

      mySessionId: "SessionA",
      myUserName: "Participant" + Math.floor(Math.random() * 100),

      videoEnabled: true,
      audioEnabled: true,

      message: "",
      sessionNum: 1,
      isOpenedChattingWindow: false
    });
    onMounted(() => {
      console.log(`${props.studyId}번 스터디, ${props.meetingId}번 방 입장 완료`);
      enter();
      // 이제 여기서 연결 준비를 하고 세팅을 하고, 들어가면 신호를 주고받고 해서 화상을 시작하면 된다.
    });

    const enter = () => {
      console.dir("스토어 정보:");
      console.dir(store.state);
      state.mySessionId = props.meetingName;
      state.myUserName = store.state.user.nickname + `(${store.state.user.email})`;
      joinSession();
    };
    const switchSession = (newSession) => {
      state.sessionNum = newSession;
    };
    const exit = () => {
      leaveSession();
      console.log(`/study/${props.studyId}`);
      router.push({ path: `/study/${props.studyId}` });
    };
    const joinSession = () => {
      state.OV = new OpenVidu();

      // --- Init a session ---
      state.session = state.OV.initSession();

      // --- Specify the actions when events take place in the session ---

      // On every new Stream received...
      state.session.on("streamCreated", ({ stream }) => {
        const subscriber = state.session.subscribe(stream);
        state.subscribers.push(subscriber);
      });

      // On every Stream destroyed...
      state.session.on("streamDestroyed", ({ stream }) => {
        const index = state.subscribers.indexOf(stream.streamManager, 0);
        if (index >= 0) {
          state.subscribers.splice(index, 1);
        }
      });

      // On every asynchronous exception...
      state.session.on("exception", ({ exception }) => {
        console.warn(exception);
      });

      state.session.on("signal:my-chat", (event) => {
        let receive = event.data.split("/");
        let userName = receive[0];
        let message = receive[1];
        document.getElementById("chatting-content").innerHTML += `<li>${userName}:</li>`;
        document.getElementById("chatting-content").innerHTML += `${message}`;
      });

      // --- Connect to the session with a valid user token ---

      // 'getToken' method is simulating what your server-side should do.
      // 'token' parameter should be retrieved and returned by your own backend
      getToken(state.mySessionId).then((token) => {
        state.session
          .connect(token, { clientData: state.myUserName })
          .then(() => {
            // --- Get your own camera stream with the desired properties ---

            let publisher = state.OV.initPublisher(undefined, {
              audioSource: undefined, // The source of audio. If undefined default microphone
              videoSource: undefined, // The source of video. If undefined default webcam
              publishAudio: true, // Whether you want to start publishing with your audio unmuted or not
              publishVideo: true, // Whether you want to start publishing with your video enabled or not
              resolution: "320x240", // The resolution of your video
              frameRate: 30, // The frame rate of your video
              insertMode: "APPEND", // How the video is inserted in the target element 'video-container'
              mirror: false, // Whether to mirror your local video or not
            });

            state.mainStreamManager = publisher;
            state.publisher = publisher;

            // --- Publish your stream ---

            state.session.publish(state.publisher);
          })
          .catch((error) => {
            console.log("There was an error connecting to the session:", error.code, error.message);
          });
      });

      window.addEventListener("beforeunload", leaveSession);
    };
    const leaveSession = () => {
      if (state.session) state.session.disconnect();

      state.session = undefined;
      state.mainStreamManager = undefined;
      state.publisher = undefined;
      state.subscribers = [];
      state.OV = undefined;

      window.removeEventListener("beforeunload", leaveSession);
    };
    const getToken = (mySessionId) => {
      return createSession(mySessionId).then((sessionId) => createToken(sessionId));
    };
    const createSession = (sessionId) => {
      return new Promise((resolve, reject) => {
        axios
          .post(
            `${OPENVIDU_SERVER_URL}/openvidu/api/sessions`,
            JSON.stringify({
              customSessionId: sessionId,
            }),
            {
              auth: {
                username: "OPENVIDUAPP",
                password: OPENVIDU_SERVER_SECRET,
              },
            }
          )
          .then((response) => response.data)
          .then((data) => resolve(data.id))
          .catch((error) => {
            if (error.response.status === 409) {
              resolve(sessionId);
            } else {
              console.warn(
                `No connection to OpenVidu Server. This may be a certificate error at ${OPENVIDU_SERVER_URL}`
              );
              if (
                window.confirm(
                  `No connection to OpenVidu Server. This may be a certificate error at ${OPENVIDU_SERVER_URL}\n\nClick OK to navigate and accept it. If no certificate warning is shown, then check that your OpenVidu Server is up and running at "${OPENVIDU_SERVER_URL}"`
                )
              ) {
                location.assign(`${OPENVIDU_SERVER_URL}/accept-certificate`);
              }
              reject(error.response);
            }
          });
      });
    };
    const createToken = (sessionId) => {
      return new Promise((resolve, reject) => {
        axios
          .post(
            `${OPENVIDU_SERVER_URL}/openvidu/api/sessions/${sessionId}/connection`,
            {},
            {
              auth: {
                username: "OPENVIDUAPP",
                password: OPENVIDU_SERVER_SECRET,
              },
            }
          )
          .then((response) => response.data)
          .then((data) => resolve(data.token))
          .catch((error) => reject(error.response));
      });
    };
    const videoOnAndOff = () => {
      state.videoEnabled = !state.videoEnabled;
      state.publisher.publishVideo(state.videoEnabled);
    };
    const audioOnAndOff = () => {
      state.audioEnabled = !state.audioEnabled;
      state.publisher.publishAudio(state.audioEnabled);
    };
    const sendChat = () => {
      if (state.message && state.message != "") {
        state.session
          .signal({
            data: state.myUserName + "/" + state.message,
            to: [],
            type: "my-chat",
          })
          .then(() => {
            console.log("Message successfully sent");
          })
          .catch((error) => {
            console.error(error);
          });
      }
      state.message = "";
    };
    return {
      state,
      enter,
      switchSession,
      exit,
      joinSession,
      leaveSession,
      getToken,
      createSession,
      createToken,
      videoOnAndOff,
      audioOnAndOff,
      sendChat,
    };
  },
};
</script>

<style>
.meeting-button-box {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
  position: fixed;
  right: 35%;
  bottom: 0%;
}

#chatting-content {
  overflow-y: scroll;
  height: 300px;
}

.meeting-window {
  display: inline-block;
  width: 70%;
}

.chatting-window {
  display: inline-block;
  width: 30%;
  background-color: white;
}

.title {
  display: inline;
}
</style>
