<template>
  <div class="signup_sections">
    
    <section id="signupsection1" class="section1">
      <div class="sloganbox_signup">
        
        <div>
          <span class="welcome_signup">Welcome to</span>
          <span class="slogan_signup">Square4us</span>
        </div>
        <img class="logo_signup" src="/square4us.png" alt="">
      </div>
      <form id='signup-form' method="POST" @submit.prevent="signUp">
        <p><input class="input_signup" type="email" id="signup_email" name="email" placeholder="이메일 입력" v-model="credentials.email" @change="isValid = false;"><button class='green-button button-email-check' type="button" @click="checkEmail">중복 확인</button></p>
        <p><input class="input_signup" type="nickname" id="signup_nickname" name="nickname" placeholder="닉네임" v-model="credentials.nickname"></p>
        <p><input class="input_signup" type="password"  id="signup_password" name="password" placeholder="비밀번호" v-model="credentials.password"></p>
        <p><input class="input_signup" type="password"  id="signup_password_confirmation" name="password_confirmation" placeholder="비밀번호확인" v-model="password_confirmation"></p>
        <button class="green-button button-signup">Signup</button>
      </form>
    </section>
    <section id="signupsection2" class="section2">
      <img class="signup_img" src="/signup.jfif" alt="">
    </section>
  </div>
  
</template>

<script>
import { reactive, ref } from '@vue/reactivity'
import { useStore } from 'vuex'
import axios from 'axios'
export default {
  name: 'Signup',
  setup() {
    //로그인 객체
    const credentials = reactive({
      email : "",
      password : "",
      nickname : "",
    })
    let isValid = false;
    const password_confirmation = ref("");
    // 회원가입 함수
    const store = useStore();
    const checkEmail = async () => {
      if(!regExpTest(credentials.email)) {
        alert("이메일 형식이 잘못되었습니다!");
        return
      }
      axios({
        method: "GET",
        url: "member/checkEmail?email=" + credentials.email,
      }).then(() => {
        isValid = true;
        alert("사용 가능한 이메일입니다!");
      }).catch(() => {
        isValid = false;
        alert("사용할 수 없는 이메일입니다!");
      });
    }
    const regExpTest = (str) => {
      var regExp = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
      let res = regExp.test(str);
      return res;
    }
    const signUp = ()=>{
      if(isValid == false) {
        alert("이메일 중복 확인을 해주세요!");
        return;
      }
      if (password_confirmation.value !== credentials.password) {
        alert("비밀번호가 비밀번호 확인과 일치하지 않습니다")
        return
      }
      store.dispatch('signup', credentials)
    }
    return {
      credentials,
      isValid,
      regExpTest,
      password_confirmation,
      checkEmail,
      signUp,
    }
  }
}
</script>

<style>
.signup_sections {
  display: flex;
  justify-content: center;

}
.section1 {
  background-color: white;
  border-radius: 5px;
  margin: 50px 50px 0 50px;
  width: 350px;
  height: 500px;
}
.section2 {
  margin: 50px 50px 0 50px;
  height: 600px;
}
#signup_email {
  width: 210px;
  margin-right: 20px;
}
.button-email-check {
  display: inline !important;
  width: 70px !important;
  font-weight: 400 !important;
}

.button-signup {
  margin: 15px auto;
  background-color: #509186 !important;
}

.button-signup:hover {
  background-color: white !important;
}

#signup_email:hover {
  border:none;
  border-bottom: 2px #65BF9E solid;  
}
#signup_nickname:hover {
  border:none;
  border-bottom: 2px #65BF9E solid;
}
#signup_password:hover {
  border:none;
  border-bottom: 2px #65BF9E solid;
}
#signup_password_confirmation:hover {
  border:none;
  border-bottom: 2px #65BF9E solid;
}

  #signup-form {
    margin: 0px 0px auto;
    margin-top: 0;
  }
  .header_signup{
    height: 30px;
    display: flex;
    flex-direction: row-reverse;  
    justify-content: flex-start;
  }
  .sloganbox_signup {
    margin: 30px 0 30px 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    
  }
  .logo_signup {
    height: 60px;
    width: 60px;
    margin: 0 20px 0 20px;
  }
  .welcome_signup {
    display: flex;
    height: 25px;
    font: 25px sans-serif;
    text-align: flex-start;
    
  }
  .slogan_signup{
    height: 40px;
    font: 40px sans-serif;
    font-weight: bold;
    margin-top: 0;
    /* display: flex;
    justify-content: space-between; */
  }
  .input_signup {
    border:none;
    border-bottom: 1px lightgray solid;  
    margin-top: 1px;
    height: 40px;
    margin-bottom: 20px;
    width: 300px;
    flex-basis: 150px;
    box-sizing: border-box;
    border-radius: 3px;
    
  }
  p {
    margin: 0;
  }

  .signup_img{
    width: 350px;
    height: 500px;
    border-radius: 3px;
  }
</style>