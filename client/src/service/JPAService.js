import axios from 'axios';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

const createJpaService = (apiEndpointName) => ({
    findAll: () => axios.get(`${SERVER_URL}/api/${apiEndpointName}`).then((res) => res.data || []),
    findByParent: (apiParentName, jpaEntity) => axios.get(`${SERVER_URL}/api/${apiEndpointName}/by-${apiParentName}/${jpaEntity.id}`).then((res) => res.data || []),
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

                // if (field.type === 'enum') {
                //     const json = JSON.parse(field.enumOptionsJSON);
                //     field.options = json;
                // }

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
    }
});

export default createJpaService;