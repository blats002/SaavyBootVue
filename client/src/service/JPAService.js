import axios from 'axios';
import AuthService from '@/service/AuthService';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

// Attach JWT token to all requests
axios.interceptors.request.use(
    (config) => {
        const token = AuthService.getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Redirect to login on 401 Unauthorized
axios.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            AuthService.logout();
            if (window.location.pathname !== '/auth/login') {
                window.location.href = '/auth/login';
            }
        }
        return Promise.reject(error);
    }
);

const createJpaService = (apiEndpointName) => ({
    findAll: () => axios.get(`${SERVER_URL}/api/${apiEndpointName}`).then((res) => res.data || []),
    findByParent: (apiParentName, jpaEntity) => axios.get(`${SERVER_URL}/api/${apiEndpointName}/by-${apiParentName}/${jpaEntity.id}`).then( async (res) => res.data || []),
    findByParentWithPage: (apiParentName, jpaEntity, payload) => axios.get(`${SERVER_URL}/api/${apiEndpointName}/by-${apiParentName}/${jpaEntity.id}/page`,{params:payload}).then( async (res) => res.data || []),
    createOrUpdate: (jpaEntity) => {
        if (jpaEntity.id) {
            return axios.put(`${SERVER_URL}/api/${apiEndpointName}/${jpaEntity.id}`, jpaEntity).then((res) => res.data);
        }

        return axios.post(`${SERVER_URL}/api/${apiEndpointName}`, jpaEntity).then((res) => res.data);
    },
    delete: (id) => axios.delete(`${SERVER_URL}/api/${apiEndpointName}/${id}`),
    getMasterMeta: async () => {
        return await axios.get(`${SERVER_URL}/api/metadata/${apiEndpointName}/mastermeta`).then(async (res) => {
            const meta = res.data;
            meta.title = meta.title;
            meta.dialogHeader = meta.dialogHeader;
            meta.optionLabel = meta.optionLabel;
            // meta.fields = await axios.get(`${SERVER_URL}/api/metadata/${res.data.masterEndPoint}/fields`).then(async (res) => {
            //     return res.data;
            // });
            const jpaService = createJpaService(meta.masterEndPoint);
            meta.service = jpaService;
            meta.fields = await jpaService.getFieldsMeta();
            meta.messages = JSON.parse(meta.messagesJSON);

            // meta.createEmptyRecord  = () => (createEmptyRecord(meta.fields));

            console.log(meta);
            return meta;
        });
    },
    getDetailMeta: async () => {
        return await axios.get(`${SERVER_URL}/api/metadata/${apiEndpointName}/detailmeta`).then(async (res) => {
            const details = res.data;

            for (const meta of details) {
                const json = JSON.parse(meta.parentValueJSON);
                meta.parentValue = (entity) => ({
                    id: entity[json[json.id]],
                    name: entity[json[json.name]]
                });



                const jpaService = createJpaService(meta.detailEndpoint);
                meta.service = jpaService;

                meta.fields = await jpaService.getFieldsMeta();

                meta.messages = JSON.parse(meta.messagesJSON);

                // meta.createEmptyRecord  = () => (createEmptyRecord(meta.fields));



            }
            console.log(details);
            return details;
        });
    },
    getFieldsMeta: async () => {
        return await axios.get(`${SERVER_URL}/api/metadata/${apiEndpointName}/fields`).then(async (res) => {
            const fields = res.data;
            for (const field of fields) {
                if (field.type === 'manyToOne' && field.optionsEndpoint) {
                    const jpaService = createJpaService(field.optionsEndpoint);
                    field.optionsService = jpaService;
                    field.optionsFields = await axios.get(`${SERVER_URL}/api/metadata/${field.optionsEndpoint}/fields`).then(async (res) => {
                        return res.data;
                    });
                } else if (field.type === 'enum') {
                    const json = JSON.parse(field.enumOptionsJSON);
                    field.options = json;
                }
            }
            console.log(fields);
            return fields;
        });
    },
    findAllWithPage: async (payload) => {
        return axios.get(`${SERVER_URL}/api/${apiEndpointName}/page`, {
            params: payload
        }).then((res) => {
            return res.data || [];
        });
    }
    // ,
    // getCreateEmptyRecord: async () => {
    //     return await axios.get(`${SERVER_URL}/api/metadata/${apiEndpointName}/emptyrecord`).then(async (res) => {
    //         const fields = res.data;
    //
    //         return fields;
    //     });
    // }
});


function createEmptyRecord(fields) {
    const record = {};

    fields.forEach(field => {
        switch (field.type) {
            case "text":
                record[field.name] = "";
                break;
            case "number":
                record[field.name] = null; // or 0
                break;
            case "image":
                record[field.name] = null;
                break;
            case "manyToOne":
                record[field.name] = null;
                break;
            default:
                record[field.name] = null;
        }
    });

    return record;
}


export default createJpaService;