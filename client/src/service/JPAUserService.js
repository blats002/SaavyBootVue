import createJpaService from './JPAService';

const jpaService = createJpaService('users');

import axios from 'axios';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

const userService = {
    findAll: () => jpaService.findAll(),
    createOrUpdate: (user) => jpaService.createOrUpdate(user),
    delete: (id) => jpaService.delete(id),
    getFields: async () => await jpaService.getFieldsMeta(),
    findAllWithPage: async (payload) => jpaService.findAllWithPage(payload),
    getMasterMeta: async () => await jpaService.getMasterMeta(),
    getDetailMeta: async () => await jpaService.getDetailMeta(),
    assignRole: (userId, roleId) => axios.post(`${SERVER_URL}/api/users/${userId}/roles/${roleId}`).then((res) => res.data),
    removeRole: (userId, roleId) => axios.delete(`${SERVER_URL}/api/users/${userId}/roles/${roleId}`).then((res) => res.data)
};

export default userService;

export const createEmptyUser = () => ({
    username: '',
    fullName: '',
    email: '',
    password: '',
    role: null,
    enabled: true
});
