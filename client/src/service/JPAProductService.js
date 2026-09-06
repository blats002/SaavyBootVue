import createJpaService from './JPAService';

const jpaService = createJpaService('product');

const productService = {
    findAll: () => jpaService.findAll(),
    //findByParent: (test) => jpaService.findByParent( 'ministry', test),
    createOrUpdate: (test) => {
        return jpaService.createOrUpdate(test);
    },
    delete: (id) => jpaService.delete(id),
    getFields: async () => await jpaService.getFieldsMeta(),
    getMasterMeta: async () => await jpaService.getMasterMeta(),
    getDetailMeta: async () => await jpaService.getDetailMeta()
};

export default productService;

export const createEmptyProduct = () => ({
    description: '',
    size: null
});

