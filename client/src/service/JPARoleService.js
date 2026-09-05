import createJpaService from '@/service/JPAService';

const jpaService = createJpaService('roles');

const roleService = {
    findAll: () => jpaService.findAll(),
    createOrUpdate: (role) => jpaService.createOrUpdate(role),
    delete: (id) => jpaService.delete(id),
    getFields: async () => await jpaService.getFieldsMeta(),
    findAllWithPage: async (payload) => jpaService.findAllWithPage(payload)
};

export default roleService;

export const createEmptyRole = () => ({
    name: '',
    description: ''
});
