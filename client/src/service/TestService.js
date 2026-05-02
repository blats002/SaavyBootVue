import axios from 'axios';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

export default class TestService {
    getTests() {
        return axios.get(`${SERVER_URL}/api/tests`)
            .then((res) => res.data)
            .then((data) => data || []);
    }

    saveTest(test) {
        return axios.post(`${SERVER_URL}/api/tests`, test, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((res) => res.data);
    }

    deleteTest(id) {
        return axios.delete(`${SERVER_URL}/api/tests/${id}`);
    }
}