import axios from 'axios';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

const jpaService = {
    findAll: (apiEndpointName) => axios.get(`${SERVER_URL}/api/${apiEndpointName}`).then((res) => res.data || []),
    findByParent: (apiEndpointName, apiParentName, jpaEntity) => axios.get(`${SERVER_URL}/api/by-${apiEndpointName}/${apiParentName}}/${jpaEntity.id}`).then((res) => res.data || []),
    createOrUpdate: (apiEndpointName, jpaEntity) => {
        if (jpaEntity.id) {
            return axios.put(`${SERVER_URL}/api/${apiEndpointName}/${jpaEntity.id}`, jpaEntity).then((res) => res.data);
        }

        return axios.post(`${SERVER_URL}/api/${apiEndpointName}`, jpaEntity).then((res) => res.data);
    },
    delete: (apiEndpointName, id) => axios.delete(`${SERVER_URL}/api/${apiEndpointName}/${id}`)
};

export default jpaService;