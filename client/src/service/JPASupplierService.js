import createJpaService from '@/service/JPAService';

const jpaService = createJpaService('supplier');

const testService = {
    findAll: () => jpaService.findAll(),
    //findByParent: (test) => jpaService.findByParent( 'ministry', test),
    createOrUpdate: (test) => {
        return jpaService.createOrUpdate(test);
    },
    delete: (id) => jpaService.delete(id),
    getFields: async () => await jpaService.getFieldsMeta()
};

export default testService;

export const createEmptySupplier = () => ({
    description: '',
    size: null
});

