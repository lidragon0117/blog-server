import Vue from 'vue'
import Vuex from 'vuex'
import { loginApi, logoutApi, getUserInfoApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/cookie'

Vue.use(Vuex)
export default new Vuex.Store({
    state: {
        userInfo: sessionStorage.getItem("user") ? JSON.parse(sessionStorage.getItem("user")) : null,
        webSiteInfo: {
            showList: []
        },
        token: getToken() || '',
        searchVisible: false,
        mobileMenuVisible: false,
        visitorAccess: 0,
        siteAccess: 0,
        isLoading: false,
        notice: null,
        isUnread: false
    },
    mutations: {
        setSiteInfo(state, info) {
            state.webSiteInfo = info
        },
        SET_TOKEN(state, token) {
            state.token = token
            setToken(token)
        },
        SET_USER_INFO(state, userInfo) {
            state.userInfo = userInfo
            sessionStorage.setItem("user", JSON.stringify(userInfo))
        },

        SET_SEARCH_VISIBLE(state, visible) {
            state.searchVisible = visible
        },
        SET_MOBILE_MENU_VISIBLE(state, visible) {
            state.mobileMenuVisible = visible
        },
        setMobileMenuVisible(state, value) {
            state.mobileMenuVisible = value
        },
        setVisitorAccess(state, value) {
            state.visitorAccess = value
        },
        setSiteAccess(state, value) {
            state.siteAccess = value
        },
        SET_LOADING(state, status) {
            state.isLoading = status
        },
        SET_NOTICE(state, notice) {
            state.notice = notice
        }
    },
    actions: {

        /**
         * 设置公告信息
         */
        setNotice({ commit }, notice) {
            commit('SET_NOTICE', notice)
        },

        /**
         * 设置站点信息
         */
        setSiteInfo({ commit }, info) {
            commit('setSiteInfo', info)
        },
        /**
         * 获取用户信息
         */
        async getUserInfo({ commit }) {
            if (getToken()) {
                const res = await getUserInfoApi()
                commit('SET_USER_INFO', res.data)
            }
        },

        /**
         * 登录
         */
        async loginAction({ commit }, loginData) {
            console.log('开始登录，请求数据:', loginData)
            try {
                const res = await loginApi(loginData)
                console.log('登录接口返回完整数据:', res)
                if (res && res.data) {
                    console.log('登录返回data:', res.data)
                    console.log('accessToken:', res.data.accessToken)
                    commit('SET_TOKEN', res.data.accessToken)
                    commit('SET_USER_INFO', res.data)
                    console.log('token已设置到state和cookie')
                    return Promise.resolve(res)
                } else {
                    console.log('res或res.data不存在:', res)
                }
                return Promise.reject(res)
            } catch (error) {
                console.log('登录出错:', error)
                return Promise.reject(error)
            }
        },

        /**
         * 退出登录
         */
        async logout({ commit }) {
            await logoutApi()
            removeToken()
            commit('SET_USER_INFO', null)
        },

        showLoading({ commit }) {
            commit('SET_LOADING', true)
        },

        hideLoading({ commit }) {
            commit('SET_LOADING', false)
        }
    }
})