import createJpaService from './JPAService';

// const jpaService = createJpaService('roles');
//
// const roleService = {
//     findAll: () => jpaService.findAll(),
//     createOrUpdate: (role) => jpaService.createOrUpdate(role),
//     delete: (id) => jpaService.delete(id),
//     getFields: async () => await jpaService.getFieldsMeta(),
//     findAllWithPage: async (payload) => jpaService.findAllWithPage(payload)
// };
//
//
//
// export default roleService;
//
// export const createEmptyRole = () => ({
//     name: '',
//     description: ''
// });

const jpaService = createJpaService('roles');

const roleService = {
    findAll: () => jpaService.findAll(),
    findByParent: (field, id) => jpaService.findByParent(field, id),
    createOrUpdate: (invoice) => jpaService.createOrUpdate(invoice),
    delete: (id) => jpaService.delete(id),
    getFields: async () => await jpaService.getFieldsMeta(),
    getMasterMeta: async () => await jpaService.getMasterMeta(),
    getDetailMeta: async () => await jpaService.getDetailMeta(),
    findAllWithPage: async (payload) => jpaService.findAllWithPage(payload),
    findByParentWithPage: async (field, id, payload) => jpaService.findByParentWithPage(field, id, payload),
};

export default roleService;

export const createEmptyRole = () => ({
    name: '',
    description: '',
});
